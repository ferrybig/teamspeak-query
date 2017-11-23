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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;

/**
 * An abstract class for easy integration when resolving with repositories
 * @param <S> Result before resolving
 * @param <T> Result after resolving
 * @param <R> Repository
 */
@ParametersAreNonnullByDefault
public abstract class AbstractResolvable<
		S extends Resolvable<? extends T>, T extends S, R extends BaseResolvableRepository<S, T>>
		implements Resolvable<T> {

	/**
	 * Upstream repository for this entity
	 */
	@Nonnull
	protected final R repo;

	/**
	 * Creates a AbstractResolvable
	 * @param repo the repository that created this
	 * @throws NullPointerException when repo == null
	 */
	public AbstractResolvable(R repo) {
		this.repo = Objects.requireNonNull(repo, "repo");
	}

	/**
	 * Returns the resolve status, false for this class
	 * @return false
	 */
	@Override
	public boolean isResolved() {
		return false;
	}

	/**
	 * Resolves this object, or returns itself when its already resolved.
	 * @return A resolved version of itself wrapped in a future
	 */
	@Override
	@Nonnull
	@SuppressWarnings("unchecked")
	public Future<T> resolve() {
		if (isResolved()) {
			return repo.getConnection().io().getCompletedFuture((T) this);
		}
		return repo.get((S) this);
	}

	/**
	 * Forcefully resolves this object.
	 * @return A resolved version of itself wrapped in a future
	 */
	@Override
	@Nonnull
	@SuppressWarnings("unchecked")
	public Future<T> forceResolve() {
		return repo.get((S) this, true);
	}

	/**
	 * Schedules deletion for this object.
	 * @param force forcefully delete this
	 * @return The result of the deletion wrapped in a future
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public Future<?> delete(boolean force) {
		return repo.deleteUnresolved((S) this, force);
	}

}
