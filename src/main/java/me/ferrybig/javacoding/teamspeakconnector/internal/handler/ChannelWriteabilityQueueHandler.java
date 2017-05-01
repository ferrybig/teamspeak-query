/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.PendingWriteQueue;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class ChannelWriteabilityQueueHandler extends ChannelHandlerAdapter {
	private boolean trafficStopped = false;
	
	private PendingWriteQueue queue;
	private int counter = 0;

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		queue = new PendingWriteQueue(ctx);
	}
	
	
	private void flushWhileWriteable(ChannelHandlerContext ctx) {
		do {
			ChannelFuture f = queue.removeAndWrite();
			if(f == null || counter++ == 2) {
				counter = 0;
				ctx.flush();
			}
			if(f == null) {
				return;
			}
		} while(ctx.channel().isWritable());
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
		trafficStopped = !ctx.channel().isWritable();
		if(!trafficStopped) {
			flushWhileWriteable(ctx);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if(trafficStopped) {
			queue.add(msg, promise);
		} else {
			if(queue.isEmpty()) {
				super.write(ctx, msg, promise);
			} else {
				flushWhileWriteable(ctx);
				this.write(ctx, msg, promise); // Try again...
			}
		}
	}
	
}
