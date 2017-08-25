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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.PendingWriteQueue;

public class ChannelWriteabilityQueueHandler extends ChannelDuplexHandler {

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
			if (f == null || counter++ == 2) {
				counter = 0;
				ctx.flush();
			}
			if (f == null) {
				return;
			}
		} while (ctx.channel().isWritable());
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx)
			throws Exception {
		super.channelWritabilityChanged(ctx);
		trafficStopped = !ctx.channel().isWritable();
		if (!trafficStopped) {
			flushWhileWriteable(ctx);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if (trafficStopped) {
			queue.add(msg, promise);
		} else {
			if (queue.isEmpty()) {
				super.write(ctx, msg, promise);
			} else {
				flushWhileWriteable(ctx);
				this.write(ctx, msg, promise); // Try again...
			}
		}
	}

}
