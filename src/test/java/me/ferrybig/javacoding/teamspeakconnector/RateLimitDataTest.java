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
package me.ferrybig.javacoding.teamspeakconnector;

import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RateLimitDataTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
				new Object[]{RateLimit.AUTODETECT, "RateLimit.AUTODETECT", new InetSocketAddress("::1", 0), new InetSocketAddress("127.0.0.1", 0), -1},
				new Object[]{RateLimit.LIMITED, "RateLimit.LIMITED", new InetSocketAddress("::1", 0), new InetSocketAddress("127.0.0.1", 0), 0},
				new Object[]{RateLimit.UNLIMITED, "RateLimit.UNLIMITED", new InetSocketAddress("::1", 0), new InetSocketAddress("127.0.0.1", 0), 0}
		);
	}

	@Parameterized.Parameter(value = 0)
	public RateLimit limit;

	@Parameterized.Parameter(value = 1)
	public String toString;

	@Parameterized.Parameter(value = 2)
	public SocketAddress compareFirst;

	@Parameterized.Parameter(value = 3)
	public SocketAddress compareSecond;

	@Parameterized.Parameter(value = 4)
	public int expected;

	@Test
	public void toStringTest() {
		Assert.assertEquals(toString, limit.toString());
	}

	@Test
	public void compareTest() {
		final Comparator<SocketAddress> socketAddressComparator = limit.socketAddressComparator();
		Assert.assertEquals(expected, socketAddressComparator.compare(compareFirst, compareSecond));
		Assert.assertEquals(-expected, socketAddressComparator.compare(compareSecond, compareFirst));
	}

}
