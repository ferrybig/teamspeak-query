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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ComplexRequest {

	private final String cmd;
	private final Map<String, String> data;
	private final boolean raw;

	public ComplexRequest(String cmd, boolean raw) {
		this(cmd, Collections.emptyMap(), raw);
	}

	public ComplexRequest(String cmd, Map<String, String> data, boolean raw) {
		this.cmd = Objects.requireNonNull(cmd);
		this.data = data.isEmpty()
				? Collections.emptyMap() : Collections.unmodifiableMap(data);
		this.raw = raw;
	}

	public boolean isRaw() {
		return raw;
	}

	public String getCmd() {
		return cmd;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "ComplexRequest{" + "cmd=" + cmd + ", data=" + data + '}';
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + Objects.hashCode(this.cmd);
		hash = 29 * hash + Objects.hashCode(this.data);
		hash = 29 * hash + (this.raw ? 1 : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
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
