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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;

@ChannelHandler.Sharable
public class PacketDecoder extends MessageToMessageDecoder<String> {

	private static String decodeTeamspeakCode(String input) {

		char[] chars = input.toCharArray();
		int newLength = chars.length;
		int ahead = 0;

		for (int i = 0; i < newLength; i++) {
			if (chars[i + ahead] == '\\') {
				switch (chars[i + ahead + 1]) {
					case '\\':
						chars[i] = '\\';
						break;
					case '/':
						chars[i] = '/';
						break;
					case 's':
						chars[i] = ' ';
						break;
					case 'p':
						chars[i] = '|';
						break;
					case 'a':
						chars[i] = '\u0007';
						break;
					case 'b':
						chars[i] = '\b';
						break;
					case 'f':
						chars[i] = '\f';
						break;
					case 'n':
						chars[i] = '\n';
						break;
					case 'r':
						chars[i] = '\r';
						break;
					case 't':
						chars[i] = '\t';
						break;
					case 'v':
						chars[i] = '\u000b';
						break;
					default:
						throw new DecoderException("Unable to decode pattern \\"
								+ chars[i + ahead + 1] + " in the "
								+ "following text: " + input);
				}

				ahead++;
				newLength--;
			} else if (ahead != 0) {
				chars[i] = chars[i + ahead];
			}
		}
		if (newLength == chars.length) {
			return input;
		}
		return new String(chars, 0, newLength);
	}

	public static Response singleDecode(String msg, Map<String, String> cache,
			boolean first, boolean last) {
		String[] split = msg.split(" ");
		String cmd = "";
		for (int i = 0; i < split.length; i++) {
			String[] args = split[i].split("=", 2);
			if (args.length == 1) {
				if (i == 0 && first) {
					cmd = decodeTeamspeakCode(args[0]);
				} else {
					// Special cases for when the value is empty
					cache.put(decodeTeamspeakCode(args[0]), "");
				}
			} else {
				cache.put(decodeTeamspeakCode(args[0]),
						decodeTeamspeakCode(args[1]));
			}
		}
		return new Response(last ? cache : new HashMap<>(cache), cmd);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, String msg,
			List<Object> out) throws Exception {
		String cmd = "";
		Map<String, String> cache = new HashMap<>();
		if (msg.charAt(0) == '\r') {
			msg = msg.substring(1);
		}
		String[] compound = msg.split("\\|");
		for (int j = 0; j < compound.length; j++) {
			out.add(singleDecode(compound[j], cache, j == 0,
					compound.length - 1 == j));
		}
	}

}
