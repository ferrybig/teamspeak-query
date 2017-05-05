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
import org.junit.Test;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class TeamspeakConnectionIT {
	
	public TeamspeakConnectionIT() {
	}

	@Test
	public void testSomeMethod() throws InterruptedException, ExecutionException {
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
			for(User user : users) {
				user.poke("Hello: " + user.getNickname() + ", Your ip address: " + user.getIp());
			}
			
			System.out.println("Closing...!");
			con.quit().sync().get();
			
			
			
		} finally {
			group.shutdownGracefully();
		}
	}
	
}
