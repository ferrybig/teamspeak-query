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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author Fernando
 */
public class PacketQueueBufferTest {

	@Test
	public void testSwap() {
		EmbeddedChannel channel = new EmbeddedChannel(new PacketQueueBuffer());
		channel.pipeline().get(PacketQueueBuffer.class).replace(new LoggingHandler());

		assertNull(channel.pipeline().get(PacketQueueBuffer.class));
		assertNotNull(channel.pipeline().get(LoggingHandler.class));
	}

	@Test
	public void noBufferPassthru() {
		EmbeddedChannel channel = new EmbeddedChannel(new PacketQueueBuffer());
		channel.writeInbound(Unpooled.EMPTY_BUFFER);

		assertNull(channel.readInbound());
	}

	@Test
	public void testPassThruAfterSwap() {
		EmbeddedChannel channel = new EmbeddedChannel(new PacketQueueBuffer());
		channel.writeInbound(Unpooled.EMPTY_BUFFER);

		channel.pipeline().get(PacketQueueBuffer.class).replace(new ChannelInitializer<EmbeddedChannel>() {
			@Override
			protected void initChannel(EmbeddedChannel ch) throws Exception {
			}
		});

		assertNotNull(channel.readInbound());

	}

}
