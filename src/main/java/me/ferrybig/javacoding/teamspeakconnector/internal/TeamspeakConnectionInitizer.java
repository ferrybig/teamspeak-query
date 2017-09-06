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
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Promise;
import java.util.function.Consumer;
import me.ferrybig.javacoding.teamspeakconnector.RateLimit;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.ComplexPacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.HandshakeListener;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketEncoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketRateLimitingHandler;

public class TeamspeakConnectionInitizer implements Consumer<Channel> {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	private static final PacketDecoder PACKET_DECODER = new PacketDecoder();
	private static final PacketEncoder PACKET_ENCODER = new PacketEncoder();
	private static final ByteBuf[] LINES = new ByteBuf[]{
		// I hate you teamspeak, Why do you use this format??
		Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}),
		Unpooled.wrappedBuffer(new byte[]{'\n', '\r'}),
		Unpooled.wrappedBuffer(new byte[]{'\n'}),};
	private final Promise<TeamspeakConnection> prom;
	private final RateLimit rateLimit;
	private final int timeout;

	public TeamspeakConnectionInitizer(Promise<TeamspeakConnection> prom,
			RateLimit rateLimit, int timeout) {
		this.prom = prom;
		this.rateLimit = rateLimit;
		this.timeout = timeout;
	}

	@Override
	public void accept(Channel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new DelimiterBasedFrameDecoder(
				Short.MAX_VALUE, LINES));
		pipeline.addLast(DECODER);
		pipeline.addLast(ENCODER);
		pipeline.addLast(new ReadTimeoutHandler(timeout));
		pipeline.addLast(new HandshakeListener(prom));

		pipeline.addLast(PACKET_DECODER);
		pipeline.addLast(new ComplexPacketDecoder());
		pipeline.addLast(PACKET_ENCODER);

		pipeline.addLast(new LoggingHandler(TeamspeakConnectionInitizer.class,
				LogLevel.DEBUG));

		pipeline.addLast(new PacketRateLimitingHandler(rateLimit));
	}

}
