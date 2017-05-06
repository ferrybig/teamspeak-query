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
package me.ferrybig.javacoding.teamspeakconnector.event;

/**
 *
 * @author Fernando van Loenhout
 */
public enum ClientChannelChangeReason {
	INDEPENDENT(0, true, true),
	USER_CHANNEL_CHANGE(1, false, true),
	TIMEOUT(3, true, true),
	CHANNEL_KICK(4, false, true),
	SERVER_KICK(5, true, true),
	BAN(6, true, true),
	LEAVE_SERVER(8, true, true),
	SERER_CHANNEL_CHANGE(10, false, true),
	SERVER_SHUTDOWN(11, true, true);

	private final int id;
	private final boolean serverEvent;
	private final boolean channelEvent;

	private ClientChannelChangeReason(int id, boolean serverEvent, boolean channelEvent) {
		this.id = id;
		this.serverEvent = serverEvent;
		this.channelEvent = channelEvent;
	}

	public int getId() {
		return id;
	}

	public boolean isServerEvent() {
		return serverEvent;
	}

	public boolean isChannelEvent() {
		return channelEvent;
	}
	private static final ClientChannelChangeReason[] reasons;

	static {
		reasons = new ClientChannelChangeReason[16];
		for (ClientChannelChangeReason reason : ClientChannelChangeReason.values()) {
			reasons[reason.getId()] = reason;
		}
	}

	public static ClientChannelChangeReason getById(int id) {
		if (id > reasons.length) {
			throw new IllegalArgumentException("Invalid reason id: " + id);
		}
		ClientChannelChangeReason r = reasons[id];
		if (r == null) {
			throw new IllegalArgumentException("Invalid reason id: " + id);
		}
		return r;
	}

}
