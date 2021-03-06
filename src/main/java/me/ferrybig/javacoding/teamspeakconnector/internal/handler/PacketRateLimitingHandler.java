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
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import me.ferrybig.javacoding.teamspeakconnector.RateLimit;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;


/**
 * Handler to rate limit the maximum packets per second
 * @author Fernando
 */
public class PacketRateLimitingHandler extends ChannelTrafficShapingHandler {

	private final RateLimit rateLimit;

	public PacketRateLimitingHandler(RateLimit rateLimit) {
		super(0, 0, 1000);
		this.rateLimit = rateLimit;
	}

	@Override
	protected long calculateSize(Object msg) {
		// Assume size of 1000 B for all packets so this handler can use a
		// max "packet" per seconds instead
		return msg instanceof ComplexRequest ? 1000 : 0;
	}

	private void adjustRateLimits(ChannelHandlerContext ctx) {
		this.setPacketsPerSeconds(rateLimit
				.maxPacketsPerSecond(ctx.channel().remoteAddress()));
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		if (ctx.channel().isActive()) {
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
