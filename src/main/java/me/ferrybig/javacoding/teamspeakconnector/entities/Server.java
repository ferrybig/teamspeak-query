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
package me.ferrybig.javacoding.teamspeakconnector.entities;

import me.ferrybig.javacoding.teamspeakconnector.repository.ServerRepository;

public class Server extends UnresolvedServerWithId {

	private final int port;
	private final Status status;
	private final int clientsOnline;
	private final int queryClientsOnline;
	private final int maxClients;
	private final int uptime;
	private final String name;
	private final boolean autostart;

	public Server(ServerRepository repo, int sid, int port, Status status,
			int clientsOnline, int queryClientsOnline, int maxClients,
			int uptime, String name, boolean autostart) {
		super(repo, sid);
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

	public Status getStatus() {
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

	public static enum Status {
		ONLINE,
		OFFLINE,
		DEPLOY_RUNNING,
		BOOTING_UP,
		SHUTTING_DOWN,
		ONLINE_VIRTUAL
	}

}
