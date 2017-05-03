/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

import me.ferrybig.javacoding.teamspeakconnector.UnresolvedUser;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class PrivateMessageEvent extends MessageEvent {
	private final UnresolvedUser target;

	public PrivateMessageEvent(UnresolvedUser target, String message, int invokerId, String invokerName, String invokeruid) {
		super(message, TargetMode.PRIVATE, invokerId, invokerName, invokeruid);
		this.target = target;
	}

	@Override
	public String toString() {
		return "PrivateMessageEvent{" + "message=" + getMessage() + ", targetMode=" + getTargetMode() + ", target=" + getTarget() + ", invokerId=" + getInvokerId() + ", invokerName=" + getInvokerName() + ", invokeruid=" + getInvokerUid() + '}';
	}

	public UnresolvedUser getTarget() {
		return target;
	}
	
}
