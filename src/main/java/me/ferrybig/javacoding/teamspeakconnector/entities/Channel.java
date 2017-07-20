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
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

public class Channel extends NamedChannel {

	private final int order;
	private UnresolvedChannel parent;
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
		super(con, cid, name);
		this.order = order;
		this.parent = parent;
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

	public void setParent(UnresolvedChannel parent) {
		this.parent = parent; // TODO: make package private
	}

	@Override
	public Future<Channel> resolv() {
		return con.io().getChannel().eventLoop().newSucceededFuture(this);
	}

	@Override
	public String toString() {
		return "Channel{" + "id=" + getId() + ", order=" + order + ", parent=" + parent + ", name=" + getName() + ", topic=" + topic + ", password=" + password + ", neededSubscribePower=" + neededSubscribePower + ", neededTalkPower=" + neededTalkPower + ", defaultChannel=" + defaultChannel + ", permanent=" + permanent + ", iconId=" + iconId + ", totalClientsFamily=" + totalClientsFamily + ", maxFamilyClients=" + maxFamilyClients + ", maxClients=" + maxClients + ", totalClients=" + totalClients + ", semiPermanent=" + semiPermanent + ", codec=" + codec + ", codecQuality=" + codecQuality + '}';
	}

}
