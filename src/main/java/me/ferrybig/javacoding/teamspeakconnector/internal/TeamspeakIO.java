/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.ClientType;
import me.ferrybig.javacoding.teamspeakconnector.File;
import me.ferrybig.javacoding.teamspeakconnector.Server;
import me.ferrybig.javacoding.teamspeakconnector.ServerStatus;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.User;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

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
	private TeamspeakConnection con;
	private boolean started;

	public TeamspeakIO(Channel channel) {
		this.channel = Objects.requireNonNull(channel);
		this.closeFuture = channel.eventLoop().newPromise();
	}
	
	public void registerConnection(TeamspeakConnection con) {
		if(this.started) {
			throw new IllegalStateException("Already started");
		}
		this.con = con;
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
					if(channel.isActive()) {
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
				if (prom.isSuccess()) {
					synchronized (incomingQueue) {
						this.closed = true;
					}
					channel.close();
					LOG.fine("Closing channel because sendmessage asked it");
				}
			});
		}
		
		return prom;
	}

	public <T, R> Future<R> chainFuture(Future<T> future, Function<T, R> mapping) {
		return FutureUtil.chainFuture(this.channel.eventLoop().newPromise(), future, mapping);
	}

	private void channeClosed(Throwable upstream) {
		synchronized (incomingQueue) {
			con = null; // Help the garbage collector
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
		if(this.con == null) {
			throw new IllegalStateException("No TeamspeakConnection registered");
		}
		if(this.started) {
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
				super.exceptionCaught(ctx, cause);
				lastException = cause;
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				super.channelInactive(ctx);
				channeClosed(lastException);
			}

		});
		this.channel.pipeline().addLast(new SimpleChannelInboundHandler<Response>() {
			@Override
			protected void messageReceived(ChannelHandlerContext ctx, Response msg) throws Exception {
				LOG.log(Level.WARNING, "Unhandled packet: {0}", msg);
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

	public InetAddress tryConvertAddress(String address) {
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
				resolveEnum(ServerStatus.class, data.get("virtualserver_status")), 
				Integer.parseInt(data.getOrDefault("virtualserver_clientsonline", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_queryclientsonline", "0")), 
				Integer.parseInt(data.getOrDefault("virtualserver_maxclients", "0")), 
				Integer.parseInt(data.getOrDefault("virtualserver_uptime", "0")),
				data.get("virtualserver_name"), 
				data.get("virtualserver_autostart").equals("1"));
	}

	public me.ferrybig.javacoding.teamspeakconnector.Channel mapChannel(Map<String, String> data) {
		return new me.ferrybig.javacoding.teamspeakconnector.Channel(con, 
				Integer.parseInt(data.get("cid")),
				Integer.parseInt(data.get("channel_order")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("pid"))), 
				data.get("channel_name"), data.get("channel_topic"), 
				data.get("channel_flag_password").equals("1"), 
				Integer.parseInt(data.get("channel_needed_subscribe_power")), 
				Integer.parseInt(data.get("channel_needed_talk_power")), 
				data.get("channel_flag_default").equals("1"), 
				data.get("channel_flag_permanent").equals("1"), 
				Integer.parseInt(data.get("channel_icon_id")), 
				Integer.parseInt(data.get("total_clients_family")), 
				Integer.parseInt(data.get("channel_maxfamilyclients")), 
				Integer.parseInt(data.get("channel_maxclients")), 
				Integer.parseInt(data.get("total_clients")), 
				data.get("channel_flag_semi_permanent").equals("1"), 
				Integer.parseInt(data.get("channel_codec")), 
				Integer.parseInt(data.get("channel_codec_quality")));
	}

	public User mapUser(Map<String, String> data) {
		return new User(con, 
				Integer.parseInt(data.get("clid")), 
				con.getUnresolvedChannelById(Integer.parseInt(data.get("cid"))), 
				Integer.parseInt(data.get("client_database_id")), 
				data.get("client_nickname"), 
				ClientType.getById(Integer.parseInt(data.get("client_type"))), 
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
	
	public File mapFile(Map<String, String> data) {
		return null; // TODO
	}

	public <R> Future<R> mapComplexReponse(Future<ComplexResponse> in, Function<Map<String, String>, R> mapper) {
		return chainFuture(in, r -> {
			if(r.getCommands().size() != 1) {
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

	private Map<Integer, me.ferrybig.javacoding.teamspeakconnector.Channel> mapChannelParents(Map<Integer, me.ferrybig.javacoding.teamspeakconnector.Channel> list) {
		for (me.ferrybig.javacoding.teamspeakconnector.Channel c : list.values()) {
			me.ferrybig.javacoding.teamspeakconnector.Channel parent = list.get(c.getParent().getId());
			if (parent != null) {
				c.setParent(parent);
			}
		}
		return list;
	}

}
