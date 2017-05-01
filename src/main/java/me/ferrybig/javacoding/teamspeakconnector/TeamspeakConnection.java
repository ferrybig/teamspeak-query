/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

/**
 *
 * @author Fernando
 */
public class TeamspeakConnection implements Closeable {

	private final TeamspeakIO io;
	private static final Logger LOG = Logger.getLogger(TeamspeakConnection.class.getName());

	public TeamspeakConnection(TeamspeakIO channel) {
		this.io = channel;
	}

	public void start() {
		this.io.start();
	}

	public Future<Channel> getChannelById(int id) {
		return getUnresolvedChannelById(id).resolv();
	}

	public UnresolvedChannel getUnresolvedChannelById(int id) {
		return new UnresolvedChannel(this, id);
	}

	protected Server mapServer(Map<String, String> server) {
		// int sid, int port, ServerStatus status, int clientsOnline, int queryClientsOnline, int maxClients, int uptime, String name, boolean autostart
		return new Server(this,
				Integer.parseInt(server.get("virtualserver_id")),
				Integer.parseInt(server.get("virtualserver_port")), 
				resolveEnum(ServerStatus.class,server.get("virtualserver_status").toUpperCase()),
				Integer.parseInt(server.getOrDefault("virtualserver_clientsonline", "0")), 
				Integer.parseInt(server.getOrDefault("virtualserver_queryclientsonline", "0")),
				Integer.parseInt(server.getOrDefault("virtualserver_maxclients", "0")),
				Integer.parseInt(server.getOrDefault("virtualserver_uptime", "0")), 
				server.get("virtualserver_name"), 
				server.get("virtualserver_autostart").equals("1"));
	}
	protected me.ferrybig.javacoding.teamspeakconnector.Channel mapChannel(Map<String, String> channel) {
		return new me.ferrybig.javacoding.teamspeakconnector.Channel(this,
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
	
	protected <E extends Enum<E>> E resolveEnum(Class<E> enu, String var) {
		return Enum.valueOf(enu, var.toUpperCase().replace(' ', '_'));
	}
	
	protected Map<Integer, me.ferrybig.javacoding.teamspeakconnector.Channel> mapChannelParents(Map<Integer, me.ferrybig.javacoding.teamspeakconnector.Channel> list) {
		for(me.ferrybig.javacoding.teamspeakconnector.Channel c : list.values()) {
			me.ferrybig.javacoding.teamspeakconnector.Channel parent = list.get(c.getParent().getId());
			if(parent != null) {
				c.setParent(parent);
			}
		}
		return list;
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
	
	public Future<List<me.ferrybig.javacoding.teamspeakconnector.Channel>> getChannelList() {
		return io.chainFuture(io.sendPacket(
				new ComplexRequestBuilder("channellist").addOption("topic").addOption("flags").addOption("voice").addOption("limits").addOption("icon").build()),
				packet -> packet.getCommands().stream().map(this::mapChannel).collect(Collectors.toList()));
	}

	public UnresolvedServer getUnresolvedServerById(int id) {
		return new UnresolvedServer(this, id);
	}

	public Future<User> getUserById(int id) {
		return getUnresolvedUserById(id).resolv();
	}

	public UnresolvedUser getUnresolvedUserById(int id) {
		return new UnresolvedUser(id);
	}

	public TeamspeakIO io() {
		return io;
	}
}
