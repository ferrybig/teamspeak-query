/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal;

import io.netty.util.concurrent.Promise;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakException;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexResponse;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class PendingPacket {
	private final Promise<ComplexResponse> promise;
	private final ComplexRequest request;
	private final boolean closeRequested;

	public PendingPacket(Promise<ComplexResponse> promise, ComplexRequest request, boolean closeRequested) {
		this.promise = promise;
		this.request = request;
		this.closeRequested = closeRequested;
	}
	
	public void onResponseReceived(ComplexResponse response) {
		if (response.getId() != 0) {
			TeamspeakException ex;
			if(response.getExtraMsg() == null) {
				ex = new TeamspeakException(request.getCmd() + ": " + response.getMsg());
			} else {
				ex = new TeamspeakException(request.getCmd() + ": " + response.getMsg() + "; " + response.getExtraMsg());
			}
			promise.setFailure(ex);
		} else {
			promise.setSuccess(response);
		}
	}
	
	public void onChannelClose(Throwable lastException) {
		if(closeRequested) {
			promise.setSuccess(null);
		} else {
			TeamspeakException ex;
			if(lastException == null) {
				ex = new TeamspeakException(request.getCmd() + ": -1: Channel closed");
			} else {
				final String message = lastException.getMessage();
				int index = message.indexOf(':');
				if(index < 0) {
					index = message.length() - 1;
				}
				ex = new TeamspeakException(request.getCmd() + ": -1: Channel closed: " + (message.substring(0, index)));
			}
			promise.setFailure(ex);
		}
	}
}
