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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

@ChannelHandler.Sharable
public class PacketEncoder extends MessageToMessageEncoder<ComplexRequest> {

	private static final ByteBuf SPACE = Unpooled.wrappedBuffer(" ".getBytes(StandardCharsets.UTF_8));
	private static final ByteBuf EQUALS = Unpooled.wrappedBuffer("=".getBytes(StandardCharsets.UTF_8));
	private static final ByteBuf LINEFEED = Unpooled.wrappedBuffer("\n".getBytes(StandardCharsets.UTF_8));

	public static String encodeTeamspeakCode(String input) {

		final char[] chars = input.toCharArray();
		char[] output = Arrays.copyOf(chars, chars.length + 16);
		int writeIndex = 0;
		for (int readIndex = 0; readIndex < chars.length; readIndex++) {
			char encoded;
			switch (chars[readIndex]) {
				case '\\':
					encoded = '\\';
					break;
				case '/':
					encoded = '/';
					break;
				case ' ':
					encoded = 's';
					break;
				case '|':
					encoded = 'p';
					break;
				case '\u0007':
					encoded = 'a';
					break;
				case '\b':
					encoded = 'b';
					break;
				case '\f':
					encoded = 'f';
					break;
				case '\n':
					encoded = 'n';
					break;
				case '\r':
					encoded = 'r';
					break;
				case '\t':
					encoded = 't';
					break;
				case '\u000b':
					encoded = 'v';
					break;
				default:
					encoded = 0;
			}
			if (output.length < writeIndex + 1) {
				output = Arrays.copyOf(output, output.length + 16);
			}
			if (encoded != 0) {
				output[writeIndex++] = '\\';
				output[writeIndex++] = encoded;
			} else {
				output[writeIndex++] = chars[readIndex];
			}
		}
		if (chars.length == output.length) {
			return input;
		}
		return new String(output, 0, writeIndex);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ComplexRequest msg, List<Object> out) throws Exception {
		out.add(msg.getCmd());
		for (Map.Entry<String, String> data : msg.getData().entrySet()) {
			out.add(SPACE.retain());
			out.add(encodeTeamspeakCode(data.getKey()));
			if (!data.getValue().isEmpty()) {
				out.add(EQUALS.retain());
				out.add(encodeTeamspeakCode(data.getValue()));
			}
		}
		out.add(LINEFEED.retain());
	}

}
