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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakConnectionInitizer;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketQueueBuffer;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

/**
 * Constructor for the Teamspeak api.
 *
 * Example usage:
 * <pre>
 * {@code
 * TeamspeakBootstrap ts = new TeamspeakBootstrap(group);
 * ts.login("root", "toor").selectServerID(1);
 * ts.clientName("TSQuery");
 * Future<TeamspeakConnection> future =
 *     ts.connect("ts.example.com", TeamspeakBootstrap.DEFAULT_QUERY_PORT);
 * future.addListener(f -> {
 *     assert f == future;
 *     if(future.isSuccess()) {
 *         System.out.println("Connection success");
 *     } else {
 *         System.out.println("Connection failure");
 *         future.cause().printStacktrace();
 *     }
 * }); }
 * </pre>
 *
 * Notice: This class is not safe for concurrent access, if one thread modifies
 * variables, while another thread repeaticly calls {@code connect}, it is
 * unspecified what the other thread will see. It is however safe to repeatable
 * call connect and call modify methods, or to call connect from multiple
 * threads without modifying the base object. If concurrent modification is
 * required, you should {@link TeamspeakBootstrap#clone} the object from your
 * main thread, before calling connect from your auxilery threads.
 *
 */
@NotThreadSafe
public class TeamspeakBootstrap {

	/**
	 * Default port used for the Teamspeak query interface
	 */
	public static final int DEFAULT_QUERY_PORT = 10011;
	private static final Logger LOG = Logger.getLogger(
			TeamspeakBootstrap.class.getName());

	private String username;
	private String password;
	private Integer virtualServerId;
	private Integer virtualServerPort;
	private String clientName;
	private EventLoopGroup group;
	private RateLimit ratelimit = RateLimit.AUTODETECT;

	/**
	 * Creates a @code{TeamspeakBootstrap}
	 *
	 * @param group the netty @code{EventLoopGroup} to use
	 */
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TeamspeakBootstrap(@Nonnull EventLoopGroup group) {
		group(group);
	}

	/**
	 * Returns the configured username, or null when no username is defined
	 *
	 * @see TeamspeakConnection#login(java.lang.String, java.lang.String)
	 * @see TeamspeakBootstrap#login(java.lang.String, java.lang.String)
	 * @return the username or null
	 */
	public String username() {
		return this.username;
	}

	/**
	 * Returns the configured password, or null when no password is defined
	 *
	 * @see TeamspeakConnection#login(java.lang.String, java.lang.String)
	 * @see TeamspeakBootstrap#login(java.lang.String, java.lang.String)
	 * @return the password or null
	 */
	@Nonnull
	public String password() {
		return this.password;
	}

	/**
	 * Sets the username and password
	 *
	 * @param username Username used to login
	 * @param password Password used to login
	 * @see TeamspeakConnection#login(java.lang.String, java.lang.String)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap login(
			@Nonnull String username, @Nonnull String password) {
		this.username = Objects.requireNonNull(username, "username");
		this.password = Objects.requireNonNull(password, "password");
		return this;
	}

	/**
	 * Removes any configured username and password login credentials
	 *
	 * @see TeamspeakConnection#login(java.lang.String, java.lang.String)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap noLogin() {
		this.username = null;
		this.password = null;
		return this;
	}

	/**
	 * Returns the configured rate limit implementation.
	 *
	 * @return the configured ratelimit
	 */
	@Nonnull
	public RateLimit rateLimit() {
		return ratelimit;
	}

	/**
	 * Sets the used rate limit. The default ratelimit is
	 * {@link RateLimit#AUTODETECT}, use this if you need to reset this
	 * property.
	 *
	 * @param rateLimit rateLimit implementation to set
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap rateLimit(@Nonnull RateLimit rateLimit) {
		this.ratelimit = Objects.requireNonNull(rateLimit, "rateLimit");
		return this;
	}

	/**
	 * Returns the selected server id, or null if none is selected
	 *
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @return server id
	 */
	public Integer selectServerID() {
		return this.virtualServerId;
	}

	/**
	 * Configure a server id to select after connecting
	 *
	 * @param serverId server id to connect to
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @return the selected server id
	 */
	@Nonnull
	public TeamspeakBootstrap selectServerID(int serverId) {
		this.virtualServerId = serverId;
		return this;
	}

	/**
	 * Removes the selection of the server
	 *
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @see TeamspeakBootstrap#selectServerID(int)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap noSelectServerID() {
		this.virtualServerId = null;
		return this;
	}

	/**
	 * Returns the selected server by port, or null if not defined
	 *
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @return port selected server
	 */
	public Integer selectServerPort() {
		return this.virtualServerPort;
	}

	/**
	 * Select the final server by port
	 *
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @param port port number of the virtual server
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap selectServerPort(int port) {
		this.virtualServerPort = port;
		return this;
	}

	/**
	 * Clears the selection of selected virtual server by port
	 *
	 * @see
	 * me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServer#select()
	 * @see TeamspeakBootstrap#selectServerPort(int)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap noSelectServerPort() {
		this.virtualServerPort = null;
		return this;
	}

	/**
	 * Returns the selected clientname, or null if no clientname is configured
	 *
	 * @see TeamspeakConnection#setOwnName(java.lang.String)
	 * @return the clientname
	 */
	public String clientName() {
		return this.clientName;
	}

	/**
	 * Select a clientname that will be used after connecting, if this name is
	 * already in use, the connection fails
	 *
	 * @param clientName the name to use for the clientname
	 * @see TeamspeakConnection#setOwnName(java.lang.String)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap clientName(@Nonnull String clientName) {
		this.clientName = clientName;
		return this;
	}

	/**
	 * Clears the selected clientname
	 *
	 * @see TeamspeakConnection#setOwnName(java.lang.String)
	 * @see TeamspeakBootstrap#clientName(java.lang.String)
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap noClientName() {
		this.clientName = null;
		return this;
	}

	/**
	 * Configures the group that is used in the connection. Usually this method
	 * isn't called since the group is provided by the constructor.
	 *
	 * @param group the group
	 * @throws NullPointerException If group is null
	 * @return this
	 */
	@Nonnull
	public TeamspeakBootstrap group(@Nonnull EventLoopGroup group) {
		this.group = Objects.requireNonNull(group, "group");
		return this;
	}

	/**
	 * Returns the group that is used in the connection.
	 *
	 * @return the configured group
	 */
	@Nonnull
	public EventLoopGroup group() {
		return group;
	}

	/**
	 * Internal method that generates the Netty bootstrap. This method can be
	 * overriden for a custom bootstrap, it is recommended that this methods
	 * calls the parent, and then adds more properties and options as needed.
	 *
	 * @param group the selected group
	 * @param init the initial ChannelHandler that should be added to the
	 * channel
	 * @return the generated netty bootstrap.
	 */
	@Nonnull
	protected Bootstrap generateBootstrap(
			@Nonnull EventLoopGroup group,
			@Nonnull ChannelInitializer<Channel> init) {
		Bootstrap bootstrap = new Bootstrap();
		if (group instanceof NioEventLoopGroup) {
			bootstrap.channel(NioSocketChannel.class);
		} else if (group instanceof EpollEventLoopGroup) {
			bootstrap.channel(EpollSocketChannel.class);
		} else if (group instanceof OioEventLoopGroup) {
			bootstrap.channel(OioSocketChannel.class);
		}
		bootstrap.handler(init);
		bootstrap.group(group);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		// We send relatively small packets, we benefit more from delayed ack
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		return bootstrap;
	}

	/**
	 * Happy eyeballs algorithm used to connect to a remote server in a seamless
	 * way when it has multiple ip addresses.
	 *
	 * @param bootstrap The generated bootstrap in the previous step
	 * @param addresses addresses to connect to, the best ones first
	 * @throws NullPointerException If bootstrap is null
	 * @throws IllegalArgumentException If {@code bootstrap.group()} is null
	 * @throws NullPointerException If endpoints is null
	 * @throws IllegalArgumentException If endpoints contains a null value
	 * @return a future giving the new connected socket.
	 * @see
	 * <a href="https://tools.ietf.org/html/rfc6555">https://tools.ietf.org/html/rfc6555</a>
	 */
	@Nonnull
	protected static Future<Channel> happyEyeballs(
			@Nonnull Bootstrap bootstrap,
			@Nonnull List<SocketAddress> addresses) {
		if (bootstrap.group() == null) {
			throw new IllegalArgumentException("bootstrap.group() == null");
		}
		return new ConnectionAttempt(bootstrap.group().next().newPromise(),
				addresses, bootstrap.group(), bootstrap).start();
	}

	/**
	 * Connect to a named domain name or ip address, any exceptions in the
	 * resolving of the name are propagated back using the future.
	 *
	 * @param endpoint domain name or ip address
	 * @param port port used
	 * @throws NullPointerException If endpoint is null
	 * @return the future connection
	 */
	@Nonnull
	public Future<TeamspeakConnection> connect(@Nonnull String endpoint, int port) {
		try {
			return connect(Arrays.stream(InetAddress.getAllByName(endpoint))
					.map(ip -> new InetSocketAddress(ip, port))
					.sorted(ratelimit.socketAddressComparator())
					.collect(Collectors.toList()));
		} catch (UnknownHostException ex) {
			return this.group.next().newFailedFuture(ex);
		}
	}

	/**
	 * Connect using a single ip address and port as target
	 *
	 * @param endpoint ip and port pair to connect to
	 * @throws NullPointerException If endpoint is null
	 * @return the future connection
	 */
	@Nonnull
	public Future<TeamspeakConnection> connect(@Nonnull SocketAddress endpoint) {
		return connect(Collections.singletonList(endpoint));
	}

	/**
	 * Connect to any of the passed endpoints
	 *
	 * @param endpoints a list of ip and port pairs
	 * @throws NullPointerException If endpoints is null
	 * @throws IllegalArgumentException If endpoints contains a null value
	 * @return the future connection
	 */
	@Nonnull
	public Future<TeamspeakConnection> connect(
			@Nonnull List<SocketAddress> endpoints) {
		if (this.group == null) {
			throw new IllegalStateException("Group is not defined");
		}
		final EventLoop next = this.group.next();
		if (endpoints.isEmpty()) {
			return next.newFailedFuture(new IllegalArgumentException(
					"No addresses specified"));
		}
		Bootstrap bootstrap = this.generateBootstrap(group,
				new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new PacketQueueBuffer());
			}
		});
		return connect(next, happyEyeballs(bootstrap, endpoints));
	}

	/**
	 * Decorates the {@code SocketChannel} to a {@code TeamspeakConnection}
	 *
	 * @param next The {@code EventLoop} that generates the promises
	 * @param channelFuture The future channel to decorate
	 * @throws NullPointerException If next is null
	 * @throws NullPointerException If channel is null
	 * @return the created and decorated @code{TeamspeakConnection}
	 */
	@SuppressWarnings("UseSpecificCatch")
	@Nonnull
	protected Future<TeamspeakConnection> connect(@Nonnull EventLoop next,
			@Nonnull Future<Channel> channelFuture) {
		Promise<TeamspeakConnection> inmidiateFuture = next.newPromise();
		RateLimit rateLimit = this.ratelimit;
		channelFuture.addListener(f -> {
			assert f == channelFuture;
			try {
				if (channelFuture.isSuccess()) {
					Channel ch = channelFuture.get();
					LOG.log(Level.INFO, "Connecting done! {0}", ch);
					ch.pipeline().get(PacketQueueBuffer.class).replace(
							new TeamspeakConnectionInitizer(
									inmidiateFuture, rateLimit, 20000));
				} else {
					inmidiateFuture.tryFailure(new TeamspeakException(
							"TSConnect: " + channelFuture.cause().getMessage(),
							channelFuture.cause()));
				}
			} catch (Throwable t) {
				inmidiateFuture.tryFailure(new TeamspeakException(
						"TSConnect: Internal exception: " + t.toString(), t));
			}
		});
		Future<TeamspeakConnection> finalFuture = decorateConnection(
				next, inmidiateFuture);
		finalFuture.addListener(f -> {
			assert f == finalFuture;
			if (!finalFuture.isSuccess() || finalFuture.isCancelled()) {
				if (inmidiateFuture.isSuccess()) {
					inmidiateFuture.get().quit();
				} else if (channelFuture.isSuccess()) {
					channelFuture.get().close();
				}
			}
		});
		return finalFuture;
	}

	/**
	 * Decorates the passed {@code TeamspeakConnection} with properties relating
	 * to the settings of the bootstrap.
	 *
	 * @param next The event loop to create more promises and other utilities
	 * @param connection A future for the {@code TeamspeakConnection}
	 * @throws NullPointerException If next is null
	 * @throws NullPointerException If connection is null
	 * @return A future for the decorated {@code TeamspeakConnection}
	 */
	@SuppressWarnings("LocalVariableHidesMemberVariable")
	@Nonnull
	protected Future<TeamspeakConnection> decorateConnection(
			@Nonnull EventLoop next,
			@Nonnull Future<TeamspeakConnection> connection) {
		String username = this.username;
		String password = this.password;
		if (username != null) {
			connection = FutureUtil.chainFutureFlat(
					next.newPromise(),
					connection, con -> con.login(username, password));
		}
		Integer virtualServerId = this.virtualServerId;
		if (virtualServerId != null) {
			connection = FutureUtil.chainFutureFlat(
					next.newPromise(),
					connection,
					con -> con
							.getUnresolvedServerById(virtualServerId).select());
		}
		Integer virtualServerPort = this.virtualServerPort;
		if (virtualServerPort != null) {
			connection = FutureUtil.chainFutureFlat(
					next.newPromise(),
					connection,
					con -> FutureUtil.chainFutureFlat(next.newPromise(),
							con.getUnresolvedServerByPort(virtualServerPort),
							server -> server.select()));
		}
		String clientName = this.clientName;
		if (clientName != null) {
			connection = FutureUtil.chainFutureFlat(next.newPromise(),
					connection, con -> con.setOwnName(clientName), (t, i) -> t);
		}
		return connection;
	}

	private static class ConnectionAttempt {

		private static final AtomicInteger TASK_ID_LIST = new AtomicInteger();
		private final int taskId = TASK_ID_LIST.getAndIncrement();
		private final Promise<Channel> result;
		private final List<SocketAddress> addresses;
		private final EventLoopGroup group;
		private final Bootstrap bootstrap;
		private final AtomicInteger connecting = new AtomicInteger();
		private final AtomicInteger tried = new AtomicInteger();
		private final AtomicInteger failed = new AtomicInteger();
		private ScheduledFuture<?> task;
		private final CopyOnWriteArrayList<Pair<SocketAddress, Throwable>> exceptions
				= new CopyOnWriteArrayList<>();

		public ConnectionAttempt(
				@Nonnull Promise<Channel> result,
				@Nonnull List<SocketAddress> addresses,
				@Nonnull EventLoopGroup group,
				@Nonnull Bootstrap bootstrap) {
			this.result = result;
			this.addresses = addresses;
			this.group = group;
			this.bootstrap = bootstrap;
			if (addresses == null) {
				throw new NullPointerException("addresses");
			}
			int index = 0;
			for (SocketAddress s : addresses) {
				if (s == null) {
					throw new IllegalArgumentException(
							"addresses.get(" + index + ") == null");
				}
				index++;
			}
		}

		@Nonnull
		private Throwable createException() {
			assert !exceptions.isEmpty();
			StringBuilder message = new StringBuilder("Unable to connect");
			for (Pair<SocketAddress, Throwable> ex : exceptions) {
				String addr = ex.getKey().toString();
				message.append("\n> ").append(ex.getValue().getMessage());
				if (!ex.getValue().getMessage().contains(addr)) {
					message.append(": ").append(addr);
				}
			}
			ConnectException exception = new ConnectException(
					message.toString(), "Unable to connect");
			if (exceptions.size() == 1) {
				exception.initCause(exceptions.get(0).getValue());
			} else {
				for (Pair<SocketAddress, Throwable> ex : exceptions) {
					exception.addSuppressed(ex.getValue());
				}
			}
			return exception;
		}

		private void newConnection() {
			int index = tried.getAndIncrement();
			if (index >= addresses.size() || result.isDone()) {
				task.cancel(false);
				return;
			}
			connecting.incrementAndGet();
			LOG.log(Level.FINE, "Connecting to {0}", new Object[]{
				addresses.get(index)});
			ChannelFuture channel = bootstrap.connect(addresses.get(index));
			GenericFutureListener<Future<Channel>> mainListener = future -> {
				channel.cancel(true);
			};
			GenericFutureListener<Future<Void>> subListener = future -> {
				result.removeListener(mainListener);
				if (channel.isSuccess()) {
					LOG.log(Level.FINE, "Connection finished to {0}",
							channel.channel().remoteAddress());
					if (!result.trySuccess(channel.channel())) {
						LOG.log(Level.FINER, "{0}: Another channel finished "
								+ "the connection before we did: "
								+ "{1} was later than {2}",
								new Object[]{
									taskId, channel.channel(), result.get()});
						channel.channel().close();
					}
				} else {
					final Throwable cause = future.cause();
					exceptions.add(new Pair<>(addresses.get(index), cause));
					int totalFailed = failed.incrementAndGet();
					LOG.log(Level.FINE, "We failed {0}", new Object[]{cause});
					if (totalFailed == addresses.size()) {
						// All connection attemps failed, and we were the last to fail
						result.setFailure(createException());
						return;
					}
					int concurrentAttempts = connecting.decrementAndGet();
					if (concurrentAttempts == 0) {
						LOG.log(Level.INFO, "{0}: Forcing new connection "
								+ "attempt because no pending connections",
								taskId);
						newConnection();
					}
				}
			};
			result.addListener(mainListener);
			channel.addListener(subListener);
		}

		@Nonnull
		public Promise<Channel> start() {
			task = group.next().scheduleAtFixedRate(this::newConnection,
					0, 60, TimeUnit.MILLISECONDS);
			result.addListener(f -> {
				task.cancel(false);
			});
			return result;
		}
	}

}
