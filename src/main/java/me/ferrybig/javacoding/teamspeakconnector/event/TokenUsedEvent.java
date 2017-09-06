/*
 * The MIT License
 *
 * Copyright 2017 Fernando.
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

import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedOnlineClient;

public class TokenUsedEvent {

	private final UnresolvedOnlineClient client;
	private final int databaseId;
	private final String uniqueId;
	private final PrivilegeKey key;

	public TokenUsedEvent(UnresolvedOnlineClient client, int databaseId,
			String uniqueId, PrivilegeKey key) {
		this.client = client;
		this.databaseId = databaseId;
		this.uniqueId = uniqueId;
		this.key = key;
	}

	public UnresolvedOnlineClient getClient() {
		return client;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public PrivilegeKey getKey() {
		return key;
	}

	@Deprecated
	public String getToken() {
		return key.getToken();
	}

	@Deprecated
	public String getTokencustomset() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public String getToken1() {
		return String.valueOf(key.getToken1());
	}

	@Deprecated
	public String getToken2() {
		return String.valueOf(key.getToken2());
	}

	@Override
	public String toString() {
		return "TokenUsedEvent{" + "client=" + client
				+ ", databaseId=" + databaseId + ", uniqueId=" + uniqueId
				+ ", key=" + key + '}';
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + Objects.hashCode(this.client);
		hash = 47 * hash + this.databaseId;
		hash = 47 * hash + Objects.hashCode(this.uniqueId);
		hash = 47 * hash + Objects.hashCode(this.key);
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
		final TokenUsedEvent other = (TokenUsedEvent) obj;
		if (this.databaseId != other.databaseId) {
			return false;
		}
		if (!Objects.equals(this.uniqueId, other.uniqueId)) {
			return false;
		}
		if (!Objects.equals(this.key, other.key)) {
			return false;
		}
		if (!Objects.equals(this.client, other.client)) {
			return false;
		}
		return true;
	}

}
