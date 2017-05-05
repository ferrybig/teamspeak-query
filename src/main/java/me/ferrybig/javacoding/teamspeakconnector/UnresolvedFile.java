/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class UnresolvedFile {
	private final TeamspeakConnection connection;
	private final UnresolvedChannel channel;
	private final String name;

	public UnresolvedFile(TeamspeakConnection connection, UnresolvedChannel channel, String name) {
		this.connection = connection;
		this.channel = channel;
		this.name = name;
	}
	
	public Future<File> resolve() {
		return this.connection.getFileByChannelAndName(channel, name);
	}

	public UnresolvedChannel getChannel() {
		return channel;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "UnresolvedFile{" + "connection=" + connection + ", channel=" + channel + ", name=" + name + '}';
	}
	
}
