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

// Fields: serverinstance_database_version=21 serverinstance_filetransfer_port=30033 serverinstance_max_download_total_bandwidth=18446744073709551615 serverinstance_max_upload_total_bandwidth=18446744073709551615 serverinstance_guest_serverquery_group=1 serverinstance_serverquery_flood_commands=10 serverinstance_serverquery_flood_time=3 serverinstance_serverquery_ban_time=600 serverinstance_template_serveradmin_group=3 serverinstance_template_serverdefault_group=5 serverinstance_template_channeladmin_group=1 serverinstance_template_channeldefault_group=4 serverinstance_permissions_version=19 serverinstance_pending_connections_per_ip=0
public class InstanceInfo {
	private final int filetransferPort;

	public InstanceInfo(int filetransferPort) {
		this.filetransferPort = filetransferPort;
	}

	public int getFiletransferPort() {
		return filetransferPort;
	}
	
	
}
