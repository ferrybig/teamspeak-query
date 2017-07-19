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

/**
 * Class designed as marker for things that are resolvable
 *
 * @author Fernando van Loenhout
 * @param <T> Type to return after resolving
 */
public interface Resolvable<T> {

	/**
	 * Tries to resolve this resolvable, this may return itself if it is already
	 * resolved.
	 *
	 * @return the result of a normal resolve
	 */
	public default Future<T> resolve() {
		return this.forceResolve();
	}

	/**
	 * Force resolve this instance, this will always re-resolve the instance
	 *
	 * @return the result of the force resolve
	 */
	public Future<T> forceResolve();

	/**
	 * Check if this instance is already resolved.
	 *
	 * @return if the instance is resolved
	 */
	public boolean isResolved();
}
