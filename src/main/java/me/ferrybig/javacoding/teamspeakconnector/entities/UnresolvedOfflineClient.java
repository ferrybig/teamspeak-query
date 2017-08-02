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
import java.util.Map;
import java.util.stream.Collectors;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.repository.OfflineClientRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class UnresolvedOfflineClient implements UnresolvedClient {

	protected final OfflineClientRepository repo;
	protected final int databaseId;

	public UnresolvedOfflineClient(OfflineClientRepository repo, int databaseId) {
		this.repo = repo;
		this.databaseId = databaseId;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	@Override
	public Future<OfflineClient> resolve() {
		return repo.get(this, false);
	}

	@Override
	public Future<OfflineClient> forceResolve() {
		return repo.get(this, true);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.databaseId;
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
		if (!(obj instanceof UnresolvedOfflineClient)) {
			return false;
		}
		final UnresolvedOfflineClient other = (UnresolvedOfflineClient) obj;
		if (this.databaseId != other.databaseId) {
			return false;
		}
		return true;
	}

	public Future<?> removeFromGroup(UnresolvedGroup group) {
		return repo.getConnection().io().sendPacket(
				Command.SERVER_GROUP_DEL_CLIENT.
						addData("sgid", group.getServerGroupId())
						.addData("cldbid", this.databaseId).build());
	}

	public Future<?> addToGroup(UnresolvedGroup group) {
		return repo.getConnection().io().sendPacket(
				Command.SERVER_GROUP_ADD_CLIENT
						.addData("sgid", group.getServerGroupId())
						.addData("cldbid", this.databaseId).build());
	}

	public Future<Map<String, String>> getCustomInfo() {
		return repo.getConnection().io().chainFuture(
				repo.getConnection().io().sendPacket(
						Command.CUSTOM_INFO
								.addData("cldbid", this.databaseId).build()),
				r -> r.getCommands().stream().collect(
						Collectors.toMap(
								k -> k.get("ident"),
								v -> v.get("value")))
		);
	}

}
