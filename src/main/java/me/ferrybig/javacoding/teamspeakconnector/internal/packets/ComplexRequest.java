/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Fernando
 */
public class ComplexRequest {
	private final String cmd;
	private final Map<String, String> data;

	public ComplexRequest(String cmd, Map<String, String> data) {
		this.cmd = Objects.requireNonNull(cmd);
		this.data = Collections.unmodifiableMap(data);
	}

	public String getCmd() {
		return cmd;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "ComplexRequest{" + "cmd=" + cmd + ", data=" + data + '}';
	}
	
	
}
