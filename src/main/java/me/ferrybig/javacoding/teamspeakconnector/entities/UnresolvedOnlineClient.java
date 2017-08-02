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

import me.ferrybig.javacoding.teamspeakconnector.repository.AbstractResolvable;
import me.ferrybig.javacoding.teamspeakconnector.repository.OnlineClientRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class UnresolvedOnlineClient extends AbstractResolvable<
		UnresolvedOnlineClient, OnlineClient, OnlineClientRepository> {

	private final int clientId;

	public UnresolvedOnlineClient(OnlineClientRepository repo, int clientId) {
		super(repo);
		this.clientId = clientId;
	}

	public int getClientId() {
		return clientId;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.clientId;
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
		if (!(obj instanceof UnresolvedOnlineClient)) {
			return false;
		}
		final UnresolvedOnlineClient other = (UnresolvedOnlineClient) obj;
		if (this.clientId != other.clientId) {
			return false;
		}
		return true;
	}

}
