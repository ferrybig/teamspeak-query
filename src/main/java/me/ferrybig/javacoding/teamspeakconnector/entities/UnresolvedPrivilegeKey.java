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
import java.util.NoSuchElementException;
import me.ferrybig.javacoding.teamspeakconnector.Resolvable;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;

/**
 *
 * @author Fernando van Loenhout
 */
public class UnresolvedPrivilegeKey implements Resolvable<PrivilegeKey> {

	protected final TeamspeakConnection con;
	protected final String token;

	public UnresolvedPrivilegeKey(TeamspeakConnection con, String token) {
		this.con = con;
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public Future<?> useKey() {
		return con.io().sendPacket(Command.PRIVILEGEKEY_USE
				.addData("token", getToken()).build());
	}

	@Override
	public Future<PrivilegeKey> forceResolve() {
		return con.io().chainFuture(con.io().sendPacket(
				Command.PRIVILEGEKEY_LIST.build()),
				r -> r.getCommands().stream()
						.filter(p -> p.get("token").equals("token")).findAny()
						.map(con.mapping()::mapPrivilegeKey)
						.orElseThrow(() -> new NoSuchElementException(
						"No token found by id " + token)));
	}

	public Future<?> use() {
		return con.io().sendPacket( Command.PRIVILEGEKEY_USE
				.addData("token", token).build());
	}

	public Future<?> delete() {
		return con.io().sendPacket( Command.PRIVILEGEKEY_DELETE
				.addData("token", token).build());
	}

	@Override
	public boolean isResolved() {
		return false;
	}

}
