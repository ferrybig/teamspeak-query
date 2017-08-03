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
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.entities.NamedOnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;

/**
 *
 * @author Fernando van Loenhout
 */
public class SelfInformation {

	private final TeamspeakConnection connection;

	private volatile Future<NamedOnlineClient> whoAmIPromise;

	public SelfInformation(TeamspeakConnection connection) {
		this.connection = connection;
	}

	protected NamedOnlineClient readWhoAmI(Map<String, String> data) {
		return new NamedOnlineClient(connection.onlineClients(),
				Integer.parseInt(data.get("clid")),
				data.get("virtualserver_unique_identifier"),
				data.get("client_nickname"));
	}

	public Future<?> setOwnName(String name) {
		return connection.io().sendPacket(Command.CLIENT_UPDATE
				.addData("client_nickname", name).build());
	}

	public Future<NamedOnlineClient> whoAmI() {
		Future<NamedOnlineClient> whoami = this.whoAmIPromise;
		if (whoami != null) {
			return whoami;
		}
		synchronized (this) {
			whoami = this.whoAmIPromise;
			if (whoami != null) {
				return whoami;
			}
			whoami = connection.mapping().mapComplexReponse(
					connection.io().sendPacket(Command.WHOAMI.build()),
					this::readWhoAmI);
			this.whoAmIPromise = whoami;
		}
		return whoami;
	}
}
