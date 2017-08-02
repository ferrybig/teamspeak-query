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
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.TeamspeakIO;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout
 */
public abstract class AbstractBaseResolvableRepository<U extends Resolvable<? extends T>, T extends U>
		implements BaseResolvableRepository<U, T> {

	protected final TeamspeakConnection connection;
	protected final TeamspeakIO io;

	public AbstractBaseResolvableRepository(TeamspeakConnection connection) {
		this.connection = Objects.requireNonNull(connection, "connection");
		this.io = connection.io();
	}

	@Override
	public TeamspeakConnection getConnection() {
		return connection;
	}

	@Override
	public Future<List<T>> list() {
		return connection.mapping().mapComplexReponseList(
				io.sendPacket(requestList()),
				this::readEntityChecked);
	}

	@Nonnull
	protected abstract T readEntity(Map<String, String> data);

	@Nonnull
	protected final T readEntityChecked(Map<String, String> data) {
		T object = readEntity(data);
		assert object != null : "readEntity() returned null object on "
				+ this;
		assert object.isResolved() : "readEntity() returned unreolved object on "
				+ this + ":" + object;
		return object;
	}

	@Nonnull
	protected abstract ComplexRequest requestList();

	@CheckForNull
	@Nullable
	protected abstract ComplexRequest requestGet(U unresolved);

	@Nonnull
	protected abstract ComplexRequest requestDelete(U unresolved, boolean force);
}
