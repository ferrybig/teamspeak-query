/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;

/**
 *
 * @author Fernando
 */
public class ComplexPacketDecoder extends MessageToMessageDecoder<Response> {

	private final ArrayList<Map<String, String>> queue = new ArrayList<>();

	@Override
	protected void decode(ChannelHandlerContext ctx, Response msg, List<Object> out) throws Exception {
		if (msg.getCmd().isEmpty()) {
			queue.add(msg.getOptions());
			return;
		}
		if (msg.getCmd().equals("notifytextmessage")) {
			out.add(msg);
		}
		out.add(new ComplexResponse(new ArrayList<>(queue), Integer.parseInt(msg.getOptions().get("id")), msg.getOptions().get("msg"), msg.getOptions().get("extra_msg")));
		queue.clear();
	}

}
