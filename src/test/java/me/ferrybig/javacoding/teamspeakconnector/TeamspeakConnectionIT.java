/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.event.ClientEnterViewEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ClientLeftViewEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerEditEvent;
import me.ferrybig.javacoding.teamspeakconnector.event.ServerListener;
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

//			System.out.println("Selecting server!");
//			con.getUnresolvedServerById(1).select().sync().get();

			System.out.println("Connected!");
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//			con.getServer();
//
//			System.out.println("Queue make!");
//			System.out.println(con.getServer().sync().get());
//
//			System.out.println("Username seting....");
//			con.setOwnName("TestingBot").sync().get();

			System.out.println("Group list");
			final List<Group> groups = con.getGroups().sync().get();
			groups.forEach(System.out::println);

			Optional<Group> bottest = groups.stream().filter(g -> (g.getType() == Group.Type.REGULAR) && g.getName().equals("BotTest")).findAny();

			System.out.println("User list!");
			List<User> users = con.getUsersList().sync().get();
			for (User user : users) {
				if (user.getType() == ClientType.QUERY) {
					continue;
				}

				//user.poke("Hello: " + user.getNickname() + ", Your ip address: " + user.getIp());
				user.sendMessage("Hello: " + user.getNickname()
						+ ", Your ip address: " + user.getIp()
						+ ", and your in the following groups: " + user.getServerGroup().stream().map(Object::toString).collect(Collectors.joining()));
				if (bottest.isPresent()) {
					user.addToGroup(bottest.get()).get();
				}
			}

			System.out.println("Waiting for messages!");
			con.getPrivateMessageHandler().addHandler(evt -> {
				System.out.println("Message received! " + evt);
				evt.getInvoker().sendMessage("You said: " + evt.getMessage());
			});

			System.out.println("Waiting for eserver events!");
			con.getServerHandler().addHandler(new ServerListener() {
				@Override
				public void onClientEnterView(ClientEnterViewEvent event) {
					LOG.log(Level.INFO, "ClientEnterViewEvent: {0}", event);
					event.getClient().sendMessage("Hello " + event.getClient().getNickname() + "!");
					event.getClient().getChannel().resolv().addListener((Future<Channel> channel) -> {
						event.getClient().sendMessage("Welcome to channel " + channel.get().getName() + "!");
					});
				}

				@Override
				public void onClientLeaveView(ClientLeftViewEvent event) {
					LOG.log(Level.INFO, "ClientLeftViewEvent: {0}", event);
				}

				@Override
				public void onEditServer(ServerEditEvent event) {
					LOG.log(Level.INFO, "ServerEditEvent: {0}", event);
				}

			});

			Thread.sleep(10000);
			if(bottest.isPresent()) {
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

}
