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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import me.ferrybig.javacoding.teamspeakconnector.entities.File;
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.entities.NamedOnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.NamedUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannelGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedFile;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedGroup;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedOnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedPrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServerWithId;
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
import me.ferrybig.javacoding.teamspeakconnector.repository.ChannelRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.GroupRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.OfflineClientRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.OnlineClientRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.PrivilegeKeyRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.ServerRepository;

/**
 * Main teamspeak connection.
 * @author Fernando
 */
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

	private final RepositoryAccessor<GroupRepository> groups;
	private final RepositoryAccessor<PrivilegeKeyRepository> privilegeKeys;
	private final RepositoryAccessor<ServerRepository> servers;
	private final RepositoryAccessor<ChannelRepository> channels;
	private final RepositoryAccessor<OfflineClientRepository> offlineClients;
	private final RepositoryAccessor<OnlineClientRepository> onlineClients;
	private final RepositoryAccessor<SelfInformation> self;

	{
		final Object repositoryLock = new Object();
		groups = new RepositoryAccessor<>(GroupRepository::new,
				repositoryLock);
		privilegeKeys = new RepositoryAccessor<>(PrivilegeKeyRepository::new,
				repositoryLock);
		servers = new RepositoryAccessor<>(ServerRepository::new,
				repositoryLock);
		channels = new RepositoryAccessor<>(ChannelRepository::new,
				repositoryLock);
		offlineClients = new RepositoryAccessor<>(OfflineClientRepository::new,
				repositoryLock);
		onlineClients = new RepositoryAccessor<>(OnlineClientRepository::new,
				repositoryLock);
		self = new RepositoryAccessor<>(SelfInformation::new,
				repositoryLock);
	}

	/**
	 * Constructs a TeamspeakConnection from a completed Teamspeak IO
	 * @param io
	 */
	public TeamspeakConnection(TeamspeakIO io) {
		this.io = Objects.requireNonNull(io, "io");
	}

	/**
	 * Gets the internal IO object
	 * @return the internal IO object
	 */
	public final TeamspeakIO io() {
		return io;
	}

	/**
	 * Registers this TeamspeakConnection with the upstream TeamspeakIO
	 */
	public void start() {
		this.io.registerConnection(this);
		this.io.start();
		this.io.getChannel().pipeline().addLast(new InternalPacketHandler());
	}

	private void handleMessage(Response msg, NamedOnlineClient whoAmI) {
		LOG.log(Level.FINEST, "Who I am: {0}", whoAmI);
		final Map<String, String> options = msg.getOptions();
		final int invokerId = Integer.parseInt(options.get("invokerid"));
		if (invokerId == whoAmI.getClientId()) {
			LOG.finer("Dropped packet coming from our user");
			return;
		}

		final String message = options.get("msg");
		final String invokerName = options.get("invokername");
		final String invokeruid = options.get("invokeruid");
		final NamedOnlineClient invoker = invokerId == 0 ? null
				: onlineClients().unresolved(invokerId, invokeruid, invokerName);
		switch (parseInt(options.get("targetmode"))) {
			case 1: {
				privateMessageHandler.callAll(
						PrivateMessageListener::onPrivateMessage,
						new PrivateMessageEvent(
								onlineClients().unresolved(
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

	/**
	 * Gets the handler used for all server based messages
	 * @return the server handler
	 */
	public Handler<ServerMessageListener> getServerMessageHandler() {
		return serverMessageHandler;
	}

	/**
	 * Gets the handler used for all private messages
	 * @return the private message handler
	 */
	public Handler<PrivateMessageListener> getPrivateMessageHandler() {
		return privateMessageHandler;
	}

	/**
	 * Gets the handler used for all channel based messages
	 * @return the channel handler
	 */
	public Handler<ChannelMessageListener> getChannelMessageHandler() {
		return channelMessageHandler;
	}


	/**
	 * Gets the handler used for all server based actions
	 * @return the server handler
	 */
	public Handler<ServerListener> getServerHandler() {
		return serverHandler;
	}

	@Deprecated
	public UnresolvedChannel getUnresolvedChannelById(int id) {
		return channels().unresolved(id);
	}

	@Deprecated
	public UnresolvedGroup getUnresolvedGroupById(int id) {
		return groups().unresolved(id);
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

	@Deprecated
	public UnresolvedServerWithId getUnresolvedServerById(int id) {
		return servers().unresolved(id);
	}

	@Deprecated
	public UnresolvedPrivilegeKey getUnresolvedPrivilegeKeyById(String token) {
		return privilegeKeys().unresolved(token);
	}

	@Deprecated
	public Future<UnresolvedServerWithId> getUnresolvedServerByPort(int port) {
		return servers().unresolvedByPort(port).resolveTillId();
	}

	@Deprecated
	public Future<User> getUserById(int id) {
		return mapping().mapComplexReponse(io.sendPacket(
				Command.CLIENT_INFO
						.addData("clid", String.valueOf(id))
						.build()),
				mapping()::mapUser);
	}

	@Deprecated
	public UnresolvedUser getUnresolvedUserById(int id) {
		return new UnresolvedUser(this, id);
	}

	@Deprecated
	public NamedUser getUnresolvedNamedUser(int id,
			String nickname, String uniqueId) {
		return new NamedUser(this, id, nickname, uniqueId);
	}

	@Deprecated
	public Future<Channel> getChannelById(int id) {
		return channels().getById(id);
	}

	@Deprecated
	public Future<PrivilegeKey> getPrivilegeKeyById(String token) {
		return privilegeKeys().getById(token);
	}

	@Deprecated
	public Future<Server> getServer() {
		return servers().getSelected();
	}

	/**
	 * Sends a login packet upstream
	 * @param username The username
	 * @param password The password
	 * @return a future containing the results
	 */
	public Future<TeamspeakConnection> login(String username, String password) {
		return io.chainFuture(io.sendPacket(
				Command.LOG_IN
						.addData("client_login_name", username)
						.addData("client_login_password", password)
						.build()),
				packet -> this);
	}

	/**
	 * Shuts down the teamspeak server
	 * @return a future containing the results
	 */
	public Future<?> shutdownServer() {
		return io.sendPacket(Command.SERVER_PROCESS_STOP.build(),
				SendBehaviour.CLOSE_CONNECTION);
	}

	/**
	 * Sends a logout packet upstream
	 * @return a future containing the results
	 */
	public Future<TeamspeakConnection> logout() {
		return io.chainFuture(
				io.sendPacket(Command.LOG_OUT.build()),
				packet -> this
		);
	}

	@Deprecated
	public Future<List<Server>> getServerList() {
		return servers().list();
	}

	@Deprecated
	public Future<List<Channel>> getChannelList() {
		return channels().list();
	}

	@Deprecated
	public Future<List<User>> getUsersList() {
		return mapping().mapComplexReponseList(io.sendPacket(
				Command.CLIENT_LIST.addOption("uid")
						.addOption("away").addOption("voice")
						.addOption("groups").addOption("times")
						.addOption("info").addOption("icon")
						.addOption("country").addOption("ip").build()),
				mapping()::mapUser);
	}

	@Deprecated
	public Future<List<PrivilegeKey>> getPrivilegeKeyList() {
		return privilegeKeys().list();
	}

	/**
	 * Closes this connection. This may not always reflect on the close status,
	 * if you need this, call quit()
	 * @throws TeamspeakException optionally when an error happens during the closing
	 */
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

	/**
	 * Quits this connection, after quiting, no commands can be executed
	 * @return the future for quit progress
	 */
	public Future<?> quit() {
		return io.sendPacket(Command.QUIT.build(),
				SendBehaviour.FORCE_CLOSE_CONNECTION);
	}

	@Deprecated
	public Future<?> setOwnName(String name) {
		return self().setOwnName(name);
	}

	/**
	 * Gets a group by id
	 *
	 * @param serverGroupId id of the group
	 * @return a future pointed at the group with id serverGroupId
	 * @deprecated Use {@code groups().getById(serverGroupId)} instead
	 */
	@Deprecated
	public Future<Group> getGroupById(int serverGroupId) {
		// TODO: make this more efficient with caching
		return groups().getById(serverGroupId);
	}

	/**
	 * Get the server group list
	 *
	 * @return a future pointed at the server group list
	 * @deprecated Use {@code groups().list()} instead
	 */
	@Deprecated
	public Future<List<Group>> getGroups() {
		return groups().list();
	}

	/**
	 * Gets the group repository, for interacting with any teamspeak groups
	 * @return the group repository
	 */
	@Nonnull
	public GroupRepository groups() {
		return groups.get();
	}

	/**
	 * Gets the privilege key repository, for interacting with any teamspeak groups
	 * @return the privilege key repository
	 */
	@Nonnull
	public PrivilegeKeyRepository privilegeKeys() {
		return privilegeKeys.get();
	}

	/**
	 * Gets the server repository, for interacting with any teamspeak groups
	 * @return the server repository
	 */
	@Nonnull
	public ServerRepository servers() {
		return servers.get();
	}

	/**
	 * Gets the channel repository, for interacting with any teamspeak groups
	 * @return the channel repository
	 */
	@Nonnull
	public ChannelRepository channels() {
		return channels.get();
	}

	/**
	 * Gets the offlineClients repository, for interacting with any teamspeak groups
	 * @return the offlineClients repository
	 */
	@Nonnull
	public OfflineClientRepository offlineClients() {
		return offlineClients.get();
	}

	/**
	 * Gets the onlineClients repository, for interacting with any teamspeak groups
	 * @return the onlineClients repository
	 */
	@Nonnull
	public OnlineClientRepository onlineClients() {
		return onlineClients.get();
	}

	/**
	 * Gets the own information container, for interacting with itself
	 * @return the own information container
	 */
	@Nonnull
	public SelfInformation self() {
		return self.get();
	}

	/**
	 * Gets the mapper, a class used to transform protocol specific data
	 * @return the mapper
	 */
	@Nonnull
	public final Mapper mapping() {
		return mapper;
	}

	@ThreadSafe
	@ParametersAreNonnullByDefault
	private class RepositoryAccessor<T> {

		@Nonnull
		private final Function<TeamspeakConnection, T> newInstance;
		@Nonnull
		private final Object repositoryLock;
		@Nonnull
		private volatile Reference<T> ref = new WeakReference<>(null);

		public RepositoryAccessor(Function<TeamspeakConnection, T> newInstance, Object repositoryLock) {
			this.newInstance = Objects.requireNonNull(newInstance, "newInstance");
			this.repositoryLock = Objects.requireNonNull(repositoryLock, "repositoryLock");
		}

		@CheckForNull
		@Nullable
		public T getIfLoaded() {
			return ref.get();
		}

		@Nonnull
		public T get() {
			T repo = getIfLoaded();
			if (repo != null) {
				return repo;
			}
			synchronized (repositoryLock) {
				repo = getIfLoaded();
				if (repo != null) {
					return repo;
				}
				repo = newInstance.apply(TeamspeakConnection.this);
				if (repo == null) {
					throw new IllegalStateException("Instance creator " + newInstance + " returned a null object");
				}
				makeRef(repo);
			}
			return repo;
		}

		@GuardedBy(value = "repositoryLock")
		@Nonnull
		private void makeRef(T object) {
			ref = new SoftReference<>(object);
		}

		@Override
		public String toString() {
			return "RepositoryAccessor{" + "newInstance=" + newInstance + ", ref=" + ref + '}';
		}

	}

	private class InternalPacketHandler extends SimpleChannelInboundHandler<Response> {

		private Response lastPacket = null;

		@Override
		protected void channelRead0(ChannelHandlerContext ctx,
				Response msg) throws Exception {
			final Map<String, String> options = msg.getOptions();
			LOG.log(Level.FINE, "Handling packet: {0}", msg);
			switch (msg.getCmd()) {
				case "notifytextmessage": {
					Future<NamedOnlineClient> whoami = self().whoAmI();
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
					// TODO: remove dummy test because findbugs is annoyed by
					//  future expansions
					assert Function.identity().apply(from) == from;
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
									onlineClients().unresolved(invokerId,
											invokerName, invokeruid)));
				}
				break;
				case "notifytokenused": {
					// clid=5 cldbid=4 cluid=zhPQ0oNLH8boM42jlbgTWC6G\\/64=
					// token=4oquHhp03YKofI4dYVBLWZ9Ik+Mf0M6ogomh5RsU
					// tokencustomset token1=7 token2=0
					UnresolvedOnlineClient client = onlineClients().unresolved(
							parseInt(options.get("clid")));
					int databaseId = parseInt(options.get("cldbid"));
					String uniqueId = options.get("cluid");
					PrivilegeKey key = privilegeKeys()
							.readEntityFromEvent(options);
					tokenUsedHandler.callAll(TokenListener::onTokenUsed,
							new TokenUsedEvent(
									client, databaseId, uniqueId, key));
				}
				break;
				default: {
					LOG.log(Level.WARNING, "Unhandled packet: {0}", msg);
				}
			}
		}
	}

}
