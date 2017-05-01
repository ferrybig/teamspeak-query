/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;

/**
 *
 * @author Fernando
 */
public class Server extends UnresolvedServer {

	private final int port;
	private final ServerStatus status;
	private final int clientsOnline;
	private final int queryClientsOnline;
	private final int maxClients;
	private final int uptime;
	private final String name;
	private final boolean autostart;
	
	public Server(TeamspeakConnection con, int sid, int port, ServerStatus status, int clientsOnline, int queryClientsOnline, int maxClients, int uptime, String name, boolean autostart) {
		super(con, sid);
		this.port = port;
		this.status = status;
		this.clientsOnline = clientsOnline;
		this.queryClientsOnline = queryClientsOnline;
		this.maxClients = maxClients;
		this.uptime = uptime;
		this.name = name;
		this.autostart = autostart;
	}

	public int getPort() {
		return port;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public int getClientsOnline() {
		return clientsOnline;
	}

	public int getQueryClientsOnline() {
		return queryClientsOnline;
	}

	public int getMaxClients() {
		return maxClients;
	}

	public int getUptime() {
		return uptime;
	}

	public String getName() {
		return name;
	}

	public boolean isAutostart() {
		return autostart;
	}
	
}
