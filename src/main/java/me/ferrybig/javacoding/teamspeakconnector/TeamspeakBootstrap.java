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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
 * });
 * }
 * </pre>
 */
public class TeamspeakBootstrap {

	public static final int DEFAULT_QUERY_PORT = 10011;
	private static final Logger LOG = Logger.getLogger(TeamspeakBootstrap.class.getName());

	private String username;
	private String password;
	private Integer virtualServerId;
	private Integer virtualServerPort;
	private String clientName;
	private EventLoopGroup group;

	@SuppressWarnings("OverridableMethodCallInConstructor")
	public TeamspeakBootstrap(EventLoopGroup group) {
		group(group);
	}

	/**
	 * Returns the configured username, or null when no username is defined
	 * @return the username or null
	 */
	public String username() {
		return this.username;
	}

	/**
	 * Returns the configured password, or null when no password is defined
	 * @return the password or null
	 */
	public String password() {
		return this.password;
	}

	/**
	 * Sets the username and password
	 * @param username Username used to login
	 * @param password Password used to login
	 * @return this
	 */
	public TeamspeakBootstrap login(String username, String password) {
		this.username = Objects.requireNonNull(username, "username");
		this.password = Objects.requireNonNull(password, "password");
		return this;
	}

	/**
	 * Removes any configured username and password login credentials
	 * @return this
	 */
	public TeamspeakBootstrap noLogin() {
		this.username = null;
		this.password = null;
		return this;
	}

	/**
	 * Returns the selected server id, or null if none is selected
	 * @return server id
	 */
	public Integer selectServerID() {
		return this.virtualServerId;
	}

	/**
	 * Configure a server id to select after connecting
	 * @param serverId server id to connect to
	 * @return
	 */
	public TeamspeakBootstrap selectServerID(int serverId) {
		this.virtualServerId = serverId;
		return this;
	}

	/**
	 * Removes the selection of the server
	 * @return this
	 */
	public TeamspeakBootstrap noSelectServerID() {
		this.virtualServerId = null;
		return this;
	}

	/**
	 * Returns the selected server by port, or null if not defined
	 * @return port selected server
	 */
	public Integer selectServerPort() {
		return this.virtualServerPort;
	}

	/**
	 * Select the final server by port
	 * @param port port number of the virtual server
	 * @return this
	 */
	public TeamspeakBootstrap selectServerPort(int port) {
		this.virtualServerPort = port;
		return this;
	}

	/**
	 * Clears the selection of selected virtual server by port
	 * @return this
	 */
	public TeamspeakBootstrap noSelectServerPort() {
		this.virtualServerPort = null;
		return this;
	}

	/**
	 * Returns the selected clientname
	 * @return the clientname
	 */
	public String clientName() {
		return this.clientName;
	}

	/**
	 * Select a clientname that will be used after connecting, if this name is
	 * already in use, the connection fails
	 * @param clientName
	 * @return this
	 */
	public TeamspeakBootstrap clientName(String clientName) {
		this.clientName = clientName;
		return this;
	}

	/**
	 * Clears the selected clientname
	 * @return this
	 */
	public TeamspeakBootstrap noClientName() {
		this.clientName = null;
		return this;
	}

	/**
	 * Configures the group that is used in the connection
	 * @param group the group
	 * @return this
	 */
	public TeamspeakBootstrap group(EventLoopGroup group) {
		this.group = Objects.requireNonNull(group, "group");
		return this;
	}

	/**
	 * Internal method that generates the Netty bootstrap. This method can be
	 * overriden for a custom bootstrap, it is recommended that this methods
	 * calls the parent, and then adds more properties and options as needed.
	 * @param group the selected group
	 * @param init the initial ChannelHandler that should be added to the channel
	 * @return the generated netty bootstrap.
	 */
	protected Bootstrap generateBootstrap(EventLoopGroup group, ChannelInitializer<SocketChannel> init) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(init);
		bootstrap.group(group);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.TCP_NODELAY, true); // We send relatively small packets, we benefit more from delayed ack
		return bootstrap;
	}

	/**
	 * Happy eyeballs algorithm used to connect to a remote server in a
	 * seamless way when it has multiple ip addresses.
	 * @param bootstrap The generated bootstrap in the previous step
	 * @param addresses addresses to connect to, the best ones first
	 * @return a future giving the new connected socket.
	 */
	protected static Future<SocketChannel> happyEyeballs(
			Bootstrap bootstrap,
			List<SocketAddress> addresses) {
		return new ConnectionAttempt(bootstrap.group().next().newPromise(), addresses, bootstrap.group(), bootstrap).start();
	}

	/**
	 * Connect to a named domain name or ip address, any exceptions in the
	 * resolving of the name are propagated back using the future.
	 * @param endpoint domain name or ip address
	 * @param port port used
	 * @return the future connection
	 */
	public Future<TeamspeakConnection> connect(String endpoint, int port) {
		try {
			return connect(Arrays.stream(InetAddress.getAllByName(endpoint))
					.map(ip -> new InetSocketAddress(ip, port))
					.collect(Collectors.toList()));
		} catch (UnknownHostException ex) {
			return this.group.next().newFailedFuture(ex);
		}
	}

	/**
	 * Connect using a single ip address and port as target
	 * @param endpoint ip and port pair to connect to
	 * @return the future connection
	 */
	public Future<TeamspeakConnection> connect(SocketAddress endpoint) {
		return connect(Collections.singletonList(endpoint));
	}

	/**
	 * Connect to any of the passed endpoints
	 * @param endpoints a list of ip and port pairs
	 * @return the future connection
	 */
	public Future<TeamspeakConnection> connect(List<SocketAddress> endpoints) {
		if (this.group == null) {
			throw new IllegalStateException("Group is not defined");
		}
		final EventLoop next = this.group.next();
		if (endpoints.isEmpty()) {
			return next.newFailedFuture(new IllegalArgumentException("No addresses specified"));
		}
		Bootstrap bootstrap = this.generateBootstrap(group, new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new PacketQueueBuffer());
			}
		});
		Future<SocketChannel> channel = happyEyeballs(bootstrap, endpoints);
		Promise<TeamspeakConnection> connection = next.newPromise();
		channel.addListener(f -> {
			if (f.isSuccess()) {
				SocketChannel ch = channel.get();
				LOG.log(Level.INFO, "Connecting done! {0}", ch);
				ch.pipeline().get(PacketQueueBuffer.class).replace(new TeamspeakConnectionInitizer(connection, 20000));
			} else {
				connection.setFailure(new TeamspeakException("TSConnect: " + f.cause().getMessage(), f.cause()));
			}
		});
		Future<TeamspeakConnection> con = decorateConnection(next, connection);
		con.addListener(f -> {
			if (!f.isSuccess()) {
				if (connection.isSuccess()) {
					connection.get().quit();
				} else if(channel.isSuccess()) {
					channel.get().close();
				}
			}
		});
		return con;
	}

	private Future<TeamspeakConnection> decorateConnection(EventLoop next, Future<TeamspeakConnection> connection) {
		if (username != null) {
			connection = FutureUtil.chainFutureFlat(next.newPromise(), connection, con -> con.login(username, password));
		}
		if (virtualServerId != null) {
			connection = FutureUtil.chainFutureFlat(next.newPromise(), connection, con -> con.getUnresolvedServerById(virtualServerId).select());
		}
		if (virtualServerPort != null) {
			connection = FutureUtil.chainFutureFlat(next.newPromise(), connection, con -> con.getUnresolvedServerByPort(virtualServerPort).select());
		}
		if (clientName != null) {
			connection = FutureUtil.chainFutureFlat(next.newPromise(), connection, con -> con.setOwnName(clientName), (t, i) -> t);
		}
		return connection;
	}

	private static class ConnectionAttempt {

		private final static AtomicInteger TASK_ID_LIST = new AtomicInteger();
		private final int taskId = TASK_ID_LIST.getAndIncrement();
		private final Promise<SocketChannel> result;
		private final List<SocketAddress> addresses;
		private final EventLoopGroup group;
		private final Bootstrap bootstrap;
		private final AtomicInteger connecting = new AtomicInteger();
		private final AtomicInteger tried = new AtomicInteger();
		private final AtomicInteger failed = new AtomicInteger();
		private ScheduledFuture<?> task;
		private final CopyOnWriteArrayList<Throwable> exceptions = new CopyOnWriteArrayList<>();

		public ConnectionAttempt(Promise<SocketChannel> result, List<SocketAddress> addresses, EventLoopGroup group, Bootstrap bootstrap) {
			this.result = result;
			this.addresses = addresses;
			this.group = group;
			this.bootstrap = bootstrap;
		}

		private Throwable createException() {
			Iterator<Throwable> itr = exceptions.iterator();
			assert itr.hasNext();
			IOException ex = new IOException("Unable to connect");
			ex.initCause(itr.next());
			while(itr.hasNext()) {
				ex.addSuppressed(itr.next());
			}
			return ex;
		}

		private void newConnection() {
			int index = tried.getAndIncrement();
			if (index >= addresses.size() || result.isDone()) {
				task.cancel(false);
				return;
			}
			connecting.incrementAndGet();
			LOG.log(Level.INFO, "Connecting to {0}", new Object[]{addresses.get(index)});
			ChannelFuture channel = bootstrap.connect(addresses.get(index));
			GenericFutureListener<Future<? super SocketChannel>> mainListener = (Future<? super SocketChannel> future) -> {
				channel.cancel(true);
			};
			GenericFutureListener<Future<Void>> subListener = ((Future<Void> future) -> {
				result.removeListener(mainListener);
				if (channel.isSuccess()) {
					LOG.log(Level.INFO, "Connection finished to {0}", channel.channel().remoteAddress());
					if (!result.trySuccess((SocketChannel) channel.channel())) {
						LOG.log(Level.INFO, "{0}: Another channel finished the connection before we did: {1} was later than {2}", new Object[]{taskId, channel.channel(), result.get()});
						channel.channel().close();
					}
				} else {
					final Throwable cause = future.cause();
					exceptions.add(cause);
					int totalFailed = failed.incrementAndGet();
					LOG.log(Level.INFO, "We failed {0}", new Object[]{cause});
					if (totalFailed == addresses.size()) {
						// All connection attemps failed, and we were the last to fail
						result.setFailure(createException());
						return;
					}
					int concurrentAttempts = connecting.decrementAndGet();
					if (concurrentAttempts == 0) {
						LOG.log(Level.INFO, "{0}: Forcing new connection attempt because no pending connections", taskId);
						newConnection();
					}
				}
			});
			result.addListener(mainListener);
			channel.addListener(subListener);
		}

		public Promise<SocketChannel> start() {
			task = group.next().scheduleAtFixedRate(this::newConnection, 0, 60, TimeUnit.MILLISECONDS);
			result.addListener(f -> {
				task.cancel(false);
			});
			return result;
		}
	}

}
