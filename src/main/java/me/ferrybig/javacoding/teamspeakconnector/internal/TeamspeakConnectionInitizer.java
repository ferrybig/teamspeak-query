/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Promise;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.ChannelWriteabilityQueueHandler;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.ComplexPacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.HandshakeListener;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketEncoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketRateLimitingHandler;

/**
 *
 * @author Fernando
 */
public class TeamspeakConnectionInitizer extends ChannelInitializer<SocketChannel> {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	private static final PacketDecoder PACKET_DECODER = new PacketDecoder();
	private static final PacketEncoder PACKET_ENCODER = new PacketEncoder();
	private static final ByteBuf[] LINES = new ByteBuf[] {
                Unpooled.wrappedBuffer(new byte[] { '\r', '\n' }), // I hate you teamspeak, Why do you use this format??
                Unpooled.wrappedBuffer(new byte[] { '\n', '\r' }),
                Unpooled.wrappedBuffer(new byte[] { '\n' }),
        };
	private final Promise<TeamspeakConnection> prom;
	private final int timeout;

	public TeamspeakConnectionInitizer(Promise<TeamspeakConnection> prom, int timeout) {
		this.prom = prom;
		this.timeout = timeout;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new DelimiterBasedFrameDecoder(Short.MAX_VALUE, LINES));
		pipeline.addLast(DECODER);
		pipeline.addLast(ENCODER);
		pipeline.addLast(new ReadTimeoutHandler(timeout));
		pipeline.addLast(new HandshakeListener(prom));
		
		pipeline.addLast(PACKET_DECODER);
		pipeline.addLast(new ComplexPacketDecoder());
		pipeline.addLast(PACKET_ENCODER);
		
		pipeline.addLast(new LoggingHandler(LogLevel.INFO));
		
		pipeline.addLast(new PacketRateLimitingHandler());
	}

}
