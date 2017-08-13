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

import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RateLimitAdvancedDataTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		EmbeddedChannel channel = new EmbeddedChannel();
		channel.close();
		return Arrays.asList(
				new Object[]{RateLimit.AUTODETECT, new InetSocketAddress("2001:db:08::2001", 0), 10 / 3.5},
				new Object[]{RateLimit.AUTODETECT, new InetSocketAddress("10.10.10.10", 0), 10 / 3.5},
				new Object[]{RateLimit.AUTODETECT, new InetSocketAddress("::1", 0), 10 / 3.5},
				new Object[]{RateLimit.AUTODETECT, new InetSocketAddress("127.0.0.1", 0), 0},
				new Object[]{RateLimit.AUTODETECT, channel.remoteAddress(), 10 / 3.5},
				new Object[]{RateLimit.AUTODETECT, null, 10 / 3.5},
				new Object[]{RateLimit.LIMITED, new InetSocketAddress("2001:db:08::2001", 0), 10 / 3.5},
				new Object[]{RateLimit.LIMITED, new InetSocketAddress("10.10.10.10", 0), 10 / 3.5},
				new Object[]{RateLimit.LIMITED, new InetSocketAddress("127.0.0.1", 0), 10 / 3.5},
				new Object[]{RateLimit.LIMITED, new InetSocketAddress("::1", 0), 10 / 3.5},
				new Object[]{RateLimit.LIMITED, channel.remoteAddress(), 10 / 3.5},
				new Object[]{RateLimit.LIMITED, null, 10 / 3.5},
				new Object[]{RateLimit.UNLIMITED, new InetSocketAddress("2001:db:08::2001", 0), 0},
				new Object[]{RateLimit.UNLIMITED, new InetSocketAddress("10.10.10.10", 0), 0},
				new Object[]{RateLimit.UNLIMITED, new InetSocketAddress("127.0.0.1", 0), 0},
				new Object[]{RateLimit.UNLIMITED, new InetSocketAddress("::1", 0), 0},
				new Object[]{RateLimit.UNLIMITED, channel.remoteAddress(), 0},
				new Object[]{RateLimit.UNLIMITED, null, 0}
		);
	}

	@Parameterized.Parameter(value = 0)
	public RateLimit limit;

	@Parameterized.Parameter(value = 1)
	public SocketAddress address;

	@Parameterized.Parameter(value = 2)
	public double expected;

	@Test
	public void testValue() {
		Assert.assertEquals(expected, limit.maxPacketsPerSecond(address), 0.1);
	}

}
