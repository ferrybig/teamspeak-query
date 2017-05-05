/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

/**
 *
 * @author Fernando
 */
public class UnresolvedUser implements Resolvable<User>{

	protected final TeamspeakConnection con;
	private final int id;

	public UnresolvedUser(TeamspeakConnection con, int id) {
		this.con = con;
		this.id = id;
		
	}
	
	public Future<?> sendMessage(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("sendtextmessage")
						.addData("targetmode", "1")
						.addData("target", String.valueOf(getId()))
						.addData("msg", message)
						.build());
	}
	
	public Future<?> poke(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientpoke")
						.addData("clid", String.valueOf(getId()))
						.addData("msg", message)
						.build());
	}
	
	public Future<?> kickFromChannel(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientkick")
						.addData("clid", String.valueOf(getId()))
						.addData("reasonid", "4")
						.addData("msg", message)
						.build());
	}
	
	public Future<?> kickFromServer(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientpoke")
						.addData("clid", String.valueOf(getId()))
						.addData("reasonid", "5")
						.addData("msg", message)
						.build());
	}
	
	public Future<?> move(UnresolvedChannel channel) {
		return this.move(channel, "");
	}
	
	public Future<?> move(UnresolvedChannel channel, String password) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientmove")
						.addData("clid", String.valueOf(getId()))
						.addData("cid", String.valueOf(channel.getId()))
						.build());
	}

	public int getId() {
		return id;
	}

	@Override
	public Future<User> forceResolve() {
		return con.getUserById(id);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public String toString() {
		return "UnresolvedUser{" + "id=" + id + '}';
	}
	
}
