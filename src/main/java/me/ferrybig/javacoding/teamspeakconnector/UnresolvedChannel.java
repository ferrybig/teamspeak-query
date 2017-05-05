/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import java.util.List;
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
	
	public Future<List<File>> getFileTransferList() {
		return getFileTransferList("/");
	}
	
	public Future<List<File>> getFileTransferList(String path) {
		return con.io().mapComplexReponseList(con.io().sendPacket(
				new ComplexRequestBuilder("ftgetfilelist").addData("cid", getId()).addData("path", path).build()),
				con.io()::mapFile);
	}

}
