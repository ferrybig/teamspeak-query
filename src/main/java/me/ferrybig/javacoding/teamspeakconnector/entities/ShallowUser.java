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

import io.netty.util.concurrent.Future;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.ClientType;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

/**
 *
 * @author Fernando van Loenhout
 */
public class ShallowUser extends NamedUser {

	protected final int databaseId;
	protected final UnresolvedChannel channel;
	protected final UnresolvedChannel channelGroupInherited;
	protected final ClientType type;
	protected final String awayMessage;
	protected final boolean inputMuted;
	protected final boolean outputMuted;
	protected final boolean inputHardware;
	protected final boolean outputHardware;
	protected final int talkPower;
	protected final boolean talker;
	protected final boolean prioritySpeaker;
	protected final boolean recording;
	protected final boolean channelCommander;
	protected final List<UnresolvedGroup> serverGroup;
	protected final List<UnresolvedChannelGroup> channelGroup;
	protected final int iconId;
	protected final String country;

	public ShallowUser(TeamspeakConnection con, int id, String uniqueid,
			int databaseId, UnresolvedChannel channel, String nickname,
			ClientType type, String awayMessage, boolean inputMuted,
			boolean outputMuted, boolean inputHardware, boolean outputHardware,
			int talkPower, boolean talker, boolean prioritySpeaker,
			boolean recording, boolean channelCommander,
			List<UnresolvedGroup> serverGroup,
			List<UnresolvedChannelGroup> channelGroup,
			UnresolvedChannel channelGroupInherited,
			int iconId, String country) {
		super(con, id, uniqueid, nickname);
		this.databaseId = databaseId;
		this.channel = channel;
		this.type = type;
		this.awayMessage = awayMessage;
		this.inputMuted = inputMuted;
		this.outputMuted = outputMuted;
		this.inputHardware = inputHardware;
		this.outputHardware = outputHardware;
		this.talkPower = talkPower;
		this.talker = talker;
		this.prioritySpeaker = prioritySpeaker;
		this.recording = recording;
		this.channelCommander = channelCommander;
		this.serverGroup = serverGroup;
		this.channelGroup = channelGroup;
		this.channelGroupInherited = channelGroupInherited;
		this.iconId = iconId;
		this.country = country;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public UnresolvedChannel getChannel() {
		return channel;
	}

	public ClientType getType() {
		return type;
	}

	public String getAwayMessage() {
		return awayMessage;
	}

	public boolean isAway() {
		return awayMessage != null;
	}

	public boolean isInputMuted() {
		return inputMuted;
	}

	public boolean isOutputMuted() {
		return outputMuted;
	}

	public boolean isInputHardware() {
		return inputHardware;
	}

	public boolean isOutputHardware() {
		return outputHardware;
	}

	public int getTalkPower() {
		return talkPower;
	}

	public boolean isTalker() {
		return talker;
	}

	public boolean isPrioritySpeaker() {
		return prioritySpeaker;
	}

	public boolean isRecording() {
		return recording;
	}

	public boolean isChannelCommander() {
		return channelCommander;
	}

	public List<UnresolvedGroup> getServerGroup() {
		return serverGroup;
	}

	public List<UnresolvedChannelGroup> getChannelGroup() {
		return channelGroup;
	}

	public int getIconId() {
		return iconId;
	}

	public String getCountry() {
		return country;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + "id=" + getId() + ",uniqueid=" + uniqueid + ", nickname=" + nickname + ", databaseId=" + databaseId + ", channel=" + channel + ", channelGroupInherited=" + channelGroupInherited + ", type=" + type + ", awayMessage=" + awayMessage + ", inputMuted=" + inputMuted + ", outputMuted=" + outputMuted + ", inputHardware=" + inputHardware + ", outputHardware=" + outputHardware + ", talkPower=" + talkPower + ", talker=" + talker + ", prioritySpeaker=" + prioritySpeaker + ", recording=" + recording + ", channelCommander=" + channelCommander + ", serverGroup=" + serverGroup + ", channelGroup=" + channelGroup + ", iconId=" + iconId + ", country=" + country + '}';
	}

	@Override
	public Future<? extends ShallowUser> removeFromGroup(UnresolvedGroup group) {
		if (!serverGroup.contains(group) && !outdated) {
			return con.io().getCompletedFuture(this);
		}
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("servergroupdelclient").addData("sgid", group.getServerGroupId()).addData("cldbid", this.databaseId).build()), (r) -> {
			outdated = true;
			return this;
		});
	}

	@Override
	public Future<? extends UnresolvedUser> addToGroup(UnresolvedGroup group) {
		if (serverGroup.contains(group) && !outdated) {
			return con.io().getCompletedFuture(this);
		}
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("servergroupaddclient").addData("sgid", group.getServerGroupId()).addData("cldbid", this.databaseId).build()), (r) -> {
			outdated = true;
			return this;
		});
	}

	public Future<Map<String, String>> getCustomInfo() {
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("custominfo").addData("cldbid", this.databaseId).build()),
			(r) -> r.getCommands().stream().collect(Collectors.toMap(k-> k.get("ident"), v -> v.get("value")))
		);
	}
}
