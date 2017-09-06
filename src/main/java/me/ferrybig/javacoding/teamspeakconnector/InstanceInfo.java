/*
 * The MIT License
 *
 * Copyright 2017 Fernando van Loenhout.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ferrybig.javacoding.teamspeakconnector;

/**
 * Information about the concurrent server instance.
 *
 * Unmapped fields: serverinstance_database_version=21
 * serverinstance_filetransfer_port=30033
 * serverinstance_max_download_total_bandwidth=18446744073709551615
 * serverinstance_max_upload_total_bandwidth=18446744073709551615
 * serverinstance_guest_serverquery_group=1
 * serverinstance_serverquery_flood_commands=10
 * serverinstance_serverquery_flood_time=3
 * serverinstance_serverquery_ban_time=600
 * serverinstance_template_serveradmin_group=3
 * serverinstance_template_serverdefault_group=5
 * serverinstance_template_channeladmin_group=1
 * serverinstance_template_channeldefault_group=4
 * serverinstance_permissions_version=19
 * serverinstance_pending_connections_per_ip=0
 */
public class InstanceInfo {

	private final int filetransferPort;

	public InstanceInfo(int filetransferPort) {
		this.filetransferPort = filetransferPort;
	}

	public int getFiletransferPort() {
		return filetransferPort;
	}

}
