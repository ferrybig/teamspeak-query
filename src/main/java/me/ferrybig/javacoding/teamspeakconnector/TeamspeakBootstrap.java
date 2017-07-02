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
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakConnectionInitizer;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketQueueBuffer;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

public class TeamspeakBootstrap {

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

	public String username() {
		return this.username;
	}

	public String password() {
		return this.password;
	}

	public TeamspeakBootstrap login(String username, String password) {
		this.username = Objects.requireNonNull(username, "username");
		this.password = Objects.requireNonNull(password, "password");
		return this;
	}

	public TeamspeakBootstrap noLogin() {
		this.username = null;
		this.password = null;
		return this;
	}

	public Integer selectServerID() {
		return this.virtualServerId;
	}

	public TeamspeakBootstrap selectServerID(int serverId) {
		this.virtualServerId = serverId;
		return this;
	}

	public TeamspeakBootstrap noSelectServerID() {
		this.virtualServerId = null;
		return this;
	}

	public Integer selectServerPort() {
		return this.virtualServerPort;
	}

	public TeamspeakBootstrap selectServerPort(int port) {
		this.virtualServerPort = port;
		return this;
	}

	public TeamspeakBootstrap noSelectServerPort() {
		this.virtualServerPort = null;
		return this;
	}

	public String clientName() {
		return this.clientName;
	}

	public TeamspeakBootstrap clientName(String clientName) {
		this.clientName = clientName;
		return this;
	}

	public TeamspeakBootstrap noClientName() {
		this.clientName = null;
		return this;
	}

	public TeamspeakBootstrap group(EventLoopGroup group) {
		this.group = Objects.requireNonNull(group, "group");
		return this;
	}

	protected Bootstrap generateBootstrap(EventLoopGroup group, ChannelInitializer<SocketChannel> init) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(init);
		bootstrap.group(group);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.TCP_NODELAY, true); // We send relatively small packets, we benefit more from delayed ack
		return bootstrap;
	}

	protected static Future<SocketChannel> happyEyeballs(
			Bootstrap bootstrap,
			List<SocketAddress> addresses) {
		return new ConnectionAttempt(bootstrap.group().next().newPromise(), addresses, bootstrap.group(), bootstrap).start();
	}

	public Future<TeamspeakConnection> connect(String endpoint, int port) {
		try {
			return connect(Arrays.stream(InetAddress.getAllByName(endpoint))
					.map(ip -> new InetSocketAddress(ip, port))
					.collect(Collectors.toList()));
		} catch (UnknownHostException ex) {
			return this.group.next().newFailedFuture(ex);
		}
	}

	public Future<TeamspeakConnection> connect(SocketAddress endpoint) {
		return connect(Collections.singletonList(endpoint));
	}

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
				} else {
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
			connection = FutureUtil.chainFutureFlat(next.newPromise(), connection, con -> con.setOwnName("TestingBot"), (t, i) -> t);
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
			assert !exceptions.isEmpty();
			if (exceptions.size() == 1) {
				return exceptions.get(0);
			}
			return exceptions.get(0); // TODO
		}

		private void newConnection() {
			connecting.incrementAndGet();
			int index = tried.incrementAndGet();
			if (index > addresses.size() || result.isDone()) {
				task.cancel(false);
				return;
			}
			ChannelFuture channel = bootstrap.connect(addresses.get(index));
			GenericFutureListener<Future<? super SocketChannel>> mainListener = (Future<? super SocketChannel> future) -> {
				channel.cancel(true);
			};
			GenericFutureListener<Future<Void>> subListener = ((Future<Void> future) -> {
				result.removeListener(mainListener);
				if (channel.isSuccess()) {
					if (!result.trySuccess((SocketChannel) channel.channel())) {
						LOG.log(Level.FINE, "{0}: Another channel finished the connection before we did: {1} was later than {2}", new Object[]{taskId, channel.channel(), result.get()});
						channel.channel().close();
					}
				} else {
					final Throwable cause = future.cause();
					exceptions.add(cause);
					int totalFailed = failed.incrementAndGet();
					if (totalFailed == addresses.size()) {
						// All connection attemps failed, and we were the last to fail
						result.setFailure(createException());
						return;
					}
					int concurrentAttempts = connecting.decrementAndGet();
					if (concurrentAttempts == 0) {
						LOG.log(Level.FINE, "{0}: Forcing new connection attempt because no pending connections", taskId);
						newConnection();
					}
				}
			});
			result.addListener(mainListener);
			channel.addListener(subListener);
		}

		public Promise<SocketChannel> start() {
			task = group.next().schedule(this::newConnection, 50, TimeUnit.MILLISECONDS);
			result.addListener(f -> {
				task.cancel(false);
			});
			this.newConnection();
			return result;
		}
	}

}
