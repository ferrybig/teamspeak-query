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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.repository.GroupRepository;

public class UnresolvedGroup implements Resolvable<Group> {

	protected final GroupRepository repo;
	// protected volatile boolean outdated = false;

	private final int serverGroupId;

	public UnresolvedGroup(GroupRepository repo, int serverGroupId) {
		this.repo = Objects.requireNonNull(repo, "repo");
		this.serverGroupId = serverGroupId;
	}

	/**
	 * Returns the server group id associated with the instance.
	 *
	 * @return the server group id.
	 */
	public int getServerGroupId() {
		return serverGroupId;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{"
				+ "serverGroupId=" + serverGroupId + '}';
	}

	@Override
	public Future<Group> forceResolve() {
		return repo.get(this, true);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	/**
	 * Adds a user to this group. This internally routes to
	 * {@link UnresolvedUser#addToGroup(UnresolvedGroup)}, but is included here
	 * for utility purposes
	 *
	 * @param user user to add to the group
	 * @return the modified user
	 */
	@Deprecated
	public Future<? extends UnresolvedUser> addUser(UnresolvedUser user) {
		return user.addToGroup(this);
	}

	/**
	 * Removes a user from this group. This internally routes to
	 * {@link UnresolvedUser#removeFromGroup(UnresolvedGroup)}, but is included
	 * here for utility purposes
	 *
	 * @param user user to remove from the group
	 * @return the modified user
	 */
	@Deprecated
	public Future<? extends UnresolvedUser> removeUser(UnresolvedUser user) {
		return user.removeFromGroup(this);
	}

	@Override
	public final int hashCode() {
		int hash = 5;
		hash = 89 * hash + this.serverGroupId;
		return hash;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UnresolvedGroup)) {
			return false;
		}
		final UnresolvedGroup other = (UnresolvedGroup) obj;
		if (this.serverGroupId != other.serverGroupId) {
			return false;
		}
		return true;
	}

	/**
	 * Renames this group to a different name
	 *
	 * @param newName Newname of the group
	 * @return a future for this group
	 */
	public Future<? extends UnresolvedGroup> rename(String newName) {
		return repo.getConnection().io().chainFuture(
				repo.getConnection().io().sendPacket(
						Command.SERVER_GROUP_RENAME
								.addData("sgid", this.getServerGroupId())
								.addData("name", newName).build()),
				(r) -> {
					return this;
				});
	}

	/**
	 * Requests a new privilege token for this group
	 *
	 * @return a string wrapped in a future containing the privilege key data
	 */
	public Future<PrivilegeKey> generatePrivilegeKey() {
		return generatePrivilegeKey("", Collections.emptyMap());
	}

	/**
	 * Requests a new privilege token for this group
	 *
	 * @param description description of this token
	 * @param customData custom data to be added to the privilege token
	 * @return a PrivilegeKey wrapped in a future containing the privilege key
	 * data
	 */
	public Future<PrivilegeKey> generatePrivilegeKey(String description,
			Map<String, String> customData) {
		return new PrivilegeKeyTemplate(customData, description,
				PrivilegeKey.Type.SERVER_GROUP, serverGroupId, 0)
				.createKey(repo.getConnection());
	}

	/**
	 * Requests a new privilege token for this group
	 *
	 * @return a string wrapped in a future containing the privilege token
	 * @deprecated Use {@code generatePrivilegeKey} instead
	 */
	@Deprecated
	public Future<String> generatePrivilegeToken() {
		return repo.getConnection().io().chainFuture(generatePrivilegeKey(), l -> l.getToken());
	}

	/**
	 * Requests a new privilege token for this group
	 *
	 * @param description description of this token
	 * @param customData custom data to be added to the privilege token
	 * @return a string wrapped in a future containing the privilege token
	 * @deprecated Use {@code generatePrivilegeKey} instead
	 */
	@Deprecated
	public Future<String> generatePrivilegeToken(String description,
			Map<String, String> customData) {
		return repo.getConnection().io().chainFuture(
				generatePrivilegeKey(description, customData),
				l -> l.getToken());
	}
}
