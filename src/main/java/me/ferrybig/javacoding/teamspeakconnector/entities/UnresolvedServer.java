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
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;

public class UnresolvedServer {

	protected final TeamspeakConnection con;
	private final int sid;

	public UnresolvedServer(TeamspeakConnection con, int sid) {
		this.con = con;
		this.sid = sid;
	}

	public Future<TeamspeakConnection> select() {
		return con.io().chainFuture(
				con.io().sendPacket(Command.USE.addData("sid", String.valueOf(sid)).addOption("virtual").build()),
				ignored -> con).addListener(future -> {
					if (future.isSuccess()) {
						con.io().notifyServerChanged();
					}
				});
	}

	public Future<TeamspeakConnection> stop() {
		return con.io().chainFuture(
				con.io().sendPacket(Command.SERVER_STOP.addData("sid", sid).build()),
				ignored -> con);
	}

	public Future<TeamspeakConnection> start() {
		return con.io().chainFuture(
				con.io().sendPacket(Command.SERVER_START.addData("sid", sid).build()),
				ignored -> con);
	}
}
