/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.internal.SubscriptionHandler;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

/**
 *
 * @author Fernando
 */
public class TeamspeakConnection implements Closeable {

	private final TeamspeakIO io;
	private static final Logger LOG = Logger.getLogger(TeamspeakConnection.class.getName());
	private final SubscriptionHandler<ServerMessageListener> serverMessageListener = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textserver").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textserver").build());
	private final SubscriptionHandler<ServerMessageListener> privateMessageListener = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textprivate").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textprivate").build());
	private final SubscriptionHandler<ServerMessageListener> channelMessageListener = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textchannel").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textchannel").build());

	public TeamspeakConnection(TeamspeakIO channel) {
		this.io = channel;
	}

	public TeamspeakIO io() {
		return io;
	}
	
	public void start() {
		this.io.start();
	}

	protected Server mapServer(Map<String, String> server) {
		return new Server(this,
				Integer.parseInt(server.get("virtualserver_id")),
				Integer.parseInt(server.get("virtualserver_port")),
				resolveEnum(ServerStatus.class, server.get("virtualserver_status").toUpperCase()),
				Integer.parseInt(server.getOrDefault("virtualserver_clientsonline", "0")),
				Integer.parseInt(server.getOrDefault("virtualserver_queryclientsonline", "0")),
				Integer.parseInt(server.getOrDefault("virtualserver_maxclients", "0")),
				Integer.parseInt(server.getOrDefault("virtualserver_uptime", "0")),
				server.get("virtualserver_name"),
				server.get("virtualserver_autostart").equals("1"));
	}

	protected Channel mapChannel(Map<String, String> channel) {
		return new Channel(this,
				Integer.parseInt(channel.get("cid")),
				Integer.parseInt(channel.get("channel_order")),
				getUnresolvedChannelById(Integer.parseInt(channel.get("pid"))),
				channel.get("channel_name"),
				channel.get("channel_topic"),
				channel.get("channel_flag_password").equals("1"),
				Integer.parseInt(channel.get("channel_needed_subscribe_power")),
				Integer.parseInt(channel.get("channel_needed_talk_power")),
				channel.get("channel_flag_default").equals("1"),
				channel.get("channel_flag_permanent").equals("1"),
				Integer.parseInt(channel.get("channel_icon_id")),
				Integer.parseInt(channel.get("total_clients_family")),
				Integer.parseInt(channel.get("channel_maxfamilyclients")),
				Integer.parseInt(channel.get("channel_maxclients")),
				Integer.parseInt(channel.get("total_clients")),
				channel.get("channel_flag_semi_permanent").equals("1"),
				Integer.parseInt(channel.get("channel_codec")),
				Integer.parseInt(channel.get("channel_codec_quality"))
		);
	}

	protected User mapUser(Map<String, String> user) {
		return new User(this,
				Integer.parseInt(user.get("clid")),
				getUnresolvedChannelById(Integer.parseInt(user.get("cid"))),
				Integer.parseInt(user.get("client_database_id")),
				user.get("client_nickname"),
				ClientType.getById(Integer.parseInt(user.get("client_type"))),
				user.get("client_away").equals("1") ? user.get("client_away_message") : null,
				user.get("client_flag_talking").equals("1"),
				user.get("client_input_muted").equals("1"),
				user.get("client_output_muted").equals("1"),
				user.get("client_input_hardware").equals("1"),
				user.get("client_output_hardware").equals("1"),
				Integer.parseInt(user.get("client_talk_power")),
				user.get("client_is_talker").equals("1"),
				user.get("client_is_priority_speaker").equals("1"),
				user.get("client_is_recording").equals("1"),
				user.get("client_is_channel_commander").equals("1"),
				user.get("client_unique_identifier"),
				mapList(user.get("client_servergroups"), this::getUnresolvedGroupById),
				mapList(user.get("client_channel_group_id"), this::getUnresolvedChannelGroupById),
				getUnresolvedChannelById(Integer.parseInt(user.get("client_channel_group_inherited_channel_id"))),
				user.get("client_version"),
				user.get("client_platform"),
				Integer.parseInt(user.get("client_idle_time")),
				Long.parseLong(user.get("client_created")),
				Long.parseLong(user.get("client_lastconnected")),
				Integer.parseInt(user.get("client_icon_id")),
				user.get("client_country"),
				tryConvertAddress(user.get("connection_client_ip"))
		);
	}

	private <R> List<R> mapList(String in, IntFunction<R> mapper) {
		return in.isEmpty() ? new ArrayList<>()
				: Arrays.stream(in.split(","))
						.mapToInt(Integer::parseInt)
						.mapToObj(mapper)
						.collect(Collectors.toList());
	}

	private InetAddress tryConvertAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException ex) {
			LOG.log(Level.FINE, "Trying to convert address to ip failed", ex);
			return null;
		}
	}

	private <E extends Enum<E>> E resolveEnum(Class<E> enu, String var) {
		return Enum.valueOf(enu, var.toUpperCase().replace(' ', '_'));
	}

	private Map<Integer, Channel> mapChannelParents(Map<Integer, Channel> list) {
		for (me.ferrybig.javacoding.teamspeakconnector.Channel c : list.values()) {
			me.ferrybig.javacoding.teamspeakconnector.Channel parent = list.get(c.getParent().getId());
			if (parent != null) {
				c.setParent(parent);
			}
		}
		return list;
	}

	public UnresolvedChannel getUnresolvedChannelById(int id) {
		return new UnresolvedChannel(this, id);
	}

	public UnresolvedGroup getUnresolvedGroupById(int id) {
		throw new UnsupportedOperationException(); // TODO
	}

	public UnresolvedChannelGroup getUnresolvedChannelGroupById(int id) {
		throw new UnsupportedOperationException(); // TODO
	}

	public Future<UnresolvedFile> getUnresolvedFileByChannelAndName(UnresolvedChannel channel, String name) {
		throw new UnsupportedOperationException(); // TODO;
	}

	public Future<File> getFileByChannelAndName(UnresolvedChannel channel, String name) {
		throw new UnsupportedOperationException(); // TODO;
	}

	public UnresolvedServer getUnresolvedServerById(int id) {
		return new UnresolvedServer(this, id);
	}

	public Future<User> getUserById(int id) {
		return getUnresolvedUserById(id).resolv();
	}

	public UnresolvedUser getUnresolvedUserById(int id) {
		return new UnresolvedUser(this, id);
	}

	public Future<Channel> getChannelById(int id) {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("channelinfo")
						.addData("cid", String.valueOf(id))
						.build()),
				packet -> mapChannel(packet.getCommands().get(0)));
	}

	public Future<Server> getServer() {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("serverinfo").build()),
				packet -> mapServer(packet.getCommands().get(0)));
	}

	public Future<TeamspeakConnection> login(String username, String password) {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("login")
						.addData("client_login_name", username)
						.addData("client_login_password", password)
						.build()),
				packet -> this);
	}

	public Future<?> shutdownServer() {
		return io.sendPacket(new ComplexRequestBuilder("serverprocessstop").build(), true);
	}

	public Future<TeamspeakConnection> logout() {
		return io.chainFuture(
				io.sendPacket(new ComplexRequestBuilder("quit").build()),
				packet -> this
		);
	}

	public Future<List<Server>> getServerList() {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("serverlist").addOption("virtual").build()),
				packet -> packet.getCommands().stream().map(this::mapServer).collect(Collectors.toList()));
	}

	public Future<List<Channel>> getChannelList() {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("channellist").addOption("topic").addOption("flags").addOption("voice").addOption("limits").addOption("icon").build()),
				packet -> packet.getCommands().stream().map(this::mapChannel).collect(Collectors.toList()));
	}

	@Override
	public void close() throws TeamspeakException {
		try {
			io.sendPacket(new ComplexRequestBuilder("quit").build(), true).syncUninterruptibly().get();
		} catch (InterruptedException | ExecutionException ex) {
			throw new TeamspeakException(ex);
		}
	}

	public Future<?> quit() {
		return io.sendPacket(new ComplexRequestBuilder("quit").build(), true);
	}

}
