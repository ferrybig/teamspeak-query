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

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import me.ferrybig.javacoding.teamspeakconnector.repository.OnlineClientRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class OnlineClient extends UnresolvedOnlineClient {

	private final UnresolvedChannel channel;
	private final OnlineClient.Type type;
	private final String awayMessage;
	private final boolean talking;
	private final boolean inputMuted;
	private final boolean outputMuted;
	private final boolean inputHardware;
	private final boolean outputHardware;
	private final int talkPower;
	private final boolean talker;
	private final boolean prioritySpeaker;
	private final boolean recording;
	private final boolean channelCommander;
	private final List<UnresolvedGroup> serverGroup;
	private final List<UnresolvedChannelGroup> channelGroup;
	private final UnresolvedChannel channelGroupInherited;
	private final String version;
	private final String platform;
	private final int idleTime;
	private final int iconId;
	private final String country;
	private final ShallowOfflineClient offlineClient;

	/*
	 * int databaseId, String nickname,String uniqueIdentifier, long created, long lastConnected, InetAddress ip
	 */
	public OnlineClient(OnlineClientRepository repo, int id,
			UnresolvedChannel channel, OnlineClient.Type type,
			@Nullable String awayMessage,
			boolean talking, boolean inputMuted, boolean outputMuted,
			boolean inputHardware, boolean outputHardware,
			int talkPower, boolean talker, boolean prioritySpeaker,
			boolean recording, boolean channelCommander,
			List<UnresolvedGroup> serverGroup,
			List<UnresolvedChannelGroup> channelGroup,
			UnresolvedChannel channelGroupInherited, String version,
			String platform, int idleTime,
			int iconId, String country, ShallowOfflineClient offline
	) {
		super(repo, id);
		this.channel = Objects.requireNonNull(channel, "channel");
		this.type = Objects.requireNonNull(type, "type");
		this.awayMessage = awayMessage;
		this.talking = talking;
		this.inputMuted = inputMuted;
		this.outputMuted = outputMuted;
		this.inputHardware = inputHardware;
		this.outputHardware = outputHardware;
		this.talkPower = talkPower;
		this.talker = talker;
		this.prioritySpeaker = prioritySpeaker;
		this.recording = recording;
		this.channelCommander = channelCommander;
		this.serverGroup = Objects.requireNonNull(serverGroup, "serverGroup");
		this.channelGroup
				= Objects.requireNonNull(channelGroup, "channelGroup");
		this.channelGroupInherited
				= Objects.requireNonNull(
						channelGroupInherited, "channelGroupInherited");
		this.version = Objects.requireNonNull(version, "version");
		this.platform = Objects.requireNonNull(platform, "platform");
		this.idleTime = idleTime;
		this.iconId = iconId;
		this.country = Objects.requireNonNull(country, "country");
		this.offlineClient = Objects.requireNonNull(offline, "offline");
	}

	public ShallowOfflineClient getOfflineClient() {
		return offlineClient;
	}

	public String getUniqueIdentifier() {
		return offlineClient.getUniqueIdentifier();
	}

	public String getNickname() {
		return offlineClient.getNickname();
	}

	public long getCreated() {
		return offlineClient.getCreated();
	}

	public InetAddress getLastIp() {
		return offlineClient.getLastIp();
	}

	public int getDatabaseId() {
		return offlineClient.getDatabaseId();
	}

	public UnresolvedChannel getChannel() {
		return channel;
	}

	public Type getType() {
		return type;
	}

	@Nullable
	public String getAwayMessage() {
		return awayMessage;
	}

	public boolean isAway() {
		return awayMessage != null;
	}

	public boolean isTalking() {
		return talking;
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
		return Collections.unmodifiableList(serverGroup);
	}

	public List<UnresolvedChannelGroup> getChannelGroup() {
		return Collections.unmodifiableList(channelGroup);
	}

	public UnresolvedChannel getChannelGroupInherited() {
		return channelGroupInherited;
	}

	public String getVersion() {
		return version;
	}

	public String getPlatform() {
		return platform;
	}

	public int getIdleTime() {
		return idleTime;
	}

	public int getIconId() {
		return iconId;
	}

	public String getCountry() {
		return country;
	}

	/*
	Missing:
			int totalConnections,
			String description,
			long lastConnected,
	 */
	public static enum Type {
		NORMAL, QUERY;

		public static Type getById(int id) {
			switch (id) {
				case 0:
					return NORMAL;
				case 1:
					return QUERY;
				default:
					throw new IllegalArgumentException(
							"ClientType " + id + " not known");
			}
		}
	}

	@Override
	public boolean isResolved() {
		return true;
	}
}
