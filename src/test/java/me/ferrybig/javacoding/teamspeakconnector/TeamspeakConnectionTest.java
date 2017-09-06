/*
 * The MIT License
 *
 * Copyright 2017 Fernando.
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

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.util.ArrayList;
import java.util.function.Function;
import me.ferrybig.javacoding.teamspeakconnector.internal.Mapper;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.repository.ChannelRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.OfflineClientRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.OnlineClientRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.PrivilegeKeyRepository;
import me.ferrybig.javacoding.teamspeakconnector.repository.ServerRepository;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TeamspeakConnectionTest {

	private TeamspeakConnection con;
	private TeamspeakIO io;
	private static NioEventLoopGroup loop;

	@BeforeClass
	public static void beforeClass() {
		loop = new NioEventLoopGroup(1);
	}

	@AfterClass
	public static void afterClass() {
		loop.shutdownGracefully();
	}

	@Before
	public void before() {
		io = mock(TeamspeakIO.class);
		con = new TeamspeakConnection(io);
		doReturn(loop.next().newSucceededFuture(new ComplexResponse(new ArrayList<>(), 0, "", ""))).when(io).sendPacket(any(ComplexRequest.class));
		doReturn(loop.next().newSucceededFuture(new ComplexResponse(new ArrayList<>(), 0, "", ""))).when(io).sendPacket(any(ComplexRequest.class), any());
		doAnswer(new Answer<Future<?>>() {
			@Override
			@SuppressWarnings("unchecked")
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				return FutureUtil.chainFuture(
						loop.next().newPromise(),
						(Future<Object>) invocation.getArgumentAt(0, Object.class),
						(Function<Object, Object>) invocation.getArgumentAt(1, Object.class));
			}
		}).when(io).chainFuture(any(), any());
	}

	@Test
	public void testLogin() {
		con.login("bar", "foo");

		verify(io).sendPacket(new ComplexRequestBuilder(Command.LOG_IN).addData("client_login_name", "bar").addData("client_login_password", "foo").build());
	}

	@Test
	public void testLogout() {
		con.logout();

		verify(io).sendPacket(new ComplexRequestBuilder(Command.LOG_OUT).build());
	}

	@Test
	public void testIO() {
		assertSame(io, con.io());
	}

	@Test(expected = NullPointerException.class)
	public void cannotConstructWithNullArgumentTest() {
		con = new TeamspeakConnection(null);
	}

	@Test
	public void channelsTest() {
		final ChannelRepository channels = con.channels();
		assertNotNull(channels);
		assertSame(con, channels.getConnection());
	}

	@Test
	public void serversTest() {
		final ServerRepository servers = con.servers();
		assertNotNull(servers);
		assertSame(con, servers.getConnection());
	}

	@Test
	public void onlineClientTest() {
		final OnlineClientRepository onlineClients = con.onlineClients();
		assertNotNull(onlineClients);
		assertSame(con, onlineClients.getConnection());
	}

	@Test
	public void offlineClientTest() {
		final OfflineClientRepository offlineClients = con.offlineClients();
		assertNotNull(offlineClients);
		assertSame(con, offlineClients.getConnection());
	}

	@Test
	public void privilegeKeyTest() {
		final PrivilegeKeyRepository privilegeKeys = con.privilegeKeys();
		assertNotNull(privilegeKeys);
		assertSame(con, privilegeKeys.getConnection());
	}

	@Test
	public void mapperTest() {
		final Mapper mapping = con.mapping();
		assertNotNull(mapping);
	}

	@Test
	public void selfInformationTest() {
		final SelfInformation mapping = con.self();
		assertNotNull(mapping);
	}

}
