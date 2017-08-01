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
public class OfflineClient extends UnresolvedOfflineClient implements UnresolvedOfflineClientWithUid {

	private final String uniqueIdentifier;
	private final String nickname;
	private final long created;
	private final long lastConnected;
	private final int totalConnections;
	private final String description;
	private final InetAddress lastIp;

	public OfflineClient(OfflineClientRepository repo,
			int databaseId,
			String uniqueIdentifier,
			String nickname,
			long created,
			long lastConnected,
			int totalConnections,
			String description,
			InetAddress lastIp) {
		super(repo, databaseId);
		this.uniqueIdentifier = uniqueIdentifier;
		this.nickname = nickname;
		this.created = created;
		this.lastConnected = lastConnected;
		this.totalConnections = totalConnections;
		this.description = description;
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

	public long getLastConnected() {
		return lastConnected;
	}

	public int getTotalConnections() {
		return totalConnections;
	}

	public String getDescription() {
		return description;
	}

	public InetAddress getLastIp() {
		return lastIp;
	}

	@Override
	public Future<UnresolvedOfflineClient> resolveTillDatabaseId() {
		return repo.getConnection().io().getCompletedFuture(this);
	}

}
