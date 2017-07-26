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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

/**
 *
 * @author Fernando van Loenhout
 */
@ParametersAreNonnullByDefault
public class PrivilegeKey extends UnresolvedPrivilegeKey {

	private final Map<String, String> customset;
	private final String description;
	private final PrivilegeKey.Type type;
	private final int token1;
	private final int token2;

	public PrivilegeKey(TeamspeakConnection con, String token,
			PrivilegeKeyTemplate template) {
		this(con, token,
				Collections.unmodifiableMap(new LinkedHashMap<>(
						Objects.requireNonNull(template, "template")
								.getCustomset())),
				template.getDescription(), template.getType(),
				template.getToken1(), template.getToken2());
	}

	public PrivilegeKey(TeamspeakConnection con, String token,
			Map<String, String> customset, String description, Type type,
			int token1, int token2) {
		super(con, token);
		this.customset = customset;
		this.description = description;
		this.type = type;
		this.token1 = token1;
		this.token2 = token2;
	}

	@Nonnull
	public Map<String, String> getCustomset() {
		return customset;
	}

	@Nonnull
	public String getDescription() {
		return description;
	}

	@Nonnull
	public Type getType() {
		return type;
	}

	public int getToken1() {
		return token1;
	}

	public int getToken2() {
		return token2;
	}

	@Nonnull
	public PrivilegeKeyTemplate toTemplate() {
		return new PrivilegeKeyTemplate(new HashMap<>(customset),
				description, type, token1, token2);
	}

	@Override
	public String toString() {
		return "PrivilegeKey{" + "token=" + getToken()
				+ ", customset=" + customset + ", description=" + description
				+ ", type=" + type + ", token1=" + token1
				+ ", token2=" + token2 + '}';
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public Future<PrivilegeKey> resolve() {
		return con.io().getCompletedFuture(this);
	}

	public enum Type {
		/**
		 * server group token (id1={groupID} id2=0)
		 */
		SERVER_GROUP(0),
		/**
		 * channel group token (id1={groupID} id2={channelID})
		 */
		CHANNEL_GROUP(1);

		private static final Type[] BY_ID;

		static {
			Type[] values = values();
			BY_ID = new Type[values.length];
			for (Type type : values()) {
				BY_ID[type.id] = type;
			}
		}
		private final int id;

		private Type(int id) {
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
		public static Type getById(int id) {
			if (id >= BY_ID.length || id < 0 || BY_ID[id] == null) {
				throw new IllegalArgumentException("No type found by" + id);
			}
			return BY_ID[id];
		}
	}

}
