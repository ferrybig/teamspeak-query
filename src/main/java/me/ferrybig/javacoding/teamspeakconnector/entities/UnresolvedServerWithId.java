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
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.repository.ServerRepository;

public class UnresolvedServerWithId implements UnresolvedServer {

	protected final ServerRepository repo;
	private final int sid;

	public UnresolvedServerWithId(ServerRepository repo, int sid) {
		this.repo = repo;
		this.sid = sid;
	}

	public int getSid() {
		return sid;
	}

	@Override
	public Future<TeamspeakConnection> select() {
		return repo.getConnection().io().chainFuture(
				repo.getConnection().io().sendPacket(Command.USE
						.addData("sid", String.valueOf(sid))
						.addOption("virtual")
						.build()),
				ignored -> repo.getConnection()).addListener(future -> {
					if (future.isSuccess()) {
						repo.getConnection().io().notifyServerChanged();
					}
				});
	}

	@Override
	public Future<TeamspeakConnection> stop() {
		return repo.getConnection().io().chainFuture(
				repo.getConnection().io().sendPacket(Command.SERVER_STOP
						.addData("sid", sid).build()),
				ignored -> repo.getConnection());
	}

	@Override
	public Future<TeamspeakConnection> start() {
		return repo.getConnection().io().chainFuture(
				repo.getConnection().io().sendPacket(Command.SERVER_START
						.addData("sid", sid).build()),
				ignored -> repo.getConnection());
	}

	@Override
	public Future<Server> forceResolve() {
		return repo.get(this, true);
	}

	@Override
	public Future<Server> resolve() {
		return repo.get(this);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public final int hashCode() {
		int hash = 5;
		hash = 89 * hash + this.sid;
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
		if (!(obj instanceof UnresolvedServerWithId)) {
			return false;
		}
		final UnresolvedServerWithId other = (UnresolvedServerWithId) obj;
		if (this.sid != other.sid) {
			return false;
		}
		return true;
	}
}
