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

import me.ferrybig.javacoding.teamspeakconnector.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.UnresolvedUser;

public class ClientLeftViewEvent extends ClientEvent {

	private final UnresolvedChannel from;
	private final ChangeReason reason;
	private final String message;
	private final UnresolvedUser invoker;
	private final String invokerName;
	private final String invokerUid;

	public ClientLeftViewEvent(UnresolvedUser client, UnresolvedChannel from, ChangeReason reason, String message, UnresolvedUser invoker, String invokerName, String invokerUid) {
		super(client);
		this.from = from;
		this.reason = reason;
		this.message = message;
		this.invoker = invoker;
		this.invokerName = invokerName;
		this.invokerUid = invokerUid;
	}

	public ChangeReason getReason() {
		return reason;
	}

	public UnresolvedUser getInvoker() {
		return invoker;
	}

	public String getInvokerName() {
		return invokerName;
	}

	public String getInvokerUid() {
		return invokerUid;
	}

	@Override
	public String toString() {
		return "ClientLeftViewEvent{" + "client=" + getClient() + ",channel=" + from + ", reason=" + reason + ", message=" + message + ", invoker=" + invoker + ", invokerName=" + invokerName + ", invokerUid=" + invokerUid + '}';
	}

	public UnresolvedChannel getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}

}
