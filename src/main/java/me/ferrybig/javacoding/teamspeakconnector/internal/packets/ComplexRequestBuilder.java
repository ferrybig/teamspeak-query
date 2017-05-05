/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class ComplexRequestBuilder {

	private String cmd;
	private Map<String, String> data = Collections.emptyMap();

	public ComplexRequestBuilder(String cmd) {
		this.cmd = cmd;
	}

	public ComplexRequestBuilder setCmd(String cmd) {
		this.cmd = cmd;
		return this;
	}

	public ComplexRequestBuilder setData(HashMap<String, String> data) {
		this.data = data;
		return this;
	}
	
	public ComplexRequestBuilder addData(String key, Object value) {
		return this.addData(key, String.valueOf(value));
	}
	
	public ComplexRequestBuilder addData(String key, String value) {
		if(data.isEmpty()) {
			data = new LinkedHashMap<>();
		}
		data.put(key, value);
		return this;
	}
	
	public ComplexRequestBuilder addOption(String key) {
		if(data.isEmpty()) {
			data = new LinkedHashMap<>();
		}
		data.put("-" + key, "");
		return this;
	}

	public ComplexRequest build() {
		return new ComplexRequest(cmd, data);
	}
	
}
