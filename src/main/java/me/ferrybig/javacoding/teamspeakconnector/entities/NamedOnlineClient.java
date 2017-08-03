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

import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.repository.OnlineClientRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class NamedOnlineClient extends UnresolvedOnlineClient {

	private final String uniqueid;
	private final String nickname;

	public NamedOnlineClient(OnlineClientRepository repo, int clientId, String uniqueid,
			String nickname) {
		super(repo, clientId);
		this.uniqueid = Objects.requireNonNull(uniqueid, "uniqueid");
		this.nickname = Objects.requireNonNull(nickname, "nickname");
	}

	public UnresolvedOfflineClientWithUid getOfflineClient() {
		return repo.getConnection().offlineClients().unresolvedByUniqueId(uniqueid);
	}

	public String getUniqueIdentifier() {
		return uniqueid;
	}

	public String getNickname() {
		return nickname;
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

}
