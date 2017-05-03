/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

import me.ferrybig.javacoding.teamspeakconnector.UnresolvedChannel;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class ChannelMessageEvent extends MessageEvent{
	private final UnresolvedChannel target;

	public ChannelMessageEvent(UnresolvedChannel target, String message, int invokerId, String invokerName, String invokerUid) {
		super(message, TargetMode.CHANNEL, invokerId, invokerName, invokerUid);
		this.target = target;
	}

	public UnresolvedChannel getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return "ChannelMessageEvent{" + "message=" + getMessage() + ", targetMode=" + getTargetMode() + ", target=" + getTarget() + ", invokerId=" + getInvokerId() + ", invokerName=" + getInvokerName() + ", invokeruid=" + getInvokerUid() + '}';
	}
}
