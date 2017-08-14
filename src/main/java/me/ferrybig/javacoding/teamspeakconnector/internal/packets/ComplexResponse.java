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

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ComplexResponse {

	private final ArrayList<Map<String, String>> commands;
	private final int id;
	private final String msg;
	private final String extraMsg;

	public ComplexResponse(ArrayList<Map<String, String>> commands, int id,
			String msg, String extraMsg) {
		this.commands = commands;
		this.id = id;
		this.msg = msg;
		this.extraMsg = extraMsg;
	}

	public ArrayList<Map<String, String>> getCommands() {
		return commands;
	}

	public int getId() {
		return id;
	}

	public String getMsg() {
		return msg;
	}

	public String getExtraMsg() {
		return extraMsg;
	}

	@Override
	public String toString() {
		return "ComplexResponse{" + "commands=" + commands + ",\nid="
				+ id + ", msg=" + msg + '}';
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + Objects.hashCode(this.commands);
		hash = 19 * hash + this.id;
		hash = 19 * hash + Objects.hashCode(this.msg);
		hash = 19 * hash + Objects.hashCode(this.extraMsg);
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
		final ComplexResponse other = (ComplexResponse) obj;
		if (this.id != other.id) {
			return false;
		}
		if (!Objects.equals(this.msg, other.msg)) {
			return false;
		}
		if (!Objects.equals(this.extraMsg, other.extraMsg)) {
			return false;
		}
		if (!Objects.equals(this.commands, other.commands)) {
			return false;
		}
		return true;
	}

}
