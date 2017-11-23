/*
 * The MIT License
 *
 * Copyright 2017 Fernando.
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
package me.ferrybig.javacoding.teamspeakconnector.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Simple pair class
 * @param <A> first argument
 * @param <B> second argument
 */
@Immutable
@ParametersAreNullableByDefault
public final class Pair<A, B> {

	@Nullable
	private final A first;
	@Nullable
	private final B second;

	/**
	 * Constructs a pair class
	 * @param first first argument
	 * @param second second argument
	 */
	public Pair(@Nullable A first, @Nullable B second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressFBWarnings(value = "NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair<?, ?> other = (Pair<?, ?>) obj;
		if (!Objects.equals(this.first, other.first)) {
			return false;
		}
		if (!Objects.equals(this.second, other.second)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the first argument
	 * @return the first argument
	 */
	@Nullable
	public A getFirst() {
		return first;
	}

	/**
	 * Gets the second argument
	 * @return the second argument
	 */
	@Nullable
	public B getSecond() {
		return second;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.first);
		hash = 89 * hash + Objects.hashCode(this.second);
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public String toString() {
		return "Pair{" + "first=" + first + ", second=" + second + '}';
	}

}
