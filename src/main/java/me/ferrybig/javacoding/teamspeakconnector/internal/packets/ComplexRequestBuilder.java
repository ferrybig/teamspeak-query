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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ComplexRequestBuilder {

	private final Command cmd;
	private Map<String, String> data = Collections.emptyMap();

	public ComplexRequestBuilder(Command cmd) {
		this.cmd = cmd;
	}

	@Deprecated
	public ComplexRequestBuilder(String cmd) {
		this.cmd = Command.byName(cmd);
	}

	public ComplexRequestBuilder setData(HashMap<String, String> data) {
		this.data = data;
		return this;
	}

	public ComplexRequestBuilder addData(String key, Object value) {
		return addData(key, value, false);
	}

	public ComplexRequestBuilder addData(String key, Object value, boolean force) {
		return this.addData(key, String.valueOf(value), force);
	}

	public ComplexRequestBuilder addData(String key, String value) {
		return addData(key, value, false);
	}

	public ComplexRequestBuilder addData(String key, String value, boolean force) {
		if (!force && !cmd.isValidOption(key)) {
			throw new IllegalArgumentException("Options '" + key
					+ "' is not known for cmd '" + cmd + "'");
		}
		if (data.isEmpty()) {
			data = new LinkedHashMap<>();
		}
		data.put(key, value);
		return this;
	}

	public ComplexRequestBuilder addOption(String key) {
		return addOption(key, false);
	}

	public ComplexRequestBuilder addOption(String key, boolean force) {
		if (!force && !cmd.isValidFlag(key)) {
			throw new IllegalArgumentException("Flag '" + key
					+ "' is not known for cmd '" + cmd + "'");
		}
		if (data.isEmpty()) {
			data = new LinkedHashMap<>();
		}
		data.put("-" + key, "");
		return this;
	}

	public ComplexRequest build() {
		return new ComplexRequest(cmd.getCmd(), data, false);
	}

	public Command getCmd() {
		return cmd;
	}

}
