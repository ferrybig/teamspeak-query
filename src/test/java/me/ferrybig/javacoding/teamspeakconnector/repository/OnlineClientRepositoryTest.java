/*
 * The MIT License
 *
 * Copyright 2017 Fernando.
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
package me.ferrybig.javacoding.teamspeakconnector.repository;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.OnlineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowOfflineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedChannel;
import me.ferrybig.javacoding.teamspeakconnector.internal.Mapper;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Fernando
 */
public class OnlineClientRepositoryTest {

	@Test
	public void testReadEntity() throws NoSuchFieldException, IllegalAccessException {
		Map<String, String> data = new HashMap<>();
		data.put("client_channel_group_id", "8");
		data.put("client_output_muted", "0");
		data.put("client_nickname", "TestingBot");
		data.put("client_is_channel_commander", "0");
		data.put("client_platform", "ServerQuery");
		data.put("client_type", "1");
		data.put("client_unique_identifier", "serveradmin");
		data.put("client_database_id", "1");
		data.put("client_output_hardware", "0");
		data.put("client_version", "ServerQuery");
		data.put("client_icon_id", "0");
		data.put("client_is_talker", "0");
		data.put("client_created", "0");
		data.put("client_idle_time", "64");
		data.put("clid", "6");
		data.put("client_talk_power", "0");
		data.put("client_flag_talking", "0");
		data.put("connection_client_ip", "");
		data.put("client_input_muted", "0");
		data.put("client_is_recording", "0");
		data.put("client_is_priority_speaker", "0");
		data.put("client_lastconnected", "0");
		data.put("client_channel_group_inherited_channel_id", "1");
		data.put("client_input_hardware", "0");
		data.put("client_country", "");
		data.put("client_away_message", "");
		data.put("client_servergroups", "2");
		data.put("cid", "1");
		data.put("client_away", "0");
		TeamspeakConnection con = mock(TeamspeakConnection.class);
		ChannelRepository channels = mock(ChannelRepository.class);
		GroupRepository groups = mock(GroupRepository.class);
		OfflineClientRepository offlineClients = mock(OfflineClientRepository.class);
		UnresolvedChannel channel = mock(UnresolvedChannel.class);
		ShallowOfflineClient offlineClient = mock(ShallowOfflineClient.class);

		Field mapper = TeamspeakConnection.class.getDeclaredField("mapper");
		mapper.setAccessible(true);
		mapper.set(con, new Mapper(con));

		when(con.channels()).thenReturn(channels);
		when(con.groups()).thenReturn(groups);
		when(con.offlineClients()).thenReturn(offlineClients);
		when(channels.unresolved(anyInt())).thenReturn(channel);
		when(channels.unresolvedOrNull(anyInt())).thenReturn(channel);
		when(offlineClients.readEntityShallow(any())).thenReturn(offlineClient);
		when(offlineClient.getUniqueIdentifier()).thenReturn("foobarbaz");
		when(offlineClient.getNickname()).thenReturn("bazbarfoo");

		OnlineClientRepository repo = new OnlineClientRepository(con);

		OnlineClient user = repo.readEntityChecked(data);

		assertNotNull(user);
		

	}

}
