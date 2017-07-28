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
package me.ferrybig.javacoding.teamspeakconnector.repository;

import io.netty.util.concurrent.Future;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServerWithId;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedServerWithPort;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 * Repository for containing server objects.
 */
public class ServerRepository extends AbstractIntResolvableRepository<UnresolvedServerWithId, Server> {

	public ServerRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public UnresolvedServerWithId unresolved(int id) {
		return new UnresolvedServerWithId(this, id);
	}

	public UnresolvedServerWithPort unresolvedByPort(int port) {
		return new UnresolvedServerWithPort(this, port);
	}

	@Override
	protected ComplexRequest requestGet(UnresolvedServerWithId unresolved) {
		return null;
	}

	@Override
	protected ComplexRequest requestDelete(UnresolvedServerWithId unresolved, boolean force) {
		return Command.SERVER_DELETE
				.addData("sgid", unresolved.getSid())
				.build();
	}

	@Override
	protected ComplexRequest requestList() {
		return Command.SERVER_LIST.addOption("virtual").build();
	}

	@Override
	protected int getId(UnresolvedServerWithId value) {
		return value.getSid();
	}

	/**
	 * Map a server received from Teamspeak.
	 *
	 * @param data Map containing received objects
	 * @return the mapped server
	 */
	@Override
	protected Server readEntity(Map<String, String> data) {
		return new Server(this,
				Integer.parseInt(data.get("virtualserver_id")),
				Integer.parseInt(data.get("virtualserver_port")),
				getConnection().mapping().resolveEnum(Server.Status.class,
						data.get("virtualserver_status")),
				Integer.parseInt(
						data.getOrDefault("virtualserver_clientsonline", "0")),
				Integer.parseInt(
						data.getOrDefault(
								"virtualserver_queryclientsonline", "0")),
				Integer.parseInt(
						data.getOrDefault("virtualserver_maxclients", "0")),
				Integer.parseInt(
						data.getOrDefault("virtualserver_uptime", "0")),
				data.get("virtualserver_name"),
				data.get("virtualserver_autostart").equals("1"));
	}

	public Future<Server> getSelected() {
		return getConnection().mapping().mapComplexReponse(io.sendPacket(
				Command.SERVER_INFO.build()),
				this::readEntity);
	}

}
