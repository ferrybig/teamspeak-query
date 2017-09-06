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
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Response;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PacketDecoderTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
				new Object[]{"error", new Response[]{
					new Response(Collections.emptyMap(), "error")}},
				new Object[]{"error id=256 msg=command\\snot\\sfound", new Response[]{
					new Response(map("id", "256", "msg", "command not found"), "error")}},
				new Object[]{"cid=1 pid=1", new Response[]{
					new Response(map("cid", "1", "pid", "1"), "")}},
				new Object[]{"cid=1 password", new Response[]{
					new Response(map("cid", "1", "password", ""), "")}},
				new Object[]{"cid=1 virtualserver_name=TeamSpeak\\s]\\p[\\sServer", new Response[]{
					new Response(map("cid", "1", "virtualserver_name", "TeamSpeak ]|[ Server"), "")}}
		);
	}

	private static <K, V> Map<K, V> map(K k1, V v1, K k2, V v2) {
		Map<K, V> map = new HashMap<>(2);
		map.put(k1, v1);
		map.put(k2, v2);
		return map;
	}

	private final String in;

	private final Response[] out;

	public PacketDecoderTest(String in, Response[] out) {
		this.in = in;
		this.out = out;
	}

	@Test
	public void decodeSimple() {
		EmbeddedChannel channel = new EmbeddedChannel(new PacketDecoder());
		channel.writeInbound(in);
		for (int i = 0; i < out.length; i++) {
			assertEquals(out[i], channel.readInbound());
		}
		assertNull(channel.readInbound());
	}

}
