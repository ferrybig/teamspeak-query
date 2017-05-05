/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import me.ferrybig.javacoding.teamspeakconnector.event.ChannelMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.event.Handler;
import me.ferrybig.javacoding.teamspeakconnector.event.PrivateMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.internal.SendBehaviour;
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
	private final SubscriptionHandler<ServerMessageListener> serverMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textserver").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textserver").build());
	private final SubscriptionHandler<PrivateMessageListener> privateMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textprivate").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textprivate").build());
	private final SubscriptionHandler<ChannelMessageListener> channelMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textchannel").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textchannel").build());

	public TeamspeakConnection(TeamspeakIO channel) {
		this.io = channel;
	}

	public final TeamspeakIO io() {
		return io;
	}

	public void start() {
		this.io.registerConnection(this);
		this.io.start();
	}

	public Handler<ServerMessageListener> getServerMessageHandler() {
		return serverMessageHandler;
	}

	public Handler<PrivateMessageListener> getPrivateMessageHandler() {
		return privateMessageHandler;
	}

	public Handler<ChannelMessageListener> getChannelMessageHandler() {
		return channelMessageHandler;
	}

	public UnresolvedChannel getUnresolvedChannelById(int id) {
		return new UnresolvedChannel(this, id);
	}

	public UnresolvedGroup getUnresolvedGroupById(int id) {
		return null; // TODO
	}

	public UnresolvedChannelGroup getUnresolvedChannelGroupById(int id) {
		return null; // TODO
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
		return io.mapComplexReponse(io.sendPacket(
				new ComplexRequestBuilder("clientinfo")
						.addData("clid", String.valueOf(id))
						.build()),
				io::mapUser);
	}

	public UnresolvedUser getUnresolvedUserById(int id) {
		return new UnresolvedUser(this, id);
	}

	public Future<Channel> getChannelById(int id) {
		return io.mapComplexReponse(io.sendPacket(
				new ComplexRequestBuilder("channelinfo")
						.addData("cid", String.valueOf(id))
						.build()),
				io::mapChannel);
	}

	public Future<Server> getServer() {
		return io.mapComplexReponse(io.sendPacket(
				new ComplexRequestBuilder("serverinfo").build()),
				io::mapServer);
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
		return io.sendPacket(new ComplexRequestBuilder("serverprocessstop").build(), SendBehaviour.CLOSE_CONNECTION);
	}

	public Future<TeamspeakConnection> logout() {
		return io.chainFuture(
				io.sendPacket(new ComplexRequestBuilder("quit").build()),
				packet -> this
		);
	}

	public Future<List<Server>> getServerList() {
		return io.mapComplexReponseList(io.sendPacket(
				new ComplexRequestBuilder("serverlist").addOption("virtual").build()),
				io::mapServer);
	}

	public Future<List<Channel>> getChannelList() {
		return io.mapComplexReponseList(io.sendPacket(
				new ComplexRequestBuilder("channellist").addOption("topic").addOption("flags").addOption("voice").addOption("limits").addOption("icon").build()),
				io::mapChannel);
	}

	public Future<List<User>> getUsersList() {
		return io.mapComplexReponseList(io.sendPacket(
				new ComplexRequestBuilder("clientlist").addOption("uid")
						.addOption("away").addOption("voice").addOption("groups")
						.addOption("times").addOption("info").addOption("icon")
						.addOption("country").addOption("ip").build()),
				io::mapUser);
	}

	@Override
	public void close() throws TeamspeakException {
		try {
			io.sendPacket(new ComplexRequestBuilder("quit").build(), SendBehaviour.FORCE_CLOSE_CONNECTION).get();
		} catch (InterruptedException | ExecutionException ex) {
			if(ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new TeamspeakException(ex);
		}
	}

	public Future<?> quit() {
		return io.sendPacket(new ComplexRequestBuilder("quit").build(), SendBehaviour.FORCE_CLOSE_CONNECTION);
	}

	public Future<?> setOwnName(String name) {
		return io.sendPacket(new ComplexRequestBuilder("clientupdate").addData("client_nickname", name).build());
	}

}
