/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.User;
import static me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil.waitSync;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Assume;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;

/**
 *
 *
 */
public class TeamspeakConnectionIT {

	private static final Logger LOG = Logger.getLogger(TeamspeakConnectionIT.class.getName());

	private static final NioEventLoopGroup group = new NioEventLoopGroup();

	@AfterClass
	public static void afterClass() {
		group.shutdownGracefully();
	}

	private TeamspeakBootstrap creatBootstrap() {
		TeamspeakBootstrap ts = new TeamspeakBootstrap(group);

		String username = System.getProperty("teamspesk3.username", "");
		if (username.isEmpty()) {
			username = "serveradmin";
		}
		String password = System.getProperty("teamspeak3.password", "");
		if (password.isEmpty()) {
			password = "test1234";
		}
		ts.login(username, password);
		ts.selectServerID(1);
		ts.clientName("TestingBot");
		return ts;
	}

	private Future<TeamspeakConnection> createConnection() {
		return createConnection("TestingBot");
	}

	private Future<TeamspeakConnection> createConnection(String clientname) {
		String hostname = System.getProperty("teamspeak3.hostname", "");
		if (hostname.isEmpty()) {
			hostname = "localhost";
		}
		int port = Integer.parseInt(System.getProperty("teamspeak3.port", "").isEmpty()
				? String.valueOf(TeamspeakBootstrap.DEFAULT_QUERY_PORT)
				: System.getProperty("teamspeak3.port"));
		return creatBootstrap().clientName(clientname).connect(hostname, port);
	}

	private void assumeConnectionWorking(Future<TeamspeakConnection>... cons)
			throws InterruptedException {
		if (Boolean.valueOf(System.getProperty("teamspeak3.required"))) {
			return;
		}
		for (Future<TeamspeakConnection> con : cons) {
			con.await();
			if (!con.isSuccess()) {
				if (con.cause().getCause() instanceof ConnectException) {
					Assume.assumeFalse("Cannot connect to teamspeak server, "
							+ "check the maven vales teamspeak3.hostname, "
							+ "teamspeak3.port, teamspeak3.username, "
							+ "teamspeak3.password", true);
				}
			}
		}
	}

	@Test
	public void testSomeMethod() throws InterruptedException, ExecutionException {

		System.out.println("Connecting...");
		Future<TeamspeakConnection> connect = createConnection();
		assumeConnectionWorking(connect);
		TeamspeakConnection con = connect.sync().get();

		System.out.println("Connected!");

		System.out.println("Channel list");
		final List<Channel> channel = con.getChannelList().sync().get();
		channel.forEach(System.out::println);

		System.out.println("Group list");
		final List<Group> groups = con.groups().list().get();
		groups.forEach(System.out::println);

		Optional<Group> bottest = groups.stream().filter(g -> (g.getType() == Group.Type.REGULAR) && g.getName().equals("BotTest")).findAny();

		System.out.println("User list!");
		List<User> users = con.getUsersList().sync().get();
		for (User user : users) {
			if (user.getType() == User.Type.QUERY) {
				continue;
			}
			if (bottest.isPresent()) {
				user.addToGroup(bottest.get()).get();
			}
			//user.poke("Hello: " + user.getNickname() + ", Your ip address: " + user.getIp());
			user.sendMessage("Hello: " + user.getNickname()
					+ ", Your ip address: " + user.getIp()
					+ ", and your in the following groups: " + user.getServerGroup().stream().map(Object::toString).collect(Collectors.joining()));
		}

//			System.out.println("Waiting for eserver events!");
//			con.getServerHandler().addHandler(new ServerListener() {
//				@Override
//				public void onClientEnterView(ClientEnterViewEvent event) {
//					LOG.log(Level.INFO, "ClientEnterViewEvent: {0}", event);
//					event.getClient().sendMessage("Hello " + event.getClient().getNickname() + "!");
//					event.getClient().getChannel().resolv().addListener((Future<Channel> channel) -> {
//						event.getClient().sendMessage("Welcome to channel " + channel.get().getName() + "!");
//					});
//				}
//
//				@Override
//				public void onClientLeaveView(ClientLeftViewEvent event) {
//					LOG.log(Level.INFO, "ClientLeftViewEvent: {0}", event);
//				}
//
//				@Override
//				public void onEditServer(ServerEditEvent event) {
//					LOG.log(Level.INFO, "ServerEditEvent: {0}", event);
//				}
//
//			});
		if (bottest.isPresent()) {
			for (User user : users) {
				user.removeFromGroup(bottest.get()).get();
			}
		}

		System.out.println("Closing...!");
		con.quit().sync().get();
	}

	@Test
	public void messagesAreSendAndReceived() throws InterruptedException, ExecutionException {
		System.out.println("Creating!");

		Future<TeamspeakConnection> connect1 = createConnection("TestingBot1");
		Future<TeamspeakConnection> connect2 = createConnection("TestingBot2");

		assumeConnectionWorking(connect1, connect2);

		TeamspeakConnection con1 = connect1.get();
		TeamspeakConnection con2 = connect2.get();

		System.out.println("User list!");
		User con1user2 = null;
		List<User> users = con1.getUsersList().sync().get();
		assertNotEquals(con1.io().whoAmI().get().getId(), con2.io().whoAmI().get().getId());
		for (User user : users) {
			if (user.getType() == User.Type.QUERY) {
				System.err.println(user.getId() + ":" + con2.io().whoAmI().get().getId());
				if (user.getId() == con2.io().whoAmI().get().getId()) {
					con1user2 = user;
				}
			}
		}
		if (con1user2 == null) {
			fail("Connection 2 failed, while its connection attempt didn't throw an exception.");
			assert false : "Should not reach here";
		}

		AtomicInteger received = new AtomicInteger(0);
		AtomicInteger bounced = new AtomicInteger(0);

		waitSync(
				con1.getPrivateMessageHandler().addHandler(event -> {
					received.getAndIncrement();
				}),
				con2.getPrivateMessageHandler().addHandler(event -> {
					bounced.getAndIncrement();
					event.getInvoker().sendMessage(event.getMessage());
				})
		);

		System.out.println("Sending messages...");
		List<Future<?>> messages = new ArrayList<>();
		for (int send = 0; send < 5; send++) {
			messages.add(con1user2.sendMessage("Hello " + send));
		}
		waitSync(messages);
		System.out.println("Messages ready");

		Thread.sleep(1000);

		assertEquals(messages.size(), bounced.get());
		assertEquals(messages.size(), received.get());

		waitSync(con1.quit(), con2.quit());
	}

	@Test(timeout = 10000)
	public void channelForceCloseTest() throws InterruptedException, ExecutionException {

		Future<TeamspeakConnection> connect = createConnection();
		assumeConnectionWorking(connect);
		TeamspeakConnection con = connect.get();

		Future<?> namechange1 = con.setOwnName("Test1").await();
		assertTrue(namechange1.toString(), namechange1.isSuccess());

		con.io().getChannel().close().get();

		Future<?> namechange2 = con.setOwnName("Test2").await();
		assertFalse(namechange2.toString(), namechange2.isSuccess());

		Future<?> quit = con.quit().await();
		assertTrue(quit.toString(), quit.isSuccess());
	}

	@Test
	public void privilegeTokensCanBeCreatedAndDeleted() throws InterruptedException, ExecutionException {
		System.out.println("Connecting...");
		Future<TeamspeakConnection> connect = createConnection();
		assumeConnectionWorking(connect);
		TeamspeakConnection con = connect.sync().get();

		final List<Group> groups = con.groups().list().get();

		Optional<Group> bottest = groups.stream().filter(g -> (g.getType() == Group.Type.REGULAR) && g.getName().equals("BotTest")).findAny();

		assumeTrue("No group with the name 'BotTest' found", bottest.isPresent());

		final PrivilegeKey privilegeKey = bottest.get().generatePrivilegeKey().get();
		privilegeKey.delete().sync();
		Future<?> second = privilegeKey.forceResolve().await();
		assertTrue(second.toString(), !second.isSuccess());

		System.out.println("Closing...!");
		con.quit().sync().get();
	}

}
