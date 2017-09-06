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
package me.ferrybig.javacoding.teamspeakconnector.repository;

import io.netty.util.concurrent.Future;
import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;

public abstract class AbstractResolvable<
		S extends Resolvable<? extends T>, T extends S, R extends BaseResolvableRepository<S, T>>
		implements Resolvable<T> {

	protected final R repo;

	public AbstractResolvable(R repo) {
		this.repo = Objects.requireNonNull(repo, "repo");
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Future<T> resolve() {
		if (isResolved()) {
			return repo.getConnection().io().getCompletedFuture((T) this);
		}
		return repo.get((S) this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Future<T> forceResolve() {
		return repo.get((S) this, true);
	}

	@SuppressWarnings("unchecked")
	public Future<?> delete(boolean force) {
		return repo.deleteUnresolved((S) this, force);
	}

}
