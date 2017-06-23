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
import me.ferrybig.javacoding.teamspeakconnector.UnresolvedUser;

public class TokenUsedEvent {

	private final UnresolvedUser client;
	private final int databaseId;
	private final String uniqueId;
	private final String token;
	private final String tokencustomset;
	private final String token1;
	private final String token2; // TODO figure out what there options mean

	public TokenUsedEvent(UnresolvedUser client, int databaseId, String uniqueId, String token, String tokencustomset, String token1, String token2) {
		this.client = client;
		this.databaseId = databaseId;
		this.uniqueId = uniqueId;
		this.token = token;
		this.tokencustomset = tokencustomset;
		this.token1 = token1;
		this.token2 = token2;
	}

	public UnresolvedUser getClient() {
		return client;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public String getToken() {
		return token;
	}

	public String getTokencustomset() {
		return tokencustomset;
	}

	public String getToken1() {
		return token1;
	}

	public String getToken2() {
		return token2;
	}

	@Override
	public String toString() {
		return "TokenUsedEvent{" + "client=" + client + ", databaseId=" + databaseId + ", uniqueId=" + uniqueId + ", token=" + token + ", tokencustomset=" + tokencustomset + ", token1=" + token1 + ", token2=" + token2 + '}';
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + Objects.hashCode(this.client);
		hash = 47 * hash + this.databaseId;
		hash = 47 * hash + Objects.hashCode(this.uniqueId);
		hash = 47 * hash + Objects.hashCode(this.token);
		hash = 47 * hash + Objects.hashCode(this.tokencustomset);
		hash = 47 * hash + Objects.hashCode(this.token1);
		hash = 47 * hash + Objects.hashCode(this.token2);
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
		if (!Objects.equals(this.token, other.token)) {
			return false;
		}
		if (!Objects.equals(this.tokencustomset, other.tokencustomset)) {
			return false;
		}
		if (!Objects.equals(this.token1, other.token1)) {
			return false;
		}
		if (!Objects.equals(this.token2, other.token2)) {
			return false;
		}
		if (!Objects.equals(this.client, other.client)) {
			return false;
		}
		return true;
	}

}
