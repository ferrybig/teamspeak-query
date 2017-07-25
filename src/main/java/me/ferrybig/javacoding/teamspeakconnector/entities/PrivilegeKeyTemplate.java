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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketEncoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

@NotThreadSafe
@ParametersAreNonnullByDefault
public class PrivilegeKeyTemplate {

	private Map<String, String> customset;
	private String description;
	private PrivilegeKey.Type type;
	private int token1;
	private int token2;

	public PrivilegeKeyTemplate(Map<String, String> tokencustomset, String description, PrivilegeKey.Type type, int token1, int token2) {
		this.customset = Objects.requireNonNull(tokencustomset, "customset");
		this.description = Objects.requireNonNull(description, "description");
		this.type = Objects.requireNonNull(type, "type");
		this.token1 = Objects.requireNonNull(token1, "token1");
		this.token2 = Objects.requireNonNull(token2, "token2");
	}

	public Map<String, String> getCustomset() {
		return customset;
	}

	public void setCustomset(Map<String, String> customset) {
		this.customset = Objects.requireNonNull(customset, "tokencustomset");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = Objects.requireNonNull(description, "description");
	}

	public PrivilegeKey.Type getType() {
		return type;
	}

	public void setType(PrivilegeKey.Type type) {
		this.type = Objects.requireNonNull(type, "type");
	}

	public int getToken1() {
		return token1;
	}

	public void setToken1(int token1) {
		this.token1 = token1;
	}

	public int getToken2() {
		return token2;
	}

	public void setToken2(int token2) {
		this.token2 = token2;
	}

	@Override
	public String toString() {
		return "PrivilegeKeyTemplate{" + "tokencustomset=" + customset + ", description=" + description + ", type=" + type + ", token1=" + token1 + ", token2=" + token2 + '}';
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.customset);
		hash = 79 * hash + Objects.hashCode(this.description);
		hash = 79 * hash + Objects.hashCode(this.type);
		hash = 79 * hash + this.token1;
		hash = 79 * hash + this.token2;
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
		final PrivilegeKeyTemplate other = (PrivilegeKeyTemplate) obj;
		if (this.token1 != other.token1) {
			return false;
		}
		if (this.token2 != other.token2) {
			return false;
		}
		if (!Objects.equals(this.description, other.description)) {
			return false;
		}
		if (!Objects.equals(this.customset, other.customset)) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

	public Future<PrivilegeKey> createKey(TeamspeakConnection con) {
		String description = this.description;
		PrivilegeKey.Type type = this.type;
		int token1 = this.token1;
		int token2 = this.token2;
		Map<String, String> customset = this.customset.isEmpty() ? Collections.emptyMap() : new LinkedHashMap<>(this.customset);

		final ComplexRequestBuilder builder = Command.PRIVILEGEKEY_ADD
				.addData("tokendescription", description)
				.addData("tokentype", type.getId())
				.addData("tokenid1", token1)
				.addData("tokenid2", token2);

		Iterator<Map.Entry<String, String>> itr = customset.entrySet().iterator();
		if (itr.hasNext()) {
			StringBuilder customStr = new StringBuilder();
			do {
				Map.Entry<String, String> next = itr.next();
				customStr.append("|ident=").append(PacketEncoder.encodeTeamspeakCode(next.getKey()))
						.append(" value=").append(PacketEncoder.encodeTeamspeakCode(next.getValue()));
			} while (itr.hasNext());
			builder.addData("tokencustomset", customStr.substring(1));
		}

		return con.io().chainFuture(con.io().sendPacket(builder.build()),
				p -> new PrivilegeKey(con, p.getCommands().get(0).get("token"), customset, description, type, token1, token2));
	}

}
