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
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.util.concurrent.Promise;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakCommandException;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;

public class PendingPacket {

	private final Promise<ComplexResponse> promise;
	private final ComplexRequest request;
	private final SendBehaviour sendBehaviour;

	public PendingPacket(Promise<ComplexResponse> promise, ComplexRequest request, SendBehaviour sendBehaviour) {
		this.promise = promise;
		this.request = request;
		this.sendBehaviour = sendBehaviour;
	}

	public void onResponseReceived(ComplexResponse response) {
		if (response.getId() != 0) {
			promise.setFailure(new TeamspeakCommandException(request.getCmd(), response.getId(), response.getMsg(), response.getExtraMsg()));
		} else {
			promise.setSuccess(response);
		}
	}

	public void onChannelClose(Throwable lastException) {
		if (sendBehaviour != SendBehaviour.NORMAL) {
			promise.setSuccess(null);
		} else {
			TeamspeakException ex;
			if (lastException == null) {
				ex = new TeamspeakException(request.getCmd() + ": -1: Channel closed");
			} else {
				final String message = lastException.getMessage();
				int index = message.indexOf(':');
				if (index < 0) {
					index = message.length() - 1;
				}
				ex = new TeamspeakException(request.getCmd() + ": -1: Channel closed: " + (message.substring(0, index)));
			}
			promise.setFailure(ex);
		}
	}
}
