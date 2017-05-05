/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import java.util.Map;

/**
 *
 * @author Fernando
 */
public class Response {
	private final Map<String, String> options;
	private final String cmd;

	public Response(Map<String, String> options, String cmd) {
		this.options = options;
		this.cmd = cmd;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public String getCmd() {
		return cmd;
	}

	@Override
	public String toString() {
		return "Response{" + "options=" + options + ", cmd=" + cmd + '}';
	}
	
}
