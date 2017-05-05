/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakConnectionInitizer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.SocketAddress;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

/**
 *
 * @author Fernando
 */
public class TeamspeakApi {

	private final EventLoopGroup group;

	public TeamspeakApi(EventLoopGroup group) {
		this.group = group;
	}

	public Future<TeamspeakConnection> connect(SocketAddress addr) {
		Promise<TeamspeakConnection> prom = this.group.next().newPromise();
		ChannelFuture channel = openChannel(addr, new TeamspeakConnectionInitizer(prom, 20000), 20000);
		channel.addListener(future -> {
			if (!channel.isSuccess()) {
				prom.setFailure(new TeamspeakException("Connection failed", channel.cause()));
			}
		});
		return prom;
	}
	
	public Future<TeamspeakConnection> connect(SocketAddress addr, String username, String password) {
		final Future<TeamspeakConnection> connectFuture = connect(addr);
		final Future<TeamspeakConnection> result = FutureUtil.chainFutureFlat(this.group.next().newPromise(), connectFuture, con -> con.login(username, password));
		result.addListener(f -> {
			if(!f.isSuccess()) {
				
			}
		});
		return result;
	}

	private ChannelFuture openChannel(SocketAddress addr, ChannelInitializer<? extends SocketChannel> ch, int timneout) {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.remoteAddress(addr);
		bootstrap.handler(ch);
		bootstrap.group(group);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		return bootstrap.connect();
	}

}
