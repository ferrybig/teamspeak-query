/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import java.net.InetSocketAddress;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class PacketRateLimitingHandler extends ChannelTrafficShapingHandler {

	public PacketRateLimitingHandler() {
		super(0, 0, 1000);
	}

	@Override
	protected long calculateSize(Object msg) {
		return msg instanceof ComplexRequest ? 1000 : 0; // Assume size of 1000 B for all packets so this handler can use a max "packet" per seconds instead
	}
	
	private void adjustRateLimits(ChannelHandlerContext ctx) {
		InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
		if (!addr.getAddress().isLinkLocalAddress()) {
			this.setPacketsPerSeconds(10d / 3);
		} else {
			this.setPacketsPerSeconds(0);
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		if(ctx.channel().isActive()) {
			adjustRateLimits(ctx);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		adjustRateLimits(ctx);
	}
	
	

	public void setPacketsPerSeconds(double packetsPerMiliseconds) {
		this.setWriteLimit((int) (packetsPerMiliseconds * 1000));
	}

}
