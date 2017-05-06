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
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.SocketAddress;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakConnectionInitizer;
import me.ferrybig.javacoding.teamspeakconnector.util.FutureUtil;

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
			if (!f.isSuccess()) {

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
