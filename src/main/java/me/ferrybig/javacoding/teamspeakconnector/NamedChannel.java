/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class NamedChannel extends UnresolvedChannel {

	private final String name;
	
	public NamedChannel(TeamspeakConnection con, int id, String name) {
		super(con, id);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "NamedChannel{" + "id=" + getId() + ",name=" + getName() + '}';
	}
	
	
}
