/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Promise;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakApi;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;

/**
 *
 * @author Fernando
 */
public class HandshakeListener extends SimpleChannelInboundHandler<String> {

	private static final String TS_HEADER_1 = "TS3";
	private static final String TS_HEADER_2 = 
			"Welcome to the TeamSpeak 3 ServerQuery interface, type \"help\" for a list of commands and \"help <command>\" for information on a specific command.";
	private boolean headerReceived = false;
	private final Promise<TeamspeakConnection> prom;

	public HandshakeListener(Promise<TeamspeakConnection> prom) {
		this.prom = prom;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if(!prom.isDone()) {
			prom.tryFailure(new TeamspeakException("Connection closed before handshake"));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		if(!prom.isDone()) {
			prom.tryFailure(new TeamspeakException("Exception caugth", cause));
		}
		ctx.close();
	}
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
		if(headerReceived) {
			if(!TS_HEADER_2.equals(msg)) {
				throw new DecoderException("Line 2 of magic header mismatch, expected: " + TS_HEADER_2 + "; got:" + msg);
			}
			ctx.pipeline().remove(ReadTimeoutHandler.class);
			TeamspeakConnection con = new TeamspeakConnection(new TeamspeakIO(ctx.channel()));
			con.start();
			if(!prom.trySuccess(con)) {
				ctx.channel().close();
			}
			ctx.pipeline().remove(this);
		} else {
			headerReceived = true;
			if(TS_HEADER_1.equals(msg)) {
				return;
			}
			throw new DecoderException("Line 1 of magic header mismatch, expected: " + TS_HEADER_1 + "; got:" + msg);
		}
	}
	
	
}
