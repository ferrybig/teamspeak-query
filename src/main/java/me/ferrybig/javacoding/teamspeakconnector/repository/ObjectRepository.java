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
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Repository that allows its objects to be exposed over any object as key keys
 *
 * @param <T> class that this repository contains
 * @param <I> Id representing the key of the types
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public interface ObjectRepository<T, I> extends BaseRepository<T> {

	/**
	 * Deletes an object by its id
	 *
	 * @param id id to delete
	 * @return a future containing the deletion result
	 */
	@Nonnull
	default Future<?> deleteById(I id) {
		return deleteById(id, false);
	}

	/**
	 * Deletes an object by its id, optionally with force
	 *
	 * @param id id to delete
	 * @param force should deletion be forced, this usually means bypassing any
	 * in use status if this object has that kind of status
	 * @return a future containing the deletion result
	 */
	@Nonnull
	Future<?> deleteById(I id, boolean force);

	/**
	 * Gets an object by its id
	 *
	 * @param id id to get
	 * @return a future containing the received object
	 */
	@Nonnull
	default Future<T> getById(I id) {
		return getById(id, false);
	}

	/**
	 * Gets an object by its id, optionally bypassing any cached values
	 *
	 * @param id id to get
	 * @param force should this retrieval bypass any cached values
	 * @return a future containing the received object
	 */
	@Nonnull
	Future<T> getById(I id, boolean force);

}
