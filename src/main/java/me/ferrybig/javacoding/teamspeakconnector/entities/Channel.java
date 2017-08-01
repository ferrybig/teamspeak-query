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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.repository.ChannelRepository;

/**
 * A Teamspeak channel, a channel has an optional parent channel, and a required
 * name.
 */
@Nonnull
@ThreadSafe
@ParametersAreNonnullByDefault
public class Channel extends UnresolvedChannelWithName {

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
	private final Codec codec;
	private final int codecQuality;

	/**
	 * Creates a Teamspeak channel object
	 *
	 * @param repo connection that created this object
	 * @param cid id of this channel
	 * @param order Sorting order of this channel
	 * @param parent Parent channel, may be null
	 * @param name Name of this channel
	 * @param topic Channel topic
	 * @param password has this channel a password
	 * @param neededSubscribePower Needed power to see the clients inside the
	 * channel
	 * @param neededTalkPower Needed power to let your voice be heard in this
	 * channel
	 * @param defaultChannel Is this channel the default channel on the server
	 * @param permanent If this channel survives a restart
	 * @param iconId The id of the icon of this channel
	 * @param totalClientsFamily total people joined in this channel group
	 * family
	 * @param maxFamilyClients Maximum people allowed in this channel group
	 * family
	 * @param maxClients Maximum people allowed in this channel
	 * @param totalClients total clients joined in this channel
	 * @param semiPermanent If this channel survives being left empty
	 * @param codec Codec used in this channel
	 * @param codecQuality Codec quality
	 */
	public Channel(ChannelRepository repo, int cid, int order,
			@Nullable UnresolvedChannel parent, String name, String topic,
			boolean password, int neededSubscribePower, int neededTalkPower,
			boolean defaultChannel, boolean permanent, int iconId,
			int totalClientsFamily, int maxFamilyClients, int maxClients,
			int totalClients, boolean semiPermanent, Codec codec,
			int codecQuality) {
		super(repo, cid, name);
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

	@CheckForNull
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

	public Codec getCodec() {
		return codec;
	}

	public int getCodecQuality() {
		return codecQuality;
	}

	public void replaceParentReference(@Nonnull UnresolvedChannel parent) {
		if (!this.parent.equals(parent)
				|| this.parent.hashCode() != parent.hashCode()) {
			throw new IllegalArgumentException(
					"changed parents don't have the same equality: " + this.parent + " vs " + parent);
		}
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "Channel{" + "id=" + getId() + ", order=" + order
				+ ", parent=" + parent + ", name=" + getName()
				+ ", topic=" + topic + ", password=" + password
				+ ", neededSubscribePower=" + neededSubscribePower
				+ ", neededTalkPower=" + neededTalkPower
				+ ", defaultChannel=" + defaultChannel
				+ ", permanent=" + permanent + ", iconId=" + iconId
				+ ", totalClientsFamily=" + totalClientsFamily
				+ ", maxFamilyClients=" + maxFamilyClients
				+ ", maxClients=" + maxClients
				+ ", totalClients=" + totalClients
				+ ", semiPermanent=" + semiPermanent
				+ ", codec=" + codec + ", codecQuality=" + codecQuality + '}';
	}

	public boolean isSpacer() {
		return getName().startsWith("[spacer") && getName().indexOf(']') > 0;
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public enum Codec {
		/**
		 * speex narrowband (mono, 16bit, 8kHz)
		 */
		SPEEX_NARROWBAND(0, false, 16, 8),
		/**
		 * speex wideband (mono, 16bit, 16kHz)
		 */
		SPEEX_WIDEBAND(1, false, 16, 16),
		/**
		 * speex ultra-wideband (mono, 16bit, 32kHz)
		 */
		SPEEX_ULTRAWIDEBAND(2, false, 16, 32),
		/**
		 * celt mono (mono, 16bit, 48kHz)
		 */
		CELT_MONO(3, false, 16, 48),
		OPUS_VOICE(4, false, 0, 0),
		OPUS_MUSIC(5, false, 0, 0);

		private static final Codec[] BY_ID;

		static {
			Codec[] values = values();
			BY_ID = new Codec[values.length];
			for (Codec type : values()) {
				BY_ID[type.id] = type;
			}
		}
		private final int id;
		private final boolean sterio;
		private final int bits;
		private final int bitrate;

		private Codec(int id, boolean sterio, int bits, int bitrate) {
			this.id = id;
			this.sterio = sterio;
			this.bits = bits;
			this.bitrate = bitrate;
		}

		/**
		 * Gets the internal id of the type
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Does this codec provide sterio output
		 *
		 * @return Does this codec provide sterio output
		 */
		public boolean isSterio() {
			return sterio;
		}

		/**
		 * Gets the sampling bit depth when recording
		 *
		 * @return The sampling bit depth when recording
		 */
		public int getBits() {
			return bits;
		}

		/**
		 * Gets the bitrate in kHz this codec provides
		 *
		 * @return the bitrate in kHz this codec provides
		 */
		public int getBitrate() {
			return bitrate;
		}

		@Override
		public String toString() {
			return String.valueOf(id);
		}

		/**
		 * Gets a type y its id
		 *
		 * @param id the id to look for
		 * @return the type that matches the id
		 * @throws IllegalArgumentException if the id isn't mapped to a type
		 */
		public static Codec getById(int id) {
			if (id >= BY_ID.length || id < 0 || BY_ID[id] == null) {
				throw new IllegalArgumentException("No type found for id " + id);
			}
			return BY_ID[id];
		}

	}
}
