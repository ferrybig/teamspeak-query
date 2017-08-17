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
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.repository.ChannelRepository;

@Immutable
public class UnresolvedChannel implements Resolvable<Channel> {

	protected final ChannelRepository repo;

	private final int id;

	public UnresolvedChannel(ChannelRepository repo, int id) {
		this.repo = Objects.requireNonNull(repo, "repo");
		this.id = id;
	}

	@Override
	public Future<Channel> forceResolve() {
		return repo.get(this, true);
	}

	@Override
	public Future<Channel> resolve() {
		return repo.get(this);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "UnresolvedChannel{" + "id=" + id + '}';
	}

	public Future<List<File>> getFileTransferList() {
		return getFileTransferList("/");
	}

	public Future<List<File>> getFileTransferList(String path) {
		return repo.getConnection().mapping().mapComplexReponseList(repo.getConnection().io().sendPacket(
				Command.FT_GET_FILE_LIST.addData("cid",
						getId()).addData("path", path).build()),
				repo.getConnection().mapping()::mapFile);
	}

	public Future<?> moveInto(UnresolvedUser user) {
		return repo.getConnection().io().sendPacket(Command.CLIENT_MOVE
				.addData("cid", getId()).addData("clid", user.getId())
				.build());
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public final int hashCode() {
		int hash = 7;
		hash = 17 * hash + this.id;
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
		if (!(obj instanceof UnresolvedChannel)) {
			return false;
		}
		final UnresolvedChannel other = (UnresolvedChannel) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}

}
