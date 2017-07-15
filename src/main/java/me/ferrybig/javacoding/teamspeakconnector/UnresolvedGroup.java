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
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.util.concurrent.Future;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

public class UnresolvedGroup implements Resolvable<Group> {

	protected final TeamspeakConnection con;
	protected volatile boolean outdated = false;

	private final int serverGroupId;

	public UnresolvedGroup(TeamspeakConnection con, int serverGroupId) {
		this.con = con;
		this.serverGroupId = serverGroupId;
	}

	public int getServerGroupId() {
		return serverGroupId;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + "serverGroupId=" + serverGroupId + '}';
	}

	@Override
	public Future<Group> forceResolve() {
		return con.getGroupById(serverGroupId);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	public Future<? extends UnresolvedUser> addUser(UnresolvedUser user) {
		return user.addToGroup(this);
	}

	public Future<? extends UnresolvedUser> removeUser(UnresolvedUser user) {
		return user.removeFromGroup(this);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + this.serverGroupId;
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
	 * @param newName Newname of the group
	 * @return a future for this group
	 */
	public Future<? extends UnresolvedGroup> rename(String newName) {
		return con.io().chainFuture(con.io().sendPacket(new ComplexRequestBuilder("servergrouprename").addData("sgid", this.getServerGroupId()).addData("name", newName).build()), (r) -> {
			outdated = true;
			return this;
		});
	}

}
