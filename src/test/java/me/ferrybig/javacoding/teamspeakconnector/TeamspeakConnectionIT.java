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
import me.ferrybig.javacoding.teamspeakconnector.entities.User;
import static me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil.waitSync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 *
 */
public class TeamspeakConnectionIT {

	private static final Logger LOG = Logger.getLogger(TeamspeakConnectionIT.class.getName());

	@Test
	public void testSomeMethod() throws InterruptedException, ExecutionException {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			System.out.println("Creating!");
			TeamspeakBootstrap ts = new TeamspeakBootstrap(group);
			ts.login("serveradmin", "test1234");
			ts.selectServerID(1);
			ts.clientName("TestingBot");

			System.out.println("Connecting...");
			Future<TeamspeakConnection> connect = ts.connect("localhost", 10011);
			TeamspeakConnection con = connect.sync().get();

			System.out.println("Connected!");

			System.out.println("Channel list");
			final List<Channel> channel = con.getChannelList().sync().get();
			channel.forEach(System.out::println);

			System.out.println("Group list");
			final List<Group> groups = con.getGroups().sync().get();
			groups.forEach(System.out::println);

			Optional<Group> bottest = groups.stream().filter(g -> (g.getType() == Group.Type.REGULAR) && g.getName().equals("BotTest")).findAny();

			System.out.println("User list!");
			List<User> users = con.getUsersList().sync().get();
			for (User user : users) {
				if (user.getType() == me.ferrybig.javacoding.teamspeakconnector.entities.User.Type.QUERY) {
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

		} finally {
			group.shutdownGracefully();
		}
	}

	@Test
	public void messagesAreSendAndReceived() throws InterruptedException, ExecutionException {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			System.out.println("Creating!");
			TeamspeakBootstrap ts = new TeamspeakBootstrap(group);
			ts.login("serveradmin", "test1234");
			ts.selectServerID(1);

			Future<TeamspeakConnection> connect1 = ts.clientName("TestingBot1").connect("localhost", 10011);
			Future<TeamspeakConnection> connect2 = ts.clientName("TestingBot2").connect("localhost", 10011);

			TeamspeakConnection con1 = connect1.get();
			TeamspeakConnection con2 = connect2.get();

			System.out.println("User list!");
			User con1user2 = null;
			List<User> users = con1.getUsersList().sync().get();
			assertNotEquals(con1.io().whoAmI().get().getId(), con2.io().whoAmI().get().getId());
			for (User user : users) {
				if (user.getType() == me.ferrybig.javacoding.teamspeakconnector.entities.User.Type.QUERY) {
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
		} finally {
			group.shutdownGracefully();
		}
	}

}
