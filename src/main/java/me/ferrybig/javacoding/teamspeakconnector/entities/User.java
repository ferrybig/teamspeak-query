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
import java.net.InetAddress;
import java.util.List;
import me.ferrybig.javacoding.teamspeakconnector.ClientType;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

public class User extends ShallowUser {

	protected final boolean talking;
	protected final String version;
	protected final String platform;
	protected final int idleTime;
	protected final long created;
	protected final long lastConnected;
	protected final InetAddress ip;

	public User(TeamspeakConnection con, int id, UnresolvedChannel channel, int databaseId, String nickname,
			ClientType type, String awayMessage,
			boolean talking, boolean inputMuted, boolean outputMuted, boolean inputHardware, boolean outputHardware,
			int talkPower, boolean talker, boolean prioritySpeaker, boolean recording, boolean channelCommander,
			String uniqueIdentifier, List<UnresolvedGroup> serverGroup, List<UnresolvedChannelGroup> channelGroup,
			UnresolvedChannel channelGroupInherited, String version, String platform, int idleTime, long created, long lastConnected, int iconId, String country, InetAddress ip) {
		super(con, id, uniqueIdentifier, databaseId, channel, nickname, type, awayMessage, inputMuted, outputMuted, inputHardware, outputHardware, talkPower, talker, prioritySpeaker, recording, channelCommander, serverGroup, channelGroup, channelGroupInherited, iconId, country);
		this.talking = talking;
		this.version = version;
		this.platform = platform;
		this.idleTime = idleTime;
		this.created = created;
		this.lastConnected = lastConnected;
		this.ip = ip;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + this.getDatabaseId();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if (this.getDatabaseId() != other.getDatabaseId()) {
			return false;
		}
		return true;
	}

	public boolean isTalking() {
		return talking;
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

	public long getCreated() {
		return created;
	}

	public long getLastConnected() {
		return lastConnected;
	}

	public InetAddress getIp() {
		return ip;
	}

	public boolean hasIp() {
		return ip != null;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public Future<User> resolve() {
		return this.con.io().getCompletedFuture(this);
	}

	@Override
	public String toString() {
		return "User{" + "id=" + getId() + ",channel=" + channel + ", databaseId=" + databaseId + ", nickname=" + getNickname() + ", type=" + type + ", awayMessage=" + awayMessage + ", talking=" + talking + ", inputMuted=" + inputMuted + ", outputMuted=" + outputMuted + ", inputHardware=" + inputHardware + ", outputHardware=" + outputHardware + ", talkPower=" + talkPower + ", talker=" + talker + ", prioritySpeaker=" + prioritySpeaker + ", recording=" + recording + ", channelCommander=" + channelCommander + ", uniqueIdentifier=" + getUniqueId() + ", serverGroup=" + serverGroup + ", channelGroup=" + channelGroup + ", channelGroupInherited=" + channelGroupInherited + ", version=" + version + ", platform=" + platform + ", idleTime=" + idleTime + ", created=" + created + ", lastConnected=" + lastConnected + ", iconId=" + iconId + ", country=" + country + ", ip=" + ip + '}';
	}

}
