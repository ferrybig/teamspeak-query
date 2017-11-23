/*
 * The MIT License
 *
 * Copyright 2017 Fernando.
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

/**
 * Netty handler to buffer packets send till the next handler is ready
 */
public class PacketQueueBuffer extends ChannelInboundHandlerAdapter {

	// TODO optimalize using a single bytebuf
	private final List<ByteBuf> queue = new ArrayList<>();
	private boolean readComplete;
	private boolean replaced = false;
	private ChannelHandlerContext ctx;

	@Override
	public void channelReadComplete(@Nonnull ChannelHandlerContext ctx)
			throws Exception {
		if (replaced) {
			ctx.fireChannelReadComplete();
		} else {
			readComplete = true;
		}
	}

	@Override
	public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object msg)
			throws Exception {
		if (replaced) {
			ctx.fireChannelRead(msg);
		} else {
			queue.add((ByteBuf) msg);
		}
	}

	@Override
	public void channelActive(@Nonnull ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		this.ctx = ctx;
	}

	@Override
	public void channelRegistered(@Nonnull ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		this.ctx = ctx;
	}

	@Override
	public void channelInactive(@Nonnull ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		this.ctx = ctx;
		for (ByteBuf b : queue) {
			ReferenceCountUtil.release(b);
		}
		queue.clear();
	}

	public void replace(@Nonnull ChannelHandlerAdapter other) {
		this.ctx.pipeline().addLast(other);
		flushBuffers();
	}

	public void replace(@Nonnull Consumer<? super Channel> other) {
		other.accept(ctx.channel());
		flushBuffers();
	}

	private void flushBuffers() {
		this.ctx.pipeline().remove(this);
		for (ByteBuf b : queue) {
			this.ctx.fireChannelRead(b);
		}
		queue.clear();
		if (readComplete) {
			this.ctx.fireChannelReadComplete();
		}
		replaced = true;
	}

}
