/*
 * The MIT License
 *
 * Copyright 2017 Fernando van Loenhout.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.entities.User;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

/**
 * Internal object to the Teamspeak connection, contains potentially unsafe
 * methods, and it is not recommended to call methods of this class by yourself.
 *
 */
public class TeamspeakIO {

	private static final Logger LOG = Logger.getLogger(TeamspeakIO.class.getName());
	private static final ByteBuf PING_PACKET = Unpooled.wrappedBuffer("\n".getBytes(StandardCharsets.UTF_8));

	@GuardedBy(value = "incomingQueue")
	private final Queue<PendingPacket> incomingQueue = new LinkedList<>();
	private final AtomicLong fileTransferId = new AtomicLong(1);
	private final Channel channel;
	private boolean closed = false;
	private final Promise<ComplexResponse> closeFuture;
	private TeamspeakConnection con;
	private boolean started;
	private volatile Future<User> whoAmIPromise;

	public TeamspeakIO(Channel channel) {
		this.channel = Objects.requireNonNull(channel);
		this.closeFuture = channel.eventLoop().newPromise();
		this.closeFuture.setUncancellable();
	}

	public void registerConnection(TeamspeakConnection con) {
		if (this.started || this.con != null) {
			throw new IllegalStateException("Already started");
		}
		this.con = con;
	}

	public Future<ComplexResponse> sendPacket(String raw) {
		return sendPacket(new ComplexRequest(raw, true), SendBehaviour.NORMAL);
	}

	public Future<ComplexResponse> sendPacket(String raw, SendBehaviour sendBehaviour) {
		return sendPacket(new ComplexRequest(raw, true), sendBehaviour);
	}

	public Future<ComplexResponse> sendPacket(ComplexRequest req) {
		return sendPacket(req, SendBehaviour.NORMAL);
	}

	public Future<ComplexResponse> sendPacket(ComplexRequest req, SendBehaviour sendBehaviour) {
		if (closed) {
			if (sendBehaviour != SendBehaviour.NORMAL) {
				return this.closeFuture;
			}
			return channel.eventLoop().newFailedFuture(new TeamspeakException("Channel closed"));
		}
		Promise<ComplexResponse> prom = channel.eventLoop().newPromise();
		ChannelFuture future;
		synchronized (incomingQueue) {
			if (closed) {
				if (sendBehaviour != SendBehaviour.NORMAL) {
					return this.closeFuture;
				}
				return prom.setFailure(new TeamspeakException("Channel closed"));
			}
			incomingQueue.offer(new PendingPacket(prom, req, sendBehaviour));
			future = channel.writeAndFlush(req);
		}
		future.addListener(upstream -> {
			assert upstream == future;
			if (sendBehaviour == SendBehaviour.FORCE_CLOSE_CONNECTION) {
				channel.eventLoop().schedule(() -> {
					if (channel.isActive()) {
						LOG.fine("Closing channel by timeout");
						channel.close();
					}
				}, 10, TimeUnit.SECONDS);
			}
			if (!upstream.isSuccess()) {
				synchronized (incomingQueue) {
					if (incomingQueue.removeIf(prom::equals)) {
						prom.setFailure(new TeamspeakException("Exception during sending", upstream.cause()));
					}
				}
			}
		});
		if (sendBehaviour == SendBehaviour.CLOSE_CONNECTION || sendBehaviour == SendBehaviour.FORCE_CLOSE_CONNECTION) {
			prom.addListener(upstream -> {
				assert upstream == prom;
				if (!prom.isSuccess()) {
					LOG.log(Level.WARNING, "Failed to close channel cleanly: {0}", prom.cause());
				}
				synchronized (incomingQueue) {
					this.closed = true;
				}
				this.closeFuture.trySuccess(prom.isSuccess() ? prom.get() : null);
				channel.close();
				LOG.fine("Closing channel because sendmessage asked it");
			});
		}

		return prom;
	}

	public <T, R> Future<R> chainFuture(Future<T> future, Function<T, R> mapping) {
		return FutureUtil.chainFuture(newPromise(), future, mapping);
	}

	private void channelClosed(Throwable upstream) {
		synchronized (incomingQueue) {
			con = null; // Help the garbage collector
			closed = true;
			PendingPacket poll;
			TeamspeakException ex = new TeamspeakException("Channel closed");
			if (upstream != null) {
				ex.initCause(upstream);
				this.closeFuture.tryFailure(upstream);
			} else {
				this.closeFuture.trySuccess(null);
			}
			LOG.log(Level.FINE, "Marking {0} PendingPackets as closed", incomingQueue.size());
			while ((poll = incomingQueue.poll()) != null) {
				poll.onChannelClose(upstream);
			}
		}
	}

	public <T> Promise<T> newPromise() {
		return this.channel.eventLoop().newPromise();
	}

	public void start() {
		if (this.con == null) {
			throw new IllegalStateException("No TeamspeakConnection registered");
		}
		if (this.started) {
			throw new IllegalStateException("Already started");
		}
		this.started = true;
		this.channel.pipeline().addLast(new SimpleChannelInboundHandler<ComplexResponse>() {
			private Throwable lastException = null;

			@Override
			protected void messageReceived(ChannelHandlerContext ctx, ComplexResponse msg) throws Exception {
				recievePacket(msg);
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				lastException = cause;
				ctx.close();
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				super.channelInactive(ctx);
				channelClosed(lastException);
			}

		});
	}

	private void recievePacket(ComplexResponse r) {
		LOG.log(Level.FINE, "Packet received with {0} commands", r.getCommands());
		PendingPacket prom;
		synchronized (incomingQueue) {
			prom = incomingQueue.remove();
		}
		prom.onResponseReceived(r);

	}

	public <T> Future<T> getCompletedFuture(T object) {
		return channel.eventLoop().newSucceededFuture(object);
	}

	public Channel getChannel() {
		return channel;
	}

	/**
	 * Pings the server, and returns a future stating when the ping was
	 * delivered to the underlying channel
	 *
	 * @return the result of the ping
	 */
	public Future<?> ping() {
		return channel.writeAndFlush(PING_PACKET.retain());
	}

	public long generateFileTransferId() {
		return fileTransferId.getAndIncrement();
	}

	/**
	 * Call this method after changing a server, so the api knows it should
	 * refetch the information related to the server
	 */
	public void notifyServerChanged() {
		this.whoAmIPromise = null;
		whoAmI();
	}

	public Future<User> whoAmI() {
		Future<User> whoami = this.whoAmIPromise;
		if (whoami != null) {
			return whoami;
		}
		synchronized (this) {
			whoami = this.whoAmIPromise;
			if (whoami != null) {
				return whoami;
			}
			whoami = con.mapping().mapComplexReponse(
					sendPacket(Command.WHOAMI.build()),
					con.mapping()::mapWhoAmI);
			this.whoAmIPromise = whoami;
		}
		return whoami;
	}

}
