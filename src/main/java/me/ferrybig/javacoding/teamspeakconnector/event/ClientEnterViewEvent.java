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
package me.ferrybig.javacoding.teamspeakconnector.event;

import me.ferrybig.javacoding.teamspeakconnector.ChangeReason;
import me.ferrybig.javacoding.teamspeakconnector.entities.NamedOnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowUser;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;

public class ClientEnterViewEvent extends ClientResolvedEvent {

	private final UnresolvedChannel to;
	private final ChangeReason reason;
	private final NamedOnlineClient invoker;

	@Deprecated
	public ClientEnterViewEvent(ShallowUser client, UnresolvedChannel to,
			ChangeReason reason, NamedOnlineClient invoker) {
		super(client);
		this.to = to;
		this.reason = reason;
		this.invoker = invoker;
	}

	public UnresolvedChannel getTo() {
		return to;
	}

	public ChangeReason getReason() {
		return reason;
	}

	public NamedOnlineClient getInvoker() {
		return invoker;
	}

}
