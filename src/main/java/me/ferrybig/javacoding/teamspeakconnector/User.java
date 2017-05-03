/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

import java.net.InetAddress;
import java.util.List;

/**
 *
 * @author Fernando
 */

//clientlist -uid -away -voice -times -groups -info -icon -country -ip
public class User extends UnresolvedUser {

	private final UnresolvedChannel channel;
	private final int databaseId;
	private final String nickname;
	private final ClientType type;
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
	private final String uniqueIdentifier;
	private final List<UnresolvedGroup> serverGroup;
	private final List<UnresolvedChannelGroup> channelGroup;
	private final UnresolvedChannel channelGroupInherited;
	private final String version;
	private final String platform;
	private final int idleTime;
	private final long created;
	private final long lastConnected;
	private final int iconId;
	private final String country;
	private final InetAddress ip;
	
	public User(TeamspeakConnection con, int id, UnresolvedChannel channel, int databaseId, String nickname,
			ClientType type, String awayMessage, 
			boolean talking, boolean inputMuted, boolean outputMuted, boolean inputHardware, boolean outputHardware,
			int talkPower, boolean talker, boolean prioritySpeaker, boolean recording, boolean channelCommander,
			String uniqueIdentifier, List<UnresolvedGroup> serverGroup, List<UnresolvedChannelGroup> channelGroup, 
			UnresolvedChannel channelGroupInherited, String version, String platform, int idleTime, long created, long lastConnected, int iconId, String country, InetAddress ip) {
		super(con, id);
		this.channel = channel;
		this.databaseId = databaseId;
		this.nickname = nickname;
		this.type = type;
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
		this.uniqueIdentifier = uniqueIdentifier;
		this.serverGroup = serverGroup;
		this.channelGroup = channelGroup;
		this.channelGroupInherited = channelGroupInherited;
		this.version = version;
		this.platform = platform;
		this.idleTime = idleTime;
		this.created = created;
		this.lastConnected = lastConnected;
		this.iconId = iconId;
		this.country = country;
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

	/**
	 * @return the channel
	 */
	public UnresolvedChannel getChannel() {
		return channel;
	}

	/**
	 * @return the databaseId
	 */
	public int getDatabaseId() {
		return databaseId;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @return the type
	 */
	public ClientType getType() {
		return type;
	}

	/**
	 * @return the awayMessage
	 */
	public String getAwayMessage() {
		return awayMessage;
	}
	
	public boolean isAway() {
		return awayMessage != null;
	}

	/**
	 * @return the talking
	 */
	public boolean isTalking() {
		return talking;
	}

	/**
	 * @return the inputMuted
	 */
	public boolean isInputMuted() {
		return inputMuted;
	}

	/**
	 * @return the outputMuted
	 */
	public boolean isOutputMuted() {
		return outputMuted;
	}

	/**
	 * @return the inputHardware
	 */
	public boolean isInputHardware() {
		return inputHardware;
	}

	/**
	 * @return the outputHardware
	 */
	public boolean isOutputHardware() {
		return outputHardware;
	}

	/**
	 * @return the talkPower
	 */
	public int getTalkPower() {
		return talkPower;
	}

	/**
	 * @return the talker
	 */
	public boolean isTalker() {
		return talker;
	}

	/**
	 * @return the prioritySpeaker
	 */
	public boolean isPrioritySpeaker() {
		return prioritySpeaker;
	}

	/**
	 * @return the recording
	 */
	public boolean isRecording() {
		return recording;
	}

	/**
	 * @return the channelCommander
	 */
	public boolean isChannelCommander() {
		return channelCommander;
	}

	/**
	 * @return the uniqueIdentifier
	 */
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	/**
	 * @return the serverGroup
	 */
	public List<UnresolvedGroup> getServerGroup() {
		return serverGroup;
	}

	/**
	 * @return the channelGroup
	 */
	public List<UnresolvedChannelGroup> getChannelGroup() {
		return channelGroup;
	}

	/**
	 * @return the channelGroupInherited
	 */
	public UnresolvedChannel getChannelGroupInherited() {
		return channelGroupInherited;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @return the idleTime
	 */
	public int getIdleTime() {
		return idleTime;
	}

	/**
	 * @return the created
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * @return the lastConnected
	 */
	public long getLastConnected() {
		return lastConnected;
	}

	/**
	 * @return the iconId
	 */
	public int getIconId() {
		return iconId;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the ip
	 */
	public InetAddress getIp() {
		return ip;
	}
	
	public boolean hasIp() {
		return ip != null;
	}
}
