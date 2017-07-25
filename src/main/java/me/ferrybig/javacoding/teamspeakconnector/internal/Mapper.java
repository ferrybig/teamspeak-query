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
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.util.concurrent.Future;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import me.ferrybig.javacoding.teamspeakconnector.entities.File;
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.Server;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.entities.User;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;

public class Mapper {

	private static final Logger LOG = Logger.getLogger(Mapper.class.getName());

	private final TeamspeakConnection con;

	public Mapper(TeamspeakConnection con) {
		this.con = con;
	}

	private InetAddress tryConvertAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException ex) {
			LOG.log(Level.FINE, "Trying to convert address to ip failed", ex);
			return null;
		}
	}

	public Server mapServer(Map<String, String> data) {
		return new Server(con,
				Integer.parseInt(data.get("virtualserver_id")),
				Integer.parseInt(data.get("virtualserver_port")),
				resolveEnum(Server.Status.class, data.get("virtualserver_status")),
				Integer.parseInt(data.getOrDefault("virtualserver_clientsonline", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_queryclientsonline", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_maxclients", "0")),
				Integer.parseInt(data.getOrDefault("virtualserver_uptime", "0")),
				data.get("virtualserver_name"),
				data.get("virtualserver_autostart").equals("1"));
	}

	public me.ferrybig.javacoding.teamspeakconnector.entities.Channel mapChannel(Map<String, String> data) {
		return new me.ferrybig.javacoding.teamspeakconnector.entities.Channel(con,
				Integer.parseInt(data.get("cid")),
				Integer.parseInt(data.get("channel_order")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("pid"))),
				data.get("channel_name"), data.get("channel_topic"),
				data.get("channel_flag_password").equals("1"),
				Integer.parseInt(data.getOrDefault("channel_needed_subscribe_power", "0")), // Not sure why this one is missing with `channelinfo`
				Integer.parseInt(data.get("channel_needed_talk_power")),
				data.get("channel_flag_default").equals("1"),
				data.get("channel_flag_permanent").equals("1"),
				Integer.parseInt(data.get("channel_icon_id")),
				Integer.parseInt(data.getOrDefault("total_clients_family", "0")), // These are missing with `channelinfo`
				Integer.parseInt(data.getOrDefault("channel_maxfamilyclients", "0")),
				Integer.parseInt(data.get("channel_maxclients")),
				Integer.parseInt(data.getOrDefault("total_clients", "0")),
				data.get("channel_flag_semi_permanent").equals("1"),
				me.ferrybig.javacoding.teamspeakconnector.entities.Channel.Codec.getById(Integer.parseInt(data.get("channel_codec"))),
				Integer.parseInt(data.get("channel_codec_quality")));
	}

	public User mapUser(Map<String, String> data) {
		return new User(con,
				Integer.parseInt(data.get("clid")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("cid"))),
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_nickname"),
				User.Type.getById(Integer.parseInt(data.get("client_type"))),
				data.get("client_away").equals("1") ? data.get("client_away_message") : null,
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
				data.get("client_unique_identifier"),
				mapIntList(data.get("client_servergroups"), con::getUnresolvedGroupById),
				mapIntList(data.get("client_channel_group_id"), con::getUnresolvedChannelGroupById),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_group_inherited_channel_id"))),
				data.get("client_version"),
				data.get("client_platform"),
				Integer.parseInt(data.get("client_idle_time")),
				Long.parseLong(data.get("client_created")),
				Long.parseLong(data.get("client_lastconnected")),
				Integer.parseInt(data.get("client_icon_id")),
				data.get("client_country"),
				tryConvertAddress(data.get("connection_client_ip")));
	}

	public ShallowUser mapShallowUser(Map<String, String> data) {
		return new ShallowUser(con,
				Integer.parseInt(data.get("clid")),
				data.get("client_unique_identifier"),
				Integer.parseInt(data.get("client_database_id")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("cid"))),
				data.get("client_nickname"),
				User.Type.getById(Integer.parseInt(data.get("client_type"))),
				data.get("client_away").equals("1") ? data.get("client_away_message") : null,
				data.get("client_input_muted").equals("1"),
				data.get("client_output_muted").equals("1"),
				data.get("client_input_hardware").equals("1"),
				data.get("client_output_hardware").equals("1"),
				Integer.parseInt(data.get("client_talk_power")),
				data.get("client_is_talker").equals("1"),
				data.get("client_is_priority_speaker").equals("1"),
				data.get("client_is_recording").equals("1"),
				data.get("client_is_channel_commander").equals("1"),
				mapIntList(data.get("client_servergroups"), con::getUnresolvedGroupById),
				mapIntList(data.get("client_channel_group_id"), con::getUnresolvedChannelGroupById),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_group_inherited_channel_id"))),
				Integer.parseInt(data.get("client_icon_id")),
				data.get("client_country"));
	}

	public User mapWhoAmI(Map<String, String> data) {
		return new User(con,
				Integer.parseInt(data.get("client_id")),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_id"))),
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_nickname"),
				User.Type.QUERY,
				null,
				false,
				true,
				true,
				true,
				true,
				0,
				false,
				false,
				false,
				false,
				data.get("client_unique_identifier"),
				Collections.emptyList(),
				Collections.emptyList(),
				con.getUnresolvedChannelById(Integer.parseInt(data.get("client_channel_id"))),
				"", // TODO program version
				"", // TODO program platform
				0,
				0,
				0,
				0,
				"",
				((InetSocketAddress) con.io().getChannel().localAddress()).getAddress());
	}

	/**
	 * Map a group received from Teamspeak.
	 *
	 * @param data Map containing received objects
	 * @return the mapped group
	 */
	public Group mapGroup(Map<String, String> data) {
		return new Group(con,
				Integer.parseInt(data.get("sgid")),
				Integer.parseInt(data.get("sortid")),
				Integer.parseInt(data.get("iconid")),
				data.get("savedb").equals("1"),
				data.get("name"),
				Integer.parseInt(data.get("n_member_removep")),
				Integer.parseInt(data.get("n_member_addp")),
				Integer.parseInt(data.get("n_modifyp")),
				Integer.parseInt(data.get("namemode")),
				Group.Type.getById(Integer.parseInt(data.get("type"))));
	}

	public File mapFile(Map<String, String> data) {
		return null; // TODO
	}

	public <R> Future<R> mapComplexReponse(Future<ComplexResponse> in, Function<Map<String, String>, R> mapper) {
		return con.io().chainFuture(in, r -> {
			if (r.getCommands().size() != 1) {
				throw new IllegalArgumentException("Cannot map a response of size " + r.getCommands().size() + " as a simple instance");
			}
			return mapper.apply(r.getCommands().get(0));
		});
	}

	public <R> Future<List<R>> mapComplexReponseList(Future<ComplexResponse> in, Function<Map<String, String>, R> mapper) {
		return this.<R, R>mapComplexReponseList(in, mapper, Function.identity());
	}

	public <R, I> Future<List<R>> mapComplexReponseList(Future<ComplexResponse> in, Function<Map<String, String>, I> mapper, Function<List<I>, List<R>> finalizer) {
		return con.io().chainFuture(in, r -> {
			return finalizer.apply(r.getCommands().stream().map(mapper).collect(Collectors.toList()));
		});
	}

	public <R> List<R> mapIntList(String in, IntFunction<R> mapper) {
		return in.isEmpty() ? new ArrayList<>() : Arrays.stream(in.split(",")).mapToInt(Integer::parseInt).mapToObj(mapper).collect(Collectors.toList());
	}

	public <E extends Enum<E>> E resolveEnum(Class<E> enu, String var) {
		return Enum.valueOf(enu, var.toUpperCase().replace(' ', '_'));
	}

	private Map<Integer, Channel> mapChannelParents(Map<Integer, Channel> list) {
		for (Channel c : list.values()) {
			UnresolvedChannel parentUnresolved = c.getParent();
			if (parentUnresolved == null) {
				continue;
			}
			Channel parent = list.get(parentUnresolved.getId());
			if (parent != null) {
				c.setParent(parent);
			}
		}
		return list;
	}

	@Nonnull
	public PrivilegeKey mapPrivilegeKey(Map<String, String> data) {
		Map<String, String> customSet = new HashMap<>();
		final String tokenCustomSetString = data.get("tokencustomset");
		if(!tokenCustomSetString.isEmpty()) {
			Map<String, String> customSetCache = new HashMap<>(2);
			for(String part : tokenCustomSetString.split("\\|")) {
				PacketDecoder.singleDecode(part, customSetCache, true, true);
				customSet.put(customSetCache.get("ident"), customSetCache.get("value"));
			}
		}
		return new PrivilegeKey(con,
				data.get("token"),
				customSet,
				data.get("tokendescription"),
				PrivilegeKey.Type.getById(Integer.parseInt(data.get("tokentype"))),
				Integer.parseInt(data.get("tokenid1")),
				Integer.parseInt(data.get("tokenid2")));
	}
}
