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
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannelGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedFile;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedPrivilegeKey;
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
import me.ferrybig.javacoding.teamspeakconnector.internal.Mapper;
import me.ferrybig.javacoding.teamspeakconnector.internal.SendBehaviour;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import static me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command.SERVER_NOTIFY_REGISTER;
import static me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command.SERVER_NOTIFY_UNREGISTER;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;

@ThreadSafe
public class TeamspeakConnection implements Closeable {

	private static final Logger LOG = Logger.getLogger(TeamspeakConnection.class.getName());
	private final TeamspeakIO io;
	private final SubscriptionHandler<ServerMessageListener> serverMessageHandler = new SubscriptionHandler<>(this,
			SERVER_NOTIFY_REGISTER.addData("event", "textserver").build(),
			SERVER_NOTIFY_UNREGISTER.addData("event", "textserver").build());
	private final SubscriptionHandler<PrivateMessageListener> privateMessageHandler = new SubscriptionHandler<>(this,
			SERVER_NOTIFY_REGISTER.addData("event", "textprivate").build(),
			SERVER_NOTIFY_UNREGISTER.addData("event", "textprivate").build());
	private final SubscriptionHandler<ChannelMessageListener> channelMessageHandler = new SubscriptionHandler<>(this,
			SERVER_NOTIFY_REGISTER.addData("event", "textchannel").build(),
			SERVER_NOTIFY_UNREGISTER.addData("event", "textchannel").build());
	private final SubscriptionHandler<ServerListener> serverHandler = new SubscriptionHandler<>(this,
			SERVER_NOTIFY_REGISTER.addData("event", "server").build(),
			SERVER_NOTIFY_UNREGISTER.addData("event", "server").build());
	private final SubscriptionHandler<TokenListener> tokenUsedHandler = new SubscriptionHandler<>(this,
			SERVER_NOTIFY_REGISTER.addData("event", "tokenused").build(),
			SERVER_NOTIFY_UNREGISTER.addData("event", "tokenused").build());
	private final Mapper mapper = new Mapper(this);

	public TeamspeakConnection(TeamspeakIO channel) {
		this.io = channel;
	}

	public final TeamspeakIO io() {
		return io;
	}

	public void start() {
		this.io.registerConnection(this);
		this.io.start();
		this.io.getChannel().pipeline().addLast(new InternalPacketHandler());
	}

	private void handleMessage(Response msg, User whoAmI) {
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
		final NamedUser invoker = invokerId == 0 ? null
				: getUnresolvedNamedUser(invokerId, invokerName, invokeruid);
		switch (parseInt(options.get("targetmode"))) {
			case 1: {
				privateMessageHandler.callAll(
						PrivateMessageListener::onPrivateMessage,
						new PrivateMessageEvent(
								getUnresolvedUserById(
										parseInt(options.get("target"))),
								message, invoker));
			}
			break;
			case 2: {
				channelMessageHandler.callAll(
						ChannelMessageListener::onChannelMessage,
						new ChannelMessageEvent(message, invoker));
			}
			break;
			case 3: {
				serverMessageHandler.callAll(
						ServerMessageListener::onServerMessage,
						new ServerMessageEvent(message, invoker));
			}
			break;
			default: {
				assert false : "Target mode "
						+ options.get("targetmode") + " invalid";
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

	public Future<UnresolvedFile> getUnresolvedFileByChannelAndName(
			UnresolvedChannel channel, String name) {
		throw new UnsupportedOperationException(); // TODO;
	}

	public Future<File> getFileByChannelAndName(
			UnresolvedChannel channel, String name) {
		throw new UnsupportedOperationException(); // TODO;
	}

	public UnresolvedServer getUnresolvedServerById(int id) {
		return new UnresolvedServer(this, id);
	}

	public UnresolvedPrivilegeKey getUnresolvedPrivilegeKeyById(String token) {
		return new UnresolvedPrivilegeKey(this, token);
	}

	public Future<UnresolvedServer> getUnresolvedServerByPort(int port) {
		return this.io.chainFuture(
				this.io.sendPacket(Command.SERVER_ID_GET_BY_PORT
						.addData("virtualserver_port", port).build()),
				p -> getUnresolvedServerById(Integer.parseInt(
						p.getCommands().get(0).get("server_id"))));
	}

	public Future<User> getUserById(int id) {
		return mapping().mapComplexReponse(io.sendPacket(
				Command.CLIENT_INFO
						.addData("clid", String.valueOf(id))
						.build()),
				mapping()::mapUser);
	}

	public UnresolvedUser getUnresolvedUserById(int id) {
		return new UnresolvedUser(this, id);
	}

	public NamedUser getUnresolvedNamedUser(int id,
			String nickname, String uniqueId) {
		return new NamedUser(this, id, nickname, uniqueId);
	}

	public Future<Channel> getChannelById(int id) {
		return mapping().mapComplexReponse(io.sendPacket(
				Command.CHANNEL_INFO
						.addData("cid", String.valueOf(id))
						.build()),
				m -> {
					// This is needed because teamspeak doesn't repeat our send channel id
					m.put("cid", String.valueOf(id));
					return mapping().mapChannel(m);
				});
	}

	public Future<PrivilegeKey> getPrivilegeKeyById(String token) {
		return this.getUnresolvedPrivilegeKeyById(token).resolve();
	}

	public Future<Server> getServer() {
		return mapping().mapComplexReponse(io.sendPacket(
				Command.SERVER_INFO.build()),
				mapping()::mapServer);
	}

	public Future<TeamspeakConnection> login(String username, String password) {
		return io.chainFuture(io.sendPacket(
				Command.LOG_IN
						.addData("client_login_name", username)
						.addData("client_login_password", password)
						.build()),
				packet -> this);
	}

	public Future<?> shutdownServer() {
		return io.sendPacket(Command.SERVER_PROCESS_STOP.build(),
				SendBehaviour.CLOSE_CONNECTION);
	}

	public Future<TeamspeakConnection> logout() {
		return io.chainFuture(
				io.sendPacket(Command.LOG_OUT.build()),
				packet -> this
		);
	}

	public Future<List<Server>> getServerList() {
		return mapping().mapComplexReponseList(io.sendPacket(
				Command.SERVER_LIST.addOption("virtual").build()),
				mapping()::mapServer);
	}

	public Future<List<Channel>> getChannelList() {
		return mapping().mapComplexReponseList(io.sendPacket(
				Command.CHANNEL_LIST.addOption("topic").addOption("flags")
						.addOption("voice").addOption("limits")
						.addOption("icon").build()),
				mapping()::mapChannel);
	}

	public Future<List<User>> getUsersList() {
		return mapping().mapComplexReponseList(io.sendPacket(
				Command.CLIENT_LIST.addOption("uid")
						.addOption("away").addOption("voice")
						.addOption("groups").addOption("times")
						.addOption("info").addOption("icon")
						.addOption("country").addOption("ip").build()),
				mapping()::mapUser);
	}

	public Future<List<PrivilegeKey>> getPrivilegeKeyList() {
		return mapping().mapComplexReponseList(io().sendPacket(
				Command.PRIVILEGEKEY_LIST.build()),
				mapping()::mapPrivilegeKey);
	}

	@Override
	public void close() throws TeamspeakException {
		try {
			Future<?> sendPacket = quit();
			if (!this.io.getChannel().eventLoop().inEventLoop()) {
				sendPacket.get();
			} else if (sendPacket.isDone() && !sendPacket.isSuccess()) {
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
		return io.sendPacket(Command.QUIT.build(),
				SendBehaviour.FORCE_CLOSE_CONNECTION);
	}

	public Future<?> setOwnName(String name) {
		return io.sendPacket(Command.CLIENT_UPDATE
				.addData("client_nickname", name).build());
	}

	public Future<Group> getGroupById(int serverGroupId) {
		// TODO: make this more efficient with caching
		return io.chainFuture(getGroups(), l -> l.stream()
				.filter(g -> g.getServerGroupId() == serverGroupId)
				.findAny().orElseThrow(NoSuchElementException::new));
	}

	public Future<List<Group>> getGroups() {
		return mapping().mapComplexReponseList(io.sendPacket(
				Command.SERVER_GROUP_LIST.build()),
				mapping()::mapGroup);
	}

	public final Mapper mapping() {
		return mapper;
	}

	private class InternalPacketHandler extends SimpleChannelInboundHandler<Response> {

		private Response lastPacket = null;

		@Override
		protected void messageReceived(ChannelHandlerContext ctx,
				Response msg) throws Exception {
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
							LOG.fine("Handling delayed message delivery "
									+ "because whoami is not known");
							handleMessage(msg, whoami.get());
						});
					}
				}
				break;
				case "notifycliententerview": {
					if (msg.equals(lastPacket)) {
						LOG.log(Level.FINE, "Dropping packet {0} because "
								+ "teamspeak usually sends dublicate "
								+ "packets when both channel and server "
								+ "listener is active", msg);
						lastPacket = null;
						return;
					}
					lastPacket = msg;
					options.put("cid", options.get("ctid"));
					ShallowUser client = mapping().mapShallowUser(options);
					UnresolvedChannel from = "0".equals(options.get("cfid"))
							? null : getUnresolvedChannelById(
									parseInt(options.get("cfid")));
					ChangeReason reason = ChangeReason.getById(
							parseInt(options.get("reasonid")));
					ClientEnterViewEvent event;
					event = new ClientEnterViewEvent(client,
							client.getChannel(), reason, null);
					serverHandler.callAll(
							ServerListener::onClientEnterView, event);
					// TODO: channel change event when from == null
				}
				break;
				case "notifyclientleftview": {
					// TODO: leave notification
				}
				break;
				case "notifyserveredited": {
					final int invokerId
							= parseInt(options.get("invokerid"));
					final String invokerName = options.get("invokername");
					final String invokeruid = options.get("invokeruid");
					serverHandler.callAll(ServerListener::onEditServer,
							new ServerEditEvent(ChangeReason.getById(
									parseInt(options.get("reasonid"))),
									getUnresolvedNamedUser(invokerId,
											invokerName, invokeruid)));
				}
				break;
				case "notifytokenused": {
					// clid=5 cldbid=4 cluid=zhPQ0oNLH8boM42jlbgTWC6G\\/64=
					// token=4oquHhp03YKofI4dYVBLWZ9Ik+Mf0M6ogomh5RsU
					// tokencustomset token1=7 token2=0
					UnresolvedUser client = getUnresolvedUserById(
							parseInt(options.get("clid")));
					int databaseId = parseInt(options.get("cldbid"));
					String uniqueId = options.get("cluid");
					String token = options.get("token");
					String tokencustomset = options.get("tokencustomset");
					String token1 = options.get("token1");
					String token2 = options.get("token2");
					tokenUsedHandler.callAll(TokenListener::onTokenUsed,
							new TokenUsedEvent(client, databaseId, uniqueId,
									token, tokencustomset, token1, token2));
				}
				break;
				default: {
					LOG.log(Level.WARNING, "Unhandled packet: {0}", msg);
				}
			}
		}
	}

}
