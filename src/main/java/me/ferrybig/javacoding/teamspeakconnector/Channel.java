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
public class Channel extends UnresolvedChannel {

	private final int order;
	private UnresolvedChannel parent;
	private final String name;
	private final String topic;
	private final boolean password;
	private final int neededSubscribePower;
	private final int neededTalkPower;
	private final boolean defaultChannel;
	private final boolean permanent;
	private final int iconId;
	private final int totalClientsFamily;
	private final int maxFamilyClients;
	private final int maxClients;
	private final int totalClients;
	private final boolean semiPermanent;
	private final int codec;
	private final int codecQuality;

	public Channel(TeamspeakConnection con, int cid, int order,
			UnresolvedChannel parent, String name, String topic,
			boolean password, int neededSubscribePower, int neededTalkPower,
			boolean defaultChannel, boolean permanent, int iconId,
			int totalClientsFamily, int maxFamilyClients, int maxClients,
			int totalClients, boolean semiPermanent, int codec,
			int codecQuality) {
		super(con, cid);
		this.order = order;
		this.parent = parent;
		this.name = name;
		this.topic = topic;
		this.password = password;
		this.neededSubscribePower = neededSubscribePower;
		this.neededTalkPower = neededTalkPower;
		this.defaultChannel = defaultChannel;
		this.permanent = permanent;
		this.iconId = iconId;
		this.totalClientsFamily = totalClientsFamily;
		this.maxFamilyClients = maxFamilyClients;
		this.maxClients = maxClients;
		this.totalClients = totalClients;
		this.semiPermanent = semiPermanent;
		this.codec = codec;
		this.codecQuality = codecQuality;

	}

	public int getOrder() {
		return order;
	}

	public UnresolvedChannel getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getTopic() {
		return topic;
	}

	public boolean isPassword() {
		return password;
	}

	public int getNeededSubscribePower() {
		return neededSubscribePower;
	}

	public int getNeededTalkPower() {
		return neededTalkPower;
	}

	public boolean isDefaultChannel() {
		return defaultChannel;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public int getIconId() {
		return iconId;
	}

	public int getTotalClientsFamily() {
		return totalClientsFamily;
	}

	public int getMaxFamilyClients() {
		return maxFamilyClients;
	}

	public int getMaxClients() {
		return maxClients;
	}

	public int getTotalClients() {
		return totalClients;
	}

	public boolean isSemiPermanent() {
		return semiPermanent;
	}

	public int getCodec() {
		return codec;
	}

	public int getCodecQuality() {
		return codecQuality;
	}

	void setParent(UnresolvedChannel parent) {
		this.parent = parent;
	}

	@Override
	public Future<Channel> resolv() {
		return con.io().getChannel().eventLoop().newSucceededFuture(this);
	}

	@Override
	public String toString() {
		return "Channel{" + "id=" + getId() + ", order=" + order + ", parent=" + parent + ", name=" + name + ", topic=" + topic + ", password=" + password + ", neededSubscribePower=" + neededSubscribePower + ", neededTalkPower=" + neededTalkPower + ", defaultChannel=" + defaultChannel + ", permanent=" + permanent + ", iconId=" + iconId + ", totalClientsFamily=" + totalClientsFamily + ", maxFamilyClients=" + maxFamilyClients + ", maxClients=" + maxClients + ", totalClients=" + totalClients + ", semiPermanent=" + semiPermanent + ", codec=" + codec + ", codecQuality=" + codecQuality + '}';
	}

}
