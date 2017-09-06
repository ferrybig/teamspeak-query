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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author Fernando van Loenhout
 */
public class TeamspeakBootstrapTest {

	private static NioEventLoopGroup loop;
	private static NioEventLoopGroup loop2;
	private static SocketAddress timeout;
	private static SocketAddress refused;
	private static SocketAddress success;
	private static ChannelFuture server;
	private static Bootstrap bootstrap;

	@BeforeClass
	public static void beforeClass() throws UnknownHostException, InterruptedException {
		timeout = new InetSocketAddress(InetAddress.getByName("10.255.255.255"), 65535);
		refused = new InetSocketAddress(InetAddress.getLoopbackAddress(), 65535);
		loop = new NioEventLoopGroup(4);
		loop2 = new NioEventLoopGroup(1);
		ServerBootstrap bs = new ServerBootstrap();
		bs.channel(NioServerSocketChannel.class);
		bs.group(loop);
		bs.childHandler(new SharedHandler());
		server = bs.bind(InetAddress.getLoopbackAddress(), 0);
		server.sync();
		success = ((ServerSocketChannel) server.sync().channel()).localAddress();
		bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(loop);
		bootstrap.handler(new SharedHandler());
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
	}

	@AfterClass
	public static void afterClass() {
		if (server != null && server.isSuccess()) {
			server.channel().close();
		}
		if (loop != null) {
			loop.shutdownGracefully();
			loop = null;
		}
		if (loop2 != null) {
			loop2.shutdownGracefully();
			loop2 = null;
		}
	}

	@Test
	public void testGroupConstructor() {
		TeamspeakBootstrap ts = new TeamspeakBootstrap(loop);

		assertThat(ts.group(), is(loop));
	}

	@Test
	public void testGroupSet() {
		TeamspeakBootstrap ts = new TeamspeakBootstrap(loop);

		assumeThat(ts.group(), is(loop));

		ts.group(loop2);

		assertThat(ts.group(), is(loop2));
	}

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("null")
	public void testGroupSetNPE() {
		TeamspeakBootstrap ts = new TeamspeakBootstrap(loop);

		ts.group(null);
	}

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void testGroupConstructorNPE() {
		new TeamspeakBootstrap(null);
	}

	@Test
	public void usernameUnsetInitialTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertThat(tb.username(), nullValue());
	}

	@Test
	public void passwordUnsetInitialTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertThat(tb.password(), nullValue());
	}

	@Test
	public void usernameWillBeUnsetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.username(), nullValue());

		tb.login("bar", "baz");

		assumeThat(tb.username(), is("bar"));

		tb.noLogin();

		assertThat(tb.username(), nullValue());
	}

	@Test
	public void passwordWillBeUnsetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.password(), nullValue());

		tb.login("bar", "baz");

		assumeThat(tb.password(), is("baz"));

		tb.noLogin();

		assertThat(tb.password(), nullValue());
	}

	@Test
	public void usernameSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.username(), nullValue());

		tb.login("test", "");

		assertThat(tb.username(), is("test"));
	}

	@Test
	public void passwordSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.password(), nullValue());

		tb.login("test", "bar");

		assertThat(tb.password(), is("bar"));
	}

	@Test
	public void clientNameUnsetInitialTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertThat(tb.clientName(), nullValue());
	}

	@Test
	public void clientNameCanBeSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.clientName(), nullValue());

		tb.clientName("foo");

		assertThat(tb.clientName(), is("foo"));
	}

	@Test
	public void clientNameCanBeReplacedTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.clientName(), nullValue());

		tb.clientName("foo");

		assumeThat(tb.clientName(), is("foo"));

		tb.clientName("baz");

		assertThat(tb.clientName(), is("baz"));
	}

	@Test
	public void clientNameCanBeUnsetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assumeThat(tb.clientName(), nullValue());

		tb.clientName("foo");

		assumeThat(tb.clientName(), is("foo"));

		tb.noClientName();

		assertThat(tb.clientName(), nullValue());
	}

	@Test
	public void happyEyeballsCanConnectToSingleEndpoint() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, Collections.singletonList(success));
			ts.sync();

			assertThat(ts.cause(), nullValue());
			assertThat(ts.get(), notNullValue());
			assertThat(((SocketChannel) ts.get()).remoteAddress(), is(success));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test
	public void happyEyeballsFailsInvalidEndpoint() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, Collections.singletonList(refused));
			ts.await();

			assertThat(ts.isSuccess(), is(false));
			assertThat(ts.cause(), notNullValue());
			assertThat(ts.cause(), instanceOf(IOException.class));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test
	public void happyEyeballsCanConnectCorrectSocketIsReturned() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, Arrays.asList(success, refused));
			ts.sync();

			assertThat(ts.cause(), nullValue());
			assertThat(ts.get(), notNullValue());
			assertThat(((SocketChannel) ts.get()).remoteAddress(), is(success));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test
	public void happyEyeballsCanConnectCorrectSocketIsReturnedReverseOrder() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, Arrays.asList(refused, success));
			ts.sync();

			assertThat(ts.cause(), nullValue());
			assertThat(ts.get(), notNullValue());
			assertThat(((SocketChannel) ts.get()).remoteAddress(), is(success));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("null")
	public void happyEyeballsHandlesNullArgument() throws UnknownHostException, InterruptedException, ExecutionException {
		TeamspeakBootstrap.happyEyeballs(bootstrap, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void happyEyeballsHandlesNullArgumentInList() throws UnknownHostException, InterruptedException, ExecutionException {
		TeamspeakBootstrap.happyEyeballs(bootstrap, Collections.singletonList(null));
	}

	@Test
	public void happyEyeballsCanConnectCorrectSocketIsReturnedMany() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			List<SocketAddress> list = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				list.add(refused);
			}
			list.add(success);

			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, list);
			ts.sync();

			assertThat(ts.cause(), nullValue());
			assertThat(ts.get(), notNullValue());
			assertThat(((SocketChannel) ts.get()).remoteAddress(), is(success));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test(timeout = 1000)
	public void happyEyeballsCanConnectCorrectSocketIsReturnedManyTimeout() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			List<SocketAddress> list = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				list.add(timeout);
			}
			list.add(success);

			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, list);
			ts.sync();

			assertThat(ts.cause(), nullValue());
			assertThat(ts.get(), notNullValue());
			assertThat(((SocketChannel) ts.get()).remoteAddress(), is(success));
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test(timeout = 5000)
	public void happyEyeballsPerfornmsParalelization() throws UnknownHostException, InterruptedException, ExecutionException {
		Future<Channel> ts = null;
		try {
			List<SocketAddress> list = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				list.add(timeout);
			}

			ts = TeamspeakBootstrap.happyEyeballs(bootstrap, list);
			ts.await();

			assertThat(ts.isSuccess(), is(false));
			assertThat(ts.cause(), notNullValue());
		} finally {
			if (ts != null && ts.isSuccess()) {
				ts.get().close();
			}
		}
	}

	@Test
	public void defaultRateLimitIsAutoDetectTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertEquals(RateLimit.AUTODETECT, tb.rateLimit());
	}

	@Test
	public void rateLimitCanBeSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.rateLimit(RateLimit.UNLIMITED);
		assertEquals(RateLimit.UNLIMITED, tb.rateLimit());

		tb.rateLimit(RateLimit.LIMITED);
		assertEquals(RateLimit.LIMITED, tb.rateLimit());
	}

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("null")
	public void rateLimitDoesNotAcceptNullTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.rateLimit(null);
	}

	@Test
	public void rateLimitReturnsSameInstanceTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		TeamspeakBootstrap tb1 = tb.rateLimit(RateLimit.UNLIMITED);

		assertSame(tb, tb1);
	}

	@Test
	public void selectedServerDefaultIsNullTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertNull(tb.selectServerID());
	}

	@Test
	public void selectServerIdCanBeSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.selectServerID(1);
		assertEquals((Object)1, tb.selectServerID());

		tb.selectServerID(3);
		assertEquals((Object)3, tb.selectServerID());
	}

	@Test
	public void selectServerIdCanBeUnsetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.selectServerID(1);
		assertEquals((Object)1, tb.selectServerID());

		tb.noSelectServerID();
		assertEquals(null, tb.selectServerID());
	}

	@Test
	public void selectServerIdReturnsSameInstanceTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		TeamspeakBootstrap tb1 = tb.selectServerID(1);

		assertSame(tb, tb1);
	}

	@Test
	public void selectedServerPortDefaultIsNullTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		assertNull(tb.selectServerPort());
	}

	@Test
	public void selectServerPortCanBeSetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.selectServerPort(1);
		assertEquals((Object)1, tb.selectServerPort());

		tb.selectServerPort(3);
		assertEquals((Object)3, tb.selectServerPort());
	}

	@Test
	public void selectServerPortCanBeUnsetTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		tb.selectServerPort(1);
		assertEquals((Object)1, tb.selectServerPort());

		tb.noSelectServerPort();
		assertEquals(null, tb.selectServerPort());
	}

	@Test
	public void selectServerPortReturnsSameInstanceTest() {
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);

		TeamspeakBootstrap tb1 = tb.selectServerPort(1);

		assertSame(tb, tb1);
	}

	@Test
	public void decorateConnectionCallsLoginWhenLoginIsPresent() throws InterruptedException, ExecutionException {
		TeamspeakIO io = mock(TeamspeakIO.class);
		TeamspeakConnection con = new TeamspeakConnection(io);
		Future<TeamspeakConnection> future = loop.next().newSucceededFuture(con);
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);
		doReturn(loop.next().newSucceededFuture(new ComplexResponse(new ArrayList<>(), 0, "", ""))).when(io).sendPacket(any(ComplexRequest.class));
		doAnswer(new Answer<Future<?>>() {
			@Override
			@SuppressWarnings("unchecked")
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				return FutureUtil.chainFuture(
						loop.next().newPromise(),
						(Future<Object>)invocation.getArgumentAt(0, Object.class),
						(Function<Object, Object>)invocation.getArgumentAt(1, Object.class));
			}
		}).when(io).chainFuture(any(), any());

		Future<TeamspeakConnection> newFuture = tb.login("foo", "bar").decorateConnection(loop.next(), future);

		try {
			newFuture.get(1000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {}

		verify(io).sendPacket(new ComplexRequestBuilder(Command.LOG_IN).addData("client_login_name", "foo").addData("client_login_password", "bar").build());
		assertNotEquals(future, newFuture);
		assertSame(future.get(), newFuture.get());
		assertTrue(newFuture.isDone());
	}

	@Test
	public void decorateConnectionCallsWontCallLogin() {
		TeamspeakIO io = mock(TeamspeakIO.class);
		TeamspeakConnection con = new TeamspeakConnection(io);
		Future<TeamspeakConnection> future = loop.next().newSucceededFuture(con);
		TeamspeakBootstrap tb = new TeamspeakBootstrap(loop);
		doReturn(loop.next().newSucceededFuture(new ComplexResponse(new ArrayList<>(), 0, "", ""))).when(io).sendPacket(any(ComplexRequest.class));
		doAnswer(new Answer<Future<?>>() {
			@Override
			@SuppressWarnings("unchecked")
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				return FutureUtil.chainFuture(
						loop.next().newPromise(),
						(Future<Object>)invocation.getArgumentAt(0, Object.class),
						(Function<Object, Object>)invocation.getArgumentAt(1, Object.class));
			}
		}).when(io).chainFuture(any(), any());

		Future<TeamspeakConnection> newFuture = tb.decorateConnection(loop.next(), future);

		assertSame(future, newFuture);
		assertTrue(newFuture.isDone());
	}

	@ChannelHandler.Sharable
	private static class SharedHandler extends ChannelHandlerAdapter {

	}
}
