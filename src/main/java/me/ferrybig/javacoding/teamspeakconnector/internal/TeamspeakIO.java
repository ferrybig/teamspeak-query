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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.entities.File;
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
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

	private InetAddress tryConvertAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException ex) {
			LOG.log(Level.FINE, "Trying to convert address to ip failed", ex);
			return null;
		}
	}

	public Server mapServer(Map<String, String> data) {
		return new Server(con,
				Integer.parseInt(data.get("virtualserver_id")),
				Integer.parseInt(data.get("virtualserver_port")),
				resolveEnum(Server.Status.class, data.get("virtualserver_status")),
				Integer.parseInt(data.getOrDefault("virtualserver_clientsonline", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_queryclientsonline", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_maxclients", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_uptime", "0")),
				data.get("virtualserver_name"),
				data.get("virtualserver_autostart").equals("1"));
	}

	public me.ferrybig.javacoding.teamspeakconnector.entities.Channel mapChannel(Map<String, String> data) {
		return new me.ferrybig.javacoding.teamspeakconnector.entities.Channel(con,
				Integer.parseInt(data.get("cid")),
				Integer.parseInt(data.get("channel_order")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("pid"))),
				data.get("channel_name"), data.get("channel_topic"),
				data.get("channel_flag_password").equals("1"),
				Integer.parseInt(data.getOrDefault("channel_needed_subscribe_power", "0")), // Not sure why this one is missing with `channelinfo`
				Integer.parseInt(data.get("channel_needed_talk_power")),
				data.get("channel_flag_default").equals("1"),
				data.get("channel_flag_permanent").equals("1"),
				Integer.parseInt(data.get("channel_icon_id")),
				Integer.parseInt(data.getOrDefault("total_clients_family", "0")), // These are missing with `channelinfo`
				Integer.parseInt(data.getOrDefault("channel_maxfamilyclients", "0")),
				Integer.parseInt(data.get("channel_maxclients")),
				Integer.parseInt(data.getOrDefault("total_clients", "0")),
				data.get("channel_flag_semi_permanent").equals("1"),
				me.ferrybig.javacoding.teamspeakconnector.entities.Channel.Codec.getById(Integer.parseInt(data.get("channel_codec"))),
				Integer.parseInt(data.get("channel_codec_quality")));
	}

	public User mapUser(Map<String, String> data) {
		return new User(con,
				Integer.parseInt(data.get("clid")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("cid"))),
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_nickname"),
				User.Type.getById(Integer.parseInt(data.get("client_type"))),
				data.get("client_away").equals("1") ? data.get("client_away_message") : null,
				data.get("client_flag_talking").equals("1"),
				data.get("client_input_muted").equals("1"),
				data.get("client_output_muted").equals("1"),
				data.get("client_input_hardware").equals("1"),
				data.get("client_output_hardware").equals("1"),
				Integer.parseInt(data.get("client_talk_power")),
				data.get("client_is_talker").equals("1"),
				data.get("client_is_priority_speaker").equals("1"),
				data.get("client_is_recording").equals("1"),
				data.get("client_is_channel_commander").equals("1"),
				data.get("client_unique_identifier"),
				mapIntList(data.get("client_servergroups"), con::getUnresolvedGroupById),
				mapIntList(data.get("client_channel_group_id"), con::getUnresolvedChannelGroupById),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_group_inherited_channel_id"))),
				data.get("client_version"),
				data.get("client_platform"),
				Integer.parseInt(data.get("client_idle_time")),
				Long.parseLong(data.get("client_created")),
				Long.parseLong(data.get("client_lastconnected")),
				Integer.parseInt(data.get("client_icon_id")),
				data.get("client_country"),
				tryConvertAddress(data.get("connection_client_ip")));
	}

	public ShallowUser mapShallowUser(Map<String, String> data) {
		return new ShallowUser(con,
				Integer.parseInt(data.get("clid")),
				data.get("client_unique_identifier"),
				Integer.parseInt(data.get("client_database_id")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("cid"))),
				data.get("client_nickname"),
				User.Type.getById(Integer.parseInt(data.get("client_type"))),
				data.get("client_away").equals("1") ? data.get("client_away_message") : null,
				data.get("client_input_muted").equals("1"),
				data.get("client_output_muted").equals("1"),
				data.get("client_input_hardware").equals("1"),
				data.get("client_output_hardware").equals("1"),
				Integer.parseInt(data.get("client_talk_power")),
				data.get("client_is_talker").equals("1"),
				data.get("client_is_priority_speaker").equals("1"),
				data.get("client_is_recording").equals("1"),
				data.get("client_is_channel_commander").equals("1"),
				mapIntList(data.get("client_servergroups"), con::getUnresolvedGroupById),
				mapIntList(data.get("client_channel_group_id"), con::getUnresolvedChannelGroupById),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_group_inherited_channel_id"))),
				Integer.parseInt(data.get("client_icon_id")),
				data.get("client_country"));
	}

	public User mapWhoAmI(Map<String, String> data) {
		return new User(con,
				Integer.parseInt(data.get("client_id")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_id"))),
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_nickname"),
				User.Type.QUERY,
				null,
				false,
				true,
				true,
				true,
				true,
				0,
				false,
				false,
				false,
				false,
				data.get("client_unique_identifier"),
				Collections.emptyList(),
				Collections.emptyList(),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_id"))),
				"", // TODO program version
				"", // TODO program platform
				0,
				0,
				0,
				0,
				"",
				((InetSocketAddress) channel.localAddress()).getAddress());
	}

	/**
	 * Map a group received from Teamspeak.
	 * @param data Map containing received objects
	 * @return the mapped group
	 */
	public Group mapGroup(Map<String, String> data) {
		return new Group(con,
				Integer.parseInt(data.get("sgid")),
				Integer.parseInt(data.get("sortid")),
				Integer.parseInt(data.get("iconid")),
				data.get("savedb").equals("1"),
				data.get("name"),
				Integer.parseInt(data.get("n_member_removep")),
				Integer.parseInt(data.get("n_member_addp")),
				Integer.parseInt(data.get("n_modifyp")),
				Integer.parseInt(data.get("namemode")),
				Group.Type.getById(Integer.parseInt(data.get("type"))));
	}

	public File mapFile(Map<String, String> data) {
		return null; // TODO
	}

	public <R> Future<R> mapComplexReponse(Future<ComplexResponse> in, Function<Map<String, String>, R> mapper) {
		return chainFuture(in, r -> {
			if (r.getCommands().size() != 1) {
				throw new IllegalArgumentException("Cannot map a response of size " + r.getCommands().size() + " as a simple instance");
			}
			return mapper.apply(r.getCommands().get(0));
		});
	}

	public <R> Future<List<R>> mapComplexReponseList(Future<ComplexResponse> in, Function<Map<String, String>, R> mapper) {
		return this.<R, R>mapComplexReponseList(in, mapper, Function.identity());
	}

	public <R, I> Future<List<R>> mapComplexReponseList(Future<ComplexResponse> in, Function<Map<String, String>, I> mapper, Function<List<I>, List<R>> finalizer) {
		return chainFuture(in, r -> {
			return finalizer.apply(r.getCommands().stream().map(mapper).collect(Collectors.toList()));
		});
	}

	public <R> List<R> mapIntList(String in, IntFunction<R> mapper) {
		return in.isEmpty() ? new ArrayList<>() : Arrays.stream(in.split(",")).mapToInt(Integer::parseInt).mapToObj(mapper).collect(Collectors.toList());
	}

	public <E extends Enum<E>> E resolveEnum(Class<E> enu, String var) {
		return Enum.valueOf(enu, var.toUpperCase().replace(' ', '_'));
	}

	private Map<Integer, me.ferrybig.javacoding.teamspeakconnector.entities.Channel> mapChannelParents(Map<Integer, me.ferrybig.javacoding.teamspeakconnector.entities.Channel> list) {
		for (me.ferrybig.javacoding.teamspeakconnector.entities.Channel c : list.values()) {
			me.ferrybig.javacoding.teamspeakconnector.entities.Channel parent = list.get(c.getParent().getId());
			if (parent != null) {
				c.setParent(parent);
			}
		}
		return list;
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
			whoami = this.mapComplexReponse(
					sendPacket(Command.WHOAMI.build()),
					this::mapWhoAmI);
			this.whoAmIPromise = whoami;
		}
		return whoami;
	}

}
