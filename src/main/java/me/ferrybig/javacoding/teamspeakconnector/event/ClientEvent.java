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
public abstract class ClientEvent {
	private final UnresolvedUser client;

	public ClientEvent(UnresolvedUser client) {
		this.client = client;
	}

	public UnresolvedUser getClient() {
		return client;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + "client=" + client + '}';
	}
	
	
}
