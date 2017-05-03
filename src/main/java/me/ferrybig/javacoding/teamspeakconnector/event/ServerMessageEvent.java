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
public class ServerMessageEvent extends MessageEvent {
	
	public ServerMessageEvent(String message, int invokerId, String invokerName, String invokerUid) {
		super(message, TargetMode.SERVER, invokerId, invokerName, invokerUid);
	}
	
}
