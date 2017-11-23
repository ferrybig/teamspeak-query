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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A request, ready to be send to the Teamspeak server
 * @author Fernando
 */
@Immutable
public final class ComplexRequest {

	private final String cmd;
	private final Map<String, String> data;
	private final boolean raw;

	/**
	 * Creates a <code>ComplexRequest</code>
	 * @param cmd Command to be send
	 * @param raw Should the command be escaped when send
	 */
	public ComplexRequest(String cmd, boolean raw) {
		this(cmd, Collections.emptyMap(), raw);
	}

	/**
	 * Creates a <code>ComplexRequest</code>
	 * @param cmd Command to be send
	 * @param data Arguments of this packet
	 * @param raw Should the command be escaped when send
	 */
	public ComplexRequest(String cmd, Map<String, String> data, boolean raw) {
		this.cmd = Objects.requireNonNull(cmd);
		this.data = data.isEmpty()
				? Collections.emptyMap() : Collections.unmodifiableMap(data);
		this.raw = raw;
	}

	/**
	 * Checks if this packet is in raw mode
	 * @return returns true if in raw mode
	 */
	public boolean isRaw() {
		return raw;
	}

	/**
	 * Get the command, and optionally arguments if in raw mode
	 * @return the command
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * Returns all arguments for this packet
	 * @return all arguments for this packet
	 */
	public Map<String, String> getData() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ComplexRequest{" + "cmd=" + cmd + ", data=" + data + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + Objects.hashCode(this.cmd);
		hash = 29 * hash + Objects.hashCode(this.data);
		hash = 29 * hash + (this.raw ? 1 : 0);
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
		final ComplexRequest other = (ComplexRequest) obj;
		if (this.raw != other.raw) {
			return false;
		}
		if (!Objects.equals(this.cmd, other.cmd)) {
			return false;
		}
		if (!Objects.equals(this.data, other.data)) {
			return false;
		}
		return true;
	}

}
