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
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 * Repository for resolvable objects based on integer keys
 * @param <U> unresolved object
 * @param <T> resolved objects
 */
@ParametersAreNonnullByDefault
public abstract class AbstractIntResolvableRepository<U extends Resolvable<? extends T>, T extends U>
		extends AbstractBaseResolvableRepository<U, T>
		implements IntResolvableRepository<U, T> {

	public AbstractIntResolvableRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public Future<?> deleteUnresolved(U unresolved, boolean force) {
		return io.sendPacket(requestDelete(unresolved, force));
	}

	@Override
	public Future<T> get(U unresolved, boolean force) {
		Objects.requireNonNull(unresolved, "unresolved");
		ComplexRequest getById = requestGet(unresolved);
		if (getById == null) {
			return io.chainFuture(list(), r -> r.stream()
					.filter(unresolved::equals).findAny()
					.orElseThrow(() -> new NoSuchElementException(
					"No object found by id " + getId(unresolved))));
		} else {
			return connection.mapping()
					.mapComplexReponse(io.sendPacket(getById),
							this::readEntityChecked);
		}
	}

	@Override
	@Nonnull
	public abstract U unresolved(int id);

	/**
	 * Gets the id from an unresolved object.
	 *
	 * The general contract for {@code getId} is that it does the reverse of
	 * {@code unresolved}, more specific, for any non-null input, the following
	 * expression should be true: {@code obj.equals(unresolved(getId(obj))}
	 *
	 * @param value object to get the id from
	 * @return the id for the unresolved resolvable
	 */
	@Nonnull
	protected abstract int getId(U value);

}
