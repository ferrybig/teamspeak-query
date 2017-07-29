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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannelWithName;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout
 */
public class ChannelRepository extends AbstractIntResolvableRepository<UnresolvedChannel, Channel> {

	public ChannelRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public UnresolvedChannel unresolved(int id) {
		if (id == 0) {
			throw new IllegalArgumentException("Channel id 0 is reserved");
		}
		return new UnresolvedChannel(this, id);
	}

	@Nullable
	public UnresolvedChannel unresolvedOrNull(int id) {
		if (id == 0) {
			return null;
		}
		return new UnresolvedChannel(this, id);
	}

	@Override
	protected ComplexRequest requestGet(UnresolvedChannel unresolved) {
		return Command.CHANNEL_INFO
				.addData("cid", getId(unresolved))
				.build();
	}

	@Override
	protected ComplexRequest requestDelete(UnresolvedChannel unresolved, boolean force) {
		return Command.CHANNEL_DELETE
				.addData("cid", getId(unresolved))
				.addData("force", force ? "1" : "0")
				.build();
	}

	@Override
	protected ComplexRequest requestList() {
		return Command.CHANNEL_LIST.build();
	}

	@Override
	protected int getId(UnresolvedChannel value) {
		return value.getId();
	}

	@Override
	public Future<Channel> get(UnresolvedChannel unresolved, boolean force) {
		Objects.requireNonNull(unresolved, "unresolved");
		return connection.mapping().mapComplexReponse(
				io.sendPacket(requestGet(unresolved)),
				m -> {
					// This is needed because teamspeak doesn't repeat our send channel id
					m.put("cid", String.valueOf(getId(unresolved)));
					return readEntity(m);
				});
	}

	@Override
	public Future<List<Channel>> list() {
		return connection.mapping().mapComplexReponseList(
				io.sendPacket(requestList()),
				this::readEntity,
				this::mapChannelParents);
	}

	@Nonnull
	public Future<List<UnresolvedChannelWithName>> findByName(String name) {
		return connection.mapping().mapComplexReponseList(
				io.sendPacket(Command.CHANNEL_FIND
						.addData("pattern", name).build()),
				this::readEntity);
	}

	@Nonnull
	public Future<List<Channel>> findByNameAndResolv(String name) {
		return findByRegexAndResolv(Pattern.quote(name), Pattern.CASE_INSENSITIVE);
	}

	@Nonnull
	public Future<List<Channel>> findByRegexAndResolv(@RegEx String regex, int flags) {
		return findByRegexAndResolv(Pattern.compile(regex, flags));
	}

	@Nonnull
	public Future<List<Channel>> findByRegexAndResolv(Pattern pattern) {
		Predicate<String> predicate = pattern.asPredicate();
		return io.chainFuture(list(),
				l -> l.stream()
						.filter(c -> predicate.test(c.getName()))
						.collect(Collectors.toList()));
	}

	/**
	 * Map a channel received from Teamspeak.
	 *
	 * @param data Map containing received objects
	 * @return the mapped channel
	 */
	@Override
	protected Channel readEntity(Map<String, String> data) {

		return new Channel(this,
				Integer.parseInt(data.get("cid")),
				Integer.parseInt(data.get("channel_order")),
				unresolvedOrNull(Integer.parseInt(data.get("pid"))),
				data.get("channel_name"), data.get("channel_topic"),
				data.get("channel_flag_password").equals("1"),
				// Not sure why this one is missing with `channelinfo`
				Integer.parseInt(data.getOrDefault(
						"channel_needed_subscribe_power", "0")),
				Integer.parseInt(data.get("channel_needed_talk_power")),
				data.get("channel_flag_default").equals("1"),
				data.get("channel_flag_permanent").equals("1"),
				Integer.parseInt(data.get("channel_icon_id")),
				// These are missing with `channelinfo`
				Integer.parseInt(
						data.getOrDefault("total_clients_family", "0")),
				Integer.parseInt(
						data.getOrDefault("channel_maxfamilyclients", "0")),
				Integer.parseInt(data.get("channel_maxclients")),
				Integer.parseInt(data.getOrDefault("total_clients", "0")),
				data.get("channel_flag_semi_permanent").equals("1"),
				Channel.Codec.getById(
						Integer.parseInt(data.get("channel_codec"))),
				Integer.parseInt(data.get("channel_codec_quality")));
	}

	/**
	 * Map a channel received from Teamspeak.
	 *
	 * @param data Map containing received objects
	 * @return the mapped channel
	 */
	@Nonnull
	protected UnresolvedChannelWithName readEntityNamed(Map<String, String> data) {
		return new UnresolvedChannelWithName(this,
				Integer.parseInt(data.get("cid")),
				data.get("channel_name"));
	}

	@Nonnull
	private List<Channel> mapChannelParents(List<Channel> list) {
		Map<Integer, Channel> map = list.stream()
				.collect(Collectors.toMap(this::getId, Function.identity()));
		for (Channel c : list) {
			UnresolvedChannel parentUnresolved = c.getParent();
			if (parentUnresolved == null) {
				continue;
			}
			Channel parent = map.get(parentUnresolved.getId());
			if (parent != null) {
				c.replaceParentReference(parent);
			}
		}
		return list;
	}

}
