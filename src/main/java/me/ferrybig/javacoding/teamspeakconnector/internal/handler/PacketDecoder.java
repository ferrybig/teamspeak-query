/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;

/**
 *
 * @author Fernando
 */
public class PacketDecoder extends MessageToMessageDecoder<String> {

	private String decodeTeamspeakCode(String input) {

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
						throw new DecoderException("Unable to decode pattern \\" + 
								chars[i + ahead + 1] + " in the following text: " + input);
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

	@Override
	protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
		Map<String, String> options = new HashMap<>();
		String cmd = "";
		if(msg.charAt(0) == '\r') {
			msg = msg.substring(1);
		}
		String[] compound = msg.split("\\|");
		for (int j = 0; j < compound.length; j++) {
			String[] split = compound[j].split(" ");
			for (int i = 0; i < split.length; i++) {
				String[] args = split[i].split("=", 2);
				if (args.length == 1) {
					if (i == 0 && j == 0) {
						cmd = decodeTeamspeakCode(args[0]);
					} else {
						options.put(decodeTeamspeakCode(args[0]), ""); // Special cases for when the value is empty
					}
				} else {
					options.put(decodeTeamspeakCode(args[0]), decodeTeamspeakCode(args[1]));
				}
			}
			out.add(new Response(compound.length - 1 == j ? options : new HashMap<>(options), cmd));
		}
	}

}
