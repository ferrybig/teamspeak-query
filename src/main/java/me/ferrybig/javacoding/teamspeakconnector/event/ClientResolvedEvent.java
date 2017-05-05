/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

import me.ferrybig.javacoding.teamspeakconnector.User;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class ClientResolvedEvent extends ClientEvent {

	public ClientResolvedEvent(User client) {
		super(client);
	}

	@Override
	public User getClient() {
		return (User)super.getClient();
	}
	
	
}
