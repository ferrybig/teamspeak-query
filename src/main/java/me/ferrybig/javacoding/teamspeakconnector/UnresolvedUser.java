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

import io.netty.util.concurrent.Future;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

public class UnresolvedUser implements Resolvable<User> {

	protected final TeamspeakConnection con;
	private final int id;

	public UnresolvedUser(TeamspeakConnection con, int id) {
		this.con = con;
		this.id = id;

	}

	public Future<?> sendMessage(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("sendtextmessage")
						.addData("targetmode", "1")
						.addData("target", String.valueOf(getId()))
						.addData("msg", message)
						.build());
	}

	public Future<?> poke(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientpoke")
						.addData("clid", String.valueOf(getId()))
						.addData("msg", message)
						.build());
	}

	public Future<?> kickFromChannel(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientkick")
						.addData("clid", String.valueOf(getId()))
						.addData("reasonid", "4")
						.addData("msg", message)
						.build());
	}

	public Future<?> kickFromServer(String message) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientpoke")
						.addData("clid", String.valueOf(getId()))
						.addData("reasonid", "5")
						.addData("msg", message)
						.build());
	}

	public Future<?> move(UnresolvedChannel channel) {
		return this.move(channel, "");
	}

	public Future<?> move(UnresolvedChannel channel, String password) {
		return this.con.io().sendPacket(
				new ComplexRequestBuilder("clientmove")
						.addData("clid", String.valueOf(getId()))
						.addData("cid", String.valueOf(channel.getId()))
						.build());
	}

	public int getId() {
		return id;
	}

	@Override
	public Future<User> forceResolve() {
		return con.getUserById(id);
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + "id=" + id + '}';
	}

}
