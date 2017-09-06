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

import java.util.HashMap;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.Channel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Fernando
 */
public class ChannelRepositoryTest {

	@Test
	public void testReadEntity() {
		Map<String, String> data = new HashMap<>();
		data.put("cid", "1");
		data.put("pid", "0");
		data.put("channel_name", "Default Channel");
		data.put("channel_topic", "Default Channel has no topic");
		data.put("channel_description", "This is the default channel");
		data.put("channel_password", "");
		data.put("channel_codec", "4");
		data.put("channel_codec_quality", "6");
		data.put("channel_maxclients", "-1");
		data.put("channel_maxfamilyclients", "-1");
		data.put("channel_order", "0");
		data.put("channel_flag_permanent", "1");
		data.put("channel_flag_semi_permanent", "0");
		data.put("channel_flag_default", "1");
		data.put("channel_flag_password", "0");
		data.put("channel_codec_latency_factor", "1");
		data.put("channel_codec_is_unencrypted", "1");
		data.put("channel_security_salt", "");
		data.put("channel_delete_delay", "0");
		data.put("channel_flag_maxclients_unlimited", "1");
		data.put("channel_flag_maxfamilyclients_unlimited", "1");
		data.put("channel_flag_maxfamilyclients_inherited", "0");
		data.put("channel_filepath", "files/virtualserver_1/channel_1");
		data.put("channel_needed_talk_power", "0");
		data.put("channel_forced_silence", "0");
		data.put("channel_name_phonetic", "");
		data.put("channel_icon_id", "0");
		data.put("channel_flag_private", "0");
		data.put("seconds_empty", "-1");
		TeamspeakConnection con = mock(TeamspeakConnection.class);
		ChannelRepository repo = new ChannelRepository(con);
		
		Channel channel = repo.readEntityChecked(data);

		assertNotNull(channel);
		assertEquals(1, channel.getId());
		assertEquals(Channel.Codec.getById(4), channel.getCodec());
		assertEquals(6, channel.getCodecQuality());
		assertEquals(0, channel.getIconId());
		assertEquals("Default Channel", channel.getName());
		assertEquals(-1, channel.getMaxClients());
		assertEquals(0, channel.getOrder());
		assertEquals(null, channel.getParent());
		assertEquals("Default Channel has no topic", channel.getTopic());
		assertEquals(0, channel.getTotalClients());
		assertEquals(true, channel.isDefaultChannel());
		assertEquals(false, channel.isPassword());
		assertEquals(true, channel.isPermanent());
		assertEquals(false, channel.isSemiPermanent());

	}

}
