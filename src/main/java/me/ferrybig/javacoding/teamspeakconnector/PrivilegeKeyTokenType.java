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

/**
 *
 * @author Fernando van Loenhout
 */
public enum PrivilegeKeyTokenType {
	/**
	 * server group token (id1={groupID} id2=0)
	 */
	SERVER_GROUP(0),
	/**
	 * channel group token (id1={groupID} id2={channelID})
	 */
	CHANNEL_GROUP(1);

	private static final int BY_ID_LENGTH = 2;
	private static final PrivilegeKeyTokenType[] BY_ID;

	static {
		BY_ID = new PrivilegeKeyTokenType[BY_ID_LENGTH];
		for (PrivilegeKeyTokenType type : values()) {
			BY_ID[type.id] = type;
		}
	}

	private final int id;

	private PrivilegeKeyTokenType(int id) {
		this.id = id;
	}

	/**
	 * Gets the internal id of the type
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	/**
	 * Gets a type y its id
	 *
	 * @param id the id to look for
	 * @return the type that matches the id
	 * @throws IllegalArgumentException if the id isn't mapped to a type
	 */
	public static PrivilegeKeyTokenType getById(int id) {
		if (id >= BY_ID_LENGTH || id < 0 || BY_ID[id] == null) {
			throw new IllegalArgumentException("No type found for id " + id);
		}
		return BY_ID[id];
	}

}
