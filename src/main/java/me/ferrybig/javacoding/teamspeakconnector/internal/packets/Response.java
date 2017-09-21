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
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * A class representing a line of received content
 */
@ParametersAreNonnullByDefault
@Immutable
public final class Response {

	private final Map<String, String> options;
	private final String cmd;

	/**
	 * Creates a new Response
	 * @param options Arguments
	 * @param cmd Optional command
	 */
	public Response(Map<String, String> options, String cmd) {
		this.options = options;
		this.cmd = cmd;
	}

	/**
	 * Gets the arguments
	 * @return the arguments
	 */
	@Nonnull
	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * Get the passed response name, or an empty string if there is none
	 * @return the passed response name, or an empty string if there is none
	 */
	@Nonnull
	public String getCmd() {
		return cmd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public String toString() {
		return "Response{" + "options=" + options + ", cmd=" + cmd + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.options);
		hash = 37 * hash + Objects.hashCode(this.cmd);
		return hash;
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
		final Response other = (Response) obj;
		if (!Objects.equals(this.cmd, other.cmd)) {
			return false;
		}
		if (!Objects.equals(this.options, other.options)) {
			return false;
		}
		return true;
	}

}
