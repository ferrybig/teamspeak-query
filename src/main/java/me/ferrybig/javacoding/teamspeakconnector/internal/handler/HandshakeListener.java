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
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Promise;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;

public class HandshakeListener extends SimpleChannelInboundHandler<String> {

	private static final String TS_HEADER_1 = "TS3";
	private static final String TS_HEADER_2
			= "Welcome to the TeamSpeak 3 ServerQuery interface, type \"help\" for a list of commands and \"help <command>\" for information on a specific command.";
	private boolean headerReceived = false;
	private final Promise<TeamspeakConnection> prom;

	public HandshakeListener(Promise<TeamspeakConnection> prom) {
		this.prom = prom;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if (!prom.isDone()) {
			prom.tryFailure(new TeamspeakException("Connection closed before handshake"));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		if (!prom.isDone()) {
			prom.tryFailure(new TeamspeakException("Exception caugth", cause));
		}
		ctx.close();
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
		if (headerReceived) {
			if (!TS_HEADER_2.equals(msg)) {
				throw new DecoderException("Line 2 of magic header mismatch, expected: " + TS_HEADER_2 + "; got:" + msg);
			}
			ctx.pipeline().remove(ReadTimeoutHandler.class);
			TeamspeakConnection con = new TeamspeakConnection(new TeamspeakIO(ctx.channel()));
			con.start();
			if (!prom.trySuccess(con)) {
				ctx.channel().close();
			}
			ctx.pipeline().remove(this);
		} else {
			headerReceived = true;
			if (TS_HEADER_1.equals(msg)) {
				return;
			}
			throw new DecoderException("Line 1 of magic header mismatch, expected: " + TS_HEADER_1 + "; got:" + msg);
		}
	}

}
