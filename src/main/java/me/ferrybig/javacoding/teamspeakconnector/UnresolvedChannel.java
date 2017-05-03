/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

/**
 *
 * @author Fernando
 */
public class UnresolvedChannel {

	protected final TeamspeakConnection con;

	private final int id;

	public UnresolvedChannel(TeamspeakConnection con, int id) {
		this.con = con;
		this.id = id;
	}

	public Future<Channel> resolv() {
		return forceResolv();
	}

	public Future<Channel> forceResolv() {
		return con.getChannelById(id);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "UnresolvedChannel{" + "id=" + id + '}';
	}

	public Future<?> sendMessage(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("sendtextmessage")
						.addData("targetmode", "2")
						.addData("target", String.valueOf(getId()))
						.addData("msg", message)
						.build());
	}

}
