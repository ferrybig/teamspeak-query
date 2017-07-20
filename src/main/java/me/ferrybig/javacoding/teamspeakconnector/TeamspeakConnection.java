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
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import java.io.Closeable;
import static java.lang.Integer.parseInt;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import me.ferrybig.javacoding.teamspeakconnector.entities.File;
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.entities.NamedUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannelGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedFile;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.User;
import me.ferrybig.javacoding.teamspeakconnector.event.ChannelMessageEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ChannelMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.event.ClientEnterViewEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.PrivateMessageEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.PrivateMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerEditEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerListener;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerMessageEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerMessageListener;
import me.ferrybig.javacoding.teamspeakconnector.event.TokenListener;
import me.ferrybig.javacoding.teamspeakconnector.event.TokenUsedEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.meta.Handler;
import me.ferrybig.javacoding.teamspeakconnector.event.meta.SubscriptionHandler;
import me.ferrybig.javacoding.teamspeakconnector.internal.SendBehaviour;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;

@ThreadSafe
public class TeamspeakConnection implements Closeable {

	private static final Logger LOG = Logger.getLogger(TeamspeakConnection.class.getName());
	private final TeamspeakIO io;
	private final SubscriptionHandler<ServerMessageListener> serverMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textserver").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textserver").build());
	private final SubscriptionHandler<PrivateMessageListener> privateMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textprivate").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textprivate").build());
	private final SubscriptionHandler<ChannelMessageListener> channelMessageHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "textchannel").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "textchannel").build());
	private final SubscriptionHandler<ServerListener> serverHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "server").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "server").build());
	private final SubscriptionHandler<TokenListener> tokenUsedHandler = new SubscriptionHandler<>(this,
			new ComplexRequestBuilder("servernotifyregister").addData("event", "tokenused").build(),
			new ComplexRequestBuilder("servernotifyunregister").addData("event", "tokenused").build());

	public TeamspeakConnection(TeamspeakIO channel) {
		this.io = channel;
	}

	public final TeamspeakIO io() {
		return io;
	}

	public void start() {
		this.io.registerConnection(this);
		this.io.start();
		this.io.getChannel().pipeline().addLast(new SimpleChannelInboundHandler<Response>() {

			private Response lastPacket = null;
			//notifyclientleftview cfid=1 ctid=0 reasonid=8 reasonmsg=leaving clid=1
			//notifycliententerview cfid=0 ctid=1 reasonid=0 clid=4 client_unique_identifier=P+m\/uXn4o2nLwN5gOimuQcfQZIQ= client_nickname=Ferrybig client_input_muted=0 client_output_muted=0 client_outputonly_muted=0 client_input_hardware=0 client_output_hardware=1 client_meta_data client_is_recording=0 client_database_id=2 client_channel_group_id=5 client_servergroups=6,10 client_away=0 client_away_message client_type=0 client_flag_avatar=12f359409d033f5eebcc821a5dbcecf5 client_talk_power=75 client_talk_request=0 client_talk_request_msg client_description client_is_talker=0 client_is_priority_speaker=0 client_unread_messages=0 client_nickname_phonetic=Ferrybig client_needed_serverquery_view_power=75 client_icon_id=0 client_is_channel_commander=0 client_country client_channel_group_inherited_channel_id=1 client_badges=Overwolf=0
			//notifyclientleftview cfid=72 ctid=0 reasonid=8 reasonmsg=leaving clid=4
			//notifycliententerview cfid=0 ctid=1 reasonid=0 clid=5 client_unique_identifier=P+m\/uXn4o2nLwN5gOimuQcfQZIQ= client_nickname=Ferrybig client_input_muted=0 client_output_muted=0 client_outputonly_muted=0 client_input_hardware=0 client_output_hardware=1 client_meta_data client_is_recording=0 client_database_id=2 client_channel_group_id=5 client_servergroups=6,10 client_away=0 client_away_message client_type=0 client_flag_avatar=12f359409d033f5eebcc821a5dbcecf5 client_talk_power=75 client_talk_request=0 client_talk_request_msg client_description client_is_talker=0 client_is_priority_speaker=0 client_unread_messages=0 client_nickname_phonetic=Ferrybig client_needed_serverquery_view_power=75 client_icon_id=0 client_is_channel_commander=0 client_country client_channel_group_inherited_channel_id=1 client_badges=Overwolf=0

			@Override
			protected void messageReceived(ChannelHandlerContext ctx, Response msg) throws Exception {
				final Map<String, String> options = msg.getOptions();
				LOG.log(Level.FINE, "Handling packet: {0}", msg);
				switch (msg.getCmd()) {
					case "notifytextmessage": {
						Future<User> whoami = io.whoAmI();
						if (whoami.isSuccess()) {
							handleMessage(msg, whoami.get());
						} else {
							whoami.addListener(f -> {
								assert f == whoami;
								LOG.fine("Handling delayed message delivery because whoami is not known");
								handleMessage(msg, whoami.get());
							});
						}
					}
					break;
					case "notifycliententerview": {
						if (msg.equals(lastPacket)) {
							LOG.log(Level.FINE, "Dropping packet {0} because teamspeak usually sends dublicate packets when both channel and server listener is active", msg);
							lastPacket = null;
							return;
						}
						lastPacket = msg;
						options.put("cid", options.get("ctid"));
						ShallowUser client = io.mapShallowUser(options);
						UnresolvedChannel from = "0".equals(options.get("cfid")) ? null : getUnresolvedChannelById(parseInt(options.get("cfid")));
						ChangeReason reason = ChangeReason.getById(parseInt(options.get("reasonid")));
						ClientEnterViewEvent event;
						if (true && from == null) {
							event = new ClientEnterViewEvent(client, client.getChannel(), reason, null);
							serverHandler.callAll(ServerListener::onClientEnterView, event);
						} else {
							// TODO: channel change event
						}

					}
					break;
					case "notifyclientleftview": {
						// TODO: leave notification
					}
					break;
					case "notifyserveredited": {
						final int invokerId = Integer.parseInt(options.get("invokerid"));
						final String invokerName = options.get("invokername");
						final String invokeruid = options.get("invokeruid");
						serverHandler.callAll(ServerListener::onEditServer, new ServerEditEvent(
								ChangeReason.getById(parseInt(options.get("reasonid"))),
								getUnresolvedNamedUser(invokerId, invokerName, invokeruid)));
					}
					break;
					case "notifytokenused": {
						// clid=5 cldbid=4 cluid=zhPQ0oNLH8boM42jlbgTWC6G\\/64= token=4oquHhp03YKofI4dYVBLWZ9Ik+Mf0M6ogomh5RsU tokencustomset token1=7 token2=0
						final UnresolvedUser client = getUnresolvedUserById(parseInt(options.get("clid")));
						final int databaseId = parseInt(options.get("cldbid"));
						final String uniqueId = options.get("cluid");
						final String token = options.get("token");
						final String tokencustomset = options.get("tokencustomset");
						final String token1 = options.get("token1");
						final String token2 = options.get("token2");
						tokenUsedHandler.callAll(TokenListener::onTokenUsed, new TokenUsedEvent(
								client, databaseId, uniqueId, token, tokencustomset, token1, token2));
					}
					break;
					default: {
						LOG.log(Level.WARNING, "Unhandled packet: {0}", msg);
					}
				}
			}

		});
	}

	protected void handleMessage(Response msg, User whoAmI) {
		LOG.log(Level.FINEST, "Who I am: {0}", whoAmI);
		final Map<String, String> options = msg.getOptions();
		final int invokerId = Integer.parseInt(options.get("invokerid"));
		if (invokerId == whoAmI.getId()) {
			LOG.finer("Dropped packet coming from our user");
			return;
		}

		final String message = options.get("msg");
		final String invokerName = options.get("invokername");
		final String invokeruid = options.get("invokeruid");
		final NamedUser invoker = invokerId == 0 ? null : getUnresolvedNamedUser(invokerId, invokerName, invokeruid);
		switch (parseInt(options.get("targetmode"))) {
			case 1: {
				privateMessageHandler.callAll(PrivateMessageListener::onPrivateMessage,
						new PrivateMessageEvent(
								getUnresolvedUserById(parseInt(options.get("target"))),
								message,
								invoker));
			}
			break;
			case 2: {
				channelMessageHandler.callAll(ChannelMessageListener::onChannelMessage,
						new ChannelMessageEvent(
								message,
								invoker));
			}
			break;
			case 3: {
				serverMessageHandler.callAll(ServerMessageListener::onServerMessage,
						new ServerMessageEvent(
								message,
								invoker));
			}
			break;
			default: {
				assert false : "Target mode " + options.get("targetmode") + " invalid";
			}
		}

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

	public Handler<ServerListener> getServerHandler() {
		return serverHandler;
	}

	public UnresolvedChannel getUnresolvedChannelById(int id) {
		return new UnresolvedChannel(this, id);
	}

	public UnresolvedGroup getUnresolvedGroupById(int id) {
		return new UnresolvedGroup(this, id);
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

	public UnresolvedServer getUnresolvedServerByPort(int id) {
		throw new UnsupportedOperationException(); // TODO;
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

	public NamedUser getUnresolvedNamedUser(int id, String nickname, String uniqueId) {
		return new NamedUser(this, id, nickname, uniqueId);
	}

	public Future<Channel> getChannelById(int id) {
		return io.mapComplexReponse(io.sendPacket(
				new ComplexRequestBuilder("channelinfo")
						.addData("cid", String.valueOf(id))
						.build()),
				m -> {
					m.put("cid", String.valueOf(id)); // This is needed because teamspeak doesn't repeat our send channel id
					return io.mapChannel(m);
				});
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
			Future<ComplexResponse> sendPacket = io.sendPacket(new ComplexRequestBuilder("quit").build(), SendBehaviour.FORCE_CLOSE_CONNECTION);
			if (!this.io.getChannel().eventLoop().inEventLoop()) {
				sendPacket.get();
			} else if(sendPacket.isDone() && !sendPacket.isSuccess()) {
				throw new TeamspeakException(sendPacket.cause());
			}
		} catch (InterruptedException | ExecutionException ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
				return;
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

	public Future<Group> getGroupById(int serverGroupId) {
		return io.chainFuture(getGroups(), l -> l.stream().filter(g -> g.getServerGroupId() == serverGroupId).findAny().orElseThrow(NoSuchElementException::new)); // TODO: make this more efficient with caching
	}

	public Future<List<Group>> getGroups() {
		return io.mapComplexReponseList(io.sendPacket(
				new ComplexRequestBuilder("servergrouplist").build()),
				io::mapGroup);
	}

}
