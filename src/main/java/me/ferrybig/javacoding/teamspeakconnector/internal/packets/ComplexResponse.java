/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Fernando
 */
public class ComplexResponse {

	private final ArrayList<Map<String, String>> commands;
	private final int id;
	private final String msg;
	private final String extraMsg;

	public ComplexResponse(ArrayList<Map<String, String>> commands, int id, String msg, String extraMsg) {
		this.commands = commands;
		this.id = id;
		this.msg = msg;
		this.extraMsg = extraMsg;
	}

	public ArrayList<Map<String, String>> getCommands() {
		return commands;
	}

	public int getId() {
		return id;
	}

	public String getMsg() {
		return msg;
	}

	public String getExtraMsg() {
		return extraMsg;
	}

	@Override
	public String toString() {
		return "ComplexResponse{" + "commands=" + commands + ",\nid=" + id + ", msg=" + msg + '}';
	}

	
}
