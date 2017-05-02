/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.ComplexPacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketEncoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;

/**
 * Internal object to the teamspeak connection, contains potentially usafe
 * methods, and it is not recommended to call methods of this class by yourself.
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class TeamspeakIO {

	private static final Logger LOG = Logger.getLogger(TeamspeakIO.class.getName());

	private final Queue<PendingPacket> incomingQueue = new LinkedList<>();
	private final Channel channel;
	private boolean closed = false;
	private final Promise<ComplexResponse> closeFuture;

	public TeamspeakIO(Channel channel) {
		this.channel = Objects.requireNonNull(channel);
		this.closeFuture = channel.eventLoop().newPromise();
	}

	public Future<ComplexResponse> sendPacket(ComplexRequest req) {
		return sendPacket(req, false);
	}

	public Future<ComplexResponse> sendPacket(ComplexRequest req, boolean closeConnectionAfterWrite) {
		if (closed) {
			if (closeConnectionAfterWrite) {
				return this.closeFuture;
			}
			return channel.eventLoop().newFailedFuture(new TeamspeakException("Channel closed"));
		}
		Promise<ComplexResponse> prom = channel.eventLoop().newPromise();
		ChannelFuture future;
		synchronized (incomingQueue) {
			if (closed) {
				if (closeConnectionAfterWrite) {
					return this.closeFuture;
				}
				return prom.setFailure(new TeamspeakException("Channel closed"));
			}
			incomingQueue.offer(new PendingPacket(prom, req, closeConnectionAfterWrite));
			future = channel.writeAndFlush(req);
		}
		future.addListener(upstream -> {
			assert upstream == future;
			if (!upstream.isSuccess()) {
				synchronized (incomingQueue) {
					if (incomingQueue.removeIf(prom::equals)) {
						prom.setFailure(new TeamspeakException("Exception during sending", upstream.cause()));
					}
				}
			}
		});
		if (closeConnectionAfterWrite) {
			prom.addListener(upstream -> {
				assert upstream == prom;
				if (prom.isSuccess()) {
					synchronized (incomingQueue) {
						this.closed = true;
					}
					channel.close();
				}
			});
		}
		return prom;
	}

	private <T, R> Future<R> delegateFutureResult(Future<T> future, Promise<R> prom, Function<T, R> map) {
		future.addListener(ignored -> {
			assert ignored == future;
			try {
				if (future.isSuccess()) {
					prom.setSuccess(map.apply(future.getNow()));
				} else {
					prom.setFailure(future.cause());
				}
			} catch (Throwable e) {
				prom.setFailure(e);
			}
		});
		return prom;
	}

	public <T, R> Future<R> chainFuture(Future<T> future, Function<T, R> mapping) {
		if (future.isDone()) {
			if (future.isSuccess()) {
				return channel.eventLoop().newSucceededFuture(mapping.apply(future.getNow()));
			} else {
				return channel.eventLoop().newFailedFuture(future.cause());
			}
		}
		return delegateFutureResult(future, channel.eventLoop().newPromise(), mapping);
	}

	public <T, R> Future<R> chainFutureFlat(Future<T> future, Function<T, Future<R>> mapping) {
		return chainFutureFlat(future, mapping, Function.identity());
	}

	public <T, I, R> Future<R> chainFutureFlat(Future<T> future, Function<T, Future<I>> mapping, Function<I, R> secondary) {
		if (future.isDone()) {
			if (future.isSuccess()) {
				return chainFuture(mapping.apply(future.getNow()), secondary);
			} else {
				return channel.eventLoop().newFailedFuture(future.cause());
			}
		}
		Promise<R> result = channel.eventLoop().newPromise();
		future.addListener(ignored -> {
			assert ignored == future;
			try {
				if (future.isSuccess()) {
					delegateFutureResult(mapping.apply(future.getNow()), result, secondary);
				} else {
					result.setFailure(future.cause());
				}
			} catch (Throwable e) {
				result.setFailure(e);
			}
		});
		return result;
	}

	private void channeClosed(Throwable upstream) {
		synchronized (incomingQueue) {
			closed = true;
			PendingPacket poll;
			TeamspeakException ex = new TeamspeakException("Channel closed");
			if (upstream != null) {
				ex.initCause(upstream);
			}
			LOG.log(Level.FINE, "Marking {0} PendingPackets as closed", incomingQueue.size());
			while ((poll = incomingQueue.poll()) != null) {
				poll.onChannelClose(upstream);
			}
		}
	}

	public void start() {
		this.channel.pipeline().addLast(new SimpleChannelInboundHandler<ComplexResponse>() {
			private Throwable lastException = null;

			@Override
			protected void messageReceived(ChannelHandlerContext ctx, ComplexResponse msg) throws Exception {
				recievePacket(msg);
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				super.exceptionCaught(ctx, cause);
				lastException = cause;
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				super.channelInactive(ctx);
				channeClosed(lastException);
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

}
