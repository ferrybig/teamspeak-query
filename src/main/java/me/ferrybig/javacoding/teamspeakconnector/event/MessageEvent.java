/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class MessageEvent {

	/**
	 * The received message
	 */
	private final String message;
	/**
	 * Target of the message
	 */
	private final TargetMode targetMode;

	private final int invokerId;
	private final String invokerName;
	private final String invokerUid;

	public MessageEvent(String message, TargetMode targetMode, int invokerId, String invokerName, String invokerUid) {
		this.message = message;
		this.targetMode = targetMode;
		this.invokerId = invokerId;
		this.invokerName = invokerName;
		this.invokerUid = invokerUid;
	}

	public String getMessage() {
		return message;
	}

	public TargetMode getTargetMode() {
		return targetMode;
	}

	public int getInvokerId() {
		return invokerId;
	}

	public String getInvokerName() {
		return invokerName;
	}

	public String getInvokerUid() {
		return invokerUid;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + "message=" + message + ", targetMode=" + targetMode + ", invokerId=" + invokerId + ", invokerName=" + invokerName + ", invokeruid=" + invokerUid + '}';
	}

	public enum TargetMode {

		SERVER, CHANNEL, PRIVATE
	}
}
