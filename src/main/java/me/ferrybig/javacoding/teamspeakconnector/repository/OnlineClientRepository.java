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

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.OnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedOnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout
 */
@ParametersAreNonnullByDefault
@ThreadSafe
public class OnlineClientRepository extends AbstractIntResolvableRepository<UnresolvedOnlineClient, OnlineClient> {

	public OnlineClientRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public UnresolvedOnlineClient unresolved(int id) {
		return new UnresolvedOnlineClient(this, id);
	}

	@Override
	protected int getId(UnresolvedOnlineClient value) {
		return value.getClientId();
	}

	@Override
	protected OnlineClient readEntity(Map<String, String> data) {
		return new OnlineClient(this,
				Integer.parseInt(data.get("clid")),
				connection.channels().unresolved(Integer.parseInt(data.get("cid"))),
				OnlineClient.Type.getById(Integer.parseInt(data.get("client_type"))),
				data.get("client_away").equals("1")
				? data.get("client_away_message") : null,
				data.get("client_flag_talking").equals("1"),
				data.get("client_input_muted").equals("1"),
				data.get("client_output_muted").equals("1"),
				data.get("client_input_hardware").equals("1"),
				data.get("client_output_hardware").equals("1"),
				Integer.parseInt(data.get("client_talk_power")),
				data.get("client_is_talker").equals("1"),
				data.get("client_is_priority_speaker").equals("1"),
				data.get("client_is_recording").equals("1"),
				data.get("client_is_channel_commander").equals("1"),
				connection.mapping().mapIntList(data.get("client_servergroups"),
						connection.groups()::unresolved),
				connection.mapping().mapIntList(data.get("client_channel_group_id"),
						connection::getUnresolvedChannelGroupById),
				connection.channels().unresolved(
						Integer.parseInt(data.get(
								"client_channel_group_inherited_channel_id"))),
				data.get("client_version"),
				data.get("client_platform"),
				Integer.parseInt(data.get("client_idle_time")),
				Integer.parseInt(data.get("client_icon_id")),
				data.get("client_country"),
				connection.offlineClients().readEntityShallow(data));
	}

	@Override
	protected ComplexRequest requestList() {
		return Command.CLIENT_LIST.build();
	}

	@Override
	protected ComplexRequest requestGet(UnresolvedOnlineClient unresolved) {
		return null;
	}

	@Override
	protected ComplexRequest requestDelete(UnresolvedOnlineClient unresolved, boolean force) {
		return Command.CLIENT_KICK
				.addData("clid", getId(unresolved))
				.build();
	}

}
