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
import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.repository.PrivilegeKeyRepository;

/**
 *
 * @author Fernando van Loenhout
 */
public class UnresolvedPrivilegeKey implements Resolvable<PrivilegeKey> {

	protected final PrivilegeKeyRepository repo;
	protected final String token;

	public UnresolvedPrivilegeKey(PrivilegeKeyRepository repo, String token) {
		this.repo = repo;
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	@Override
	public Future<PrivilegeKey> forceResolve() {
		return repo.get(this);
	}

	public Future<?> use() {
		return repo.getConnection().io().sendPacket(
				Command.PRIVILEGEKEY_USE
						.addData("token", token)
						.build());
	}

	public Future<?> delete() {
		return repo.deleteUnresolved(this);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + Objects.hashCode(this.token);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UnresolvedPrivilegeKey)) {
			return false;
		}
		final UnresolvedPrivilegeKey other = (UnresolvedPrivilegeKey) obj;
		if (!Objects.equals(this.token, other.token)) {
			return false;
		}
		return true;
	}

}
