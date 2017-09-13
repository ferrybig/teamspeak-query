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
import javax.annotation.Nonnull;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

/**
 * A server instance that hasn't been resolved yet
 */
public interface UnresolvedServer extends Resolvable<Server> {

	/**
	 * Selects this server, so all future server based interactions will use
	 * this server
	 *
	 * @return the results as a future
	 */
	@Nonnull
	Future<TeamspeakConnection> select();

	/**
	 * Starts this virtual server
	 *
	 * @return the results as a future
	 */
	@Nonnull
	Future<TeamspeakConnection> start();

	/**
	 * Stops this virtual server
	 *
	 * @return the results as a future
	 */
	@Nonnull
	Future<TeamspeakConnection> stop();

}
