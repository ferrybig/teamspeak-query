/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
		Logger.getGlobal().getParent().setLevel(Level.ALL);
		Logger.getLogger(TeamspeakConnection.class.getName()).setLevel(Level.ALL);
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			System.out.println("Creating!");
			TeamspeakApi api = new TeamspeakApi(group);
			Future<TeamspeakConnection> connect = api.connect(new InetSocketAddress("127.0.0.1", 10011), "serveradmin", "test1234");

			System.out.println("Connected!");
			TeamspeakConnection con = connect.sync().get();

			System.out.println("Selecting server!");
			con.getUnresolvedServerById(1).select().sync().get();

			System.out.println("Creating!");
			con.getServer();
			con.getServer();
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

			System.out.println("Queue make!");
			System.out.println(con.getServer().sync().get());

			System.out.println("Username seting....");
			con.setOwnName("TestingBot").sync().get();

			System.out.println("User list!");
			List<User> users = con.getUsersList().sync().get();
			for (User user : users) {
				if (user.getType() == ClientType.QUERY) {
					continue;
				}
				user.poke("Hello: " + user.getNickname() + ", Your ip address: " + user.getIp());
				user.sendMessage("Hello: " + user.getNickname() + ", Your ip address: " + user.getIp());
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

			Thread.sleep(100000);

			System.out.println("Closing...!");
			con.quit().sync().get();

		} finally {
			group.shutdownGracefully();
		}
	}

}
