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
import java.util.List;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequestBuilder;

public class UnresolvedChannel {

	protected final TeamspeakConnection con;

	private final int id;

	public UnresolvedChannel(TeamspeakConnection con, int id) {
		this.con = con;
		this.id = id;
	}

	public Future<Channel> resolv() {
		return forceResolv();
	}

	public Future<Channel> forceResolv() {
		return con.getChannelById(id);
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "UnresolvedChannel{" + "id=" + id + '}';
	}

	public Future<List<File>> getFileTransferList() {
		return getFileTransferList("/");
	}

	public Future<List<File>> getFileTransferList(String path) {
		return con.io().mapComplexReponseList(con.io().sendPacket(
				new ComplexRequestBuilder("ftgetfilelist").addData("cid", getId()).addData("path", path).build()),
				con.io()::mapFile);
	}

	public Future<?> moveInto(UnresolvedUser user) {
		return con.io().sendPacket(new ComplexRequestBuilder("clientmove").addData("cid", getId()).addData("clid", user.getId()).build());
	}

}
