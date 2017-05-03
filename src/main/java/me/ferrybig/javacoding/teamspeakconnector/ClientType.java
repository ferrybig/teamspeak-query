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
public enum ClientType {
	NORMAL, QUERY;
	
	public static ClientType getById(int id) {
		return NORMAL; // TODO
	}
}
