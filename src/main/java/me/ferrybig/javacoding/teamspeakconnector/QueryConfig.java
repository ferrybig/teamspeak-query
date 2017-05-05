/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class QueryConfig {
	private String username;
	private String password;
	private List<SocketAddress> addresses;
	private int virtualServerId;
	private int virtualServerPort;
	private String localName;
	
	public QueryConfig login(String username, String password) {
		this.username = Objects.requireNonNull(username, "username");
		this.password = Objects.requireNonNull(password, "password");
		return this;
	}
	
	public QueryConfig noLogin() {
		this.username = null;
		this.password = null;
		return this;
	}
	
	public Future<TeamspeakConnection> connect(List<SocketAddress> endpoints) {
		return null; //todo
	}
	
	private Future<TeamspeakConnection> decorateConnection(Future<TeamspeakConnection> connection) {
		return null; //todo
	}
	
	
}
