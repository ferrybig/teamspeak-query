/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class TeamspeakConnectionIT {
	
	public TeamspeakConnectionIT() {
	}

	@Test
	public void testSomeMethod() throws InterruptedException, ExecutionException {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			System.out.println("Creating!");
			TeamspeakApi api = new TeamspeakApi(group);
			Future<TeamspeakConnection> connect = api.connect(new InetSocketAddress("127.0.0.1", 10011));
			
			System.out.println("Connected!");
			TeamspeakConnection con = connect.sync().get();
			
			System.out.println("Logging in!");
			con.login("serveradmin", "test1234").sync().get();
			
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
			
			System.out.println("Channel create!");
			System.out.println(con.getChannelList().sync().get());
			
			System.out.println("Closing...!");
			con.quit().sync().get();
			
		} finally {
			group.shutdownGracefully();
		}
	}
	
}
