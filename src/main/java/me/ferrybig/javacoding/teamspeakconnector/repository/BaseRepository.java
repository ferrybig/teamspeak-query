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
import java.util.List;
import javax.annotation.Nonnull;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

/**
 * Base interface for all repository interfaces
 *
 * @param <T> type that this repository returns
 */
public interface BaseRepository<T> {

	/**
	 * Deletes an object
	 *
	 * @param type object to delete
	 * @return a future containing the deletion result
	 */
	@Nonnull
	default Future<?> delete(T type) {
		return delete(type, false);
	}

	/**
	 * Deletes an object, optionally with force
	 *
	 * @param type object to delete
	 * @param force should deletion be forced, this usually means bypassing any
	 * in use status if this object has that kind of status
	 * @return a future containing the deletion result
	 */
	@Nonnull
	Future<?> delete(T type, boolean force);

	/**
	 * Gets the {@code TeamspeakConnection} that this repository contains
	 *
	 * @return the {@code TeamspeakConnection} that this repository contains
	 */
	@Nonnull
	TeamspeakConnection getConnection();

	/**
	 * Gets a list of objects of this type
	 *
	 * @return A future containing the list of objects of this type, should
	 * never wrap a null
	 */
	@Nonnull
	Future<List<T>> list();

}
