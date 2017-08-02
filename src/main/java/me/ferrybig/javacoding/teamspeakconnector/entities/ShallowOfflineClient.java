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
import me.ferrybig.javacoding.teamspeakconnector.repository.OfflineClientRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class ShallowOfflineClient extends UnresolvedOfflineClient implements UnresolvedOfflineClientWithUid {

	protected final String uniqueIdentifier;
	protected final String nickname;
	protected final long created;
	protected final InetAddress lastIp;

	public ShallowOfflineClient(OfflineClientRepository repo,
			int databaseId,
			String uniqueIdentifier,
			String nickname,
			long created,
			InetAddress lastIp) {
		super(repo, databaseId);
		this.uniqueIdentifier = uniqueIdentifier;
		this.nickname = nickname;
		this.created = created;
		this.lastIp = lastIp;
	}

	@Override
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public String getNickname() {
		return nickname;
	}

	public long getCreated() {
		return created;
	}

	public InetAddress getLastIp() {
		return lastIp;
	}

	@Override
	public Future<UnresolvedOfflineClient> resolveTillDatabaseId() {
		return repo.getConnection().io().getCompletedFuture(this);
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
