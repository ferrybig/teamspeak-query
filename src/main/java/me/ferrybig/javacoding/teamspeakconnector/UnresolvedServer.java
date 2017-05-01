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
public class UnresolvedServer {

	protected final TeamspeakConnection con;
	private final int sid;

	public UnresolvedServer(TeamspeakConnection con, int sid) {
		this.con = con;
		this.sid = sid;
	}

	public Future<TeamspeakConnection> select() {
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("use").addData("sid", String.valueOf(sid)).addOption("virtual").build()), ignored -> con);
	}
	
	public Future<TeamspeakConnection> stop() {
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("serverstop").addData("sid", String.valueOf(sid)).build()), ignored -> con);
	}
	
	public Future<TeamspeakConnection> start() {
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("serverstart").addData("sid", String.valueOf(sid)).build()), ignored -> con);
	}
}
