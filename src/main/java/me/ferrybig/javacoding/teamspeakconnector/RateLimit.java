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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Comparator;
import javax.annotation.Nonnegative;

/**
 * Ratelimit specification for the flood protection.
 */
@FunctionalInterface
public interface RateLimit {

	/**
	 * An unlimited RateLimit, this one returns {@code 0l} always. Use this if
	 * your ip is listed inside the whitelist.
	 */
	public static final RateLimit UNLIMITED = new RateLimit() {
		@Override
		public double maxPacketsPerSecond(SocketAddress s) {
			return 0;
		}

		@Override
		public String toString() {
			return "RateLimit.UNLIMITED";
		}
	};
	/**
	 * A RateLimit configured to be sligthy slower than the max packets of
	 * Teamspeak. This one allows for 10 commands every 3.5 seconds, while
	 * Teamspeak starts to disconnect the client at 10 commands every 3 seconds.
	 */
	public static final RateLimit LIMITED = new RateLimit() {
		@Override
		public double maxPacketsPerSecond(SocketAddress s) {
			return 10 / 3.5;
		}

		@Override
		public String toString() {
			return "RateLimit.LIMITED";
		}
	};
	/**
	 * An autodetecting RateLimit. This one detect if the ip address connected
	 * to matches 127.0.0.1 (no ipv6 as Teamspeak, as Teamspeak doesn't come
	 * with with ::1 inside the whitelist file by default.), and if it is, it
	 * will be using the {@link RateLimit#UNLIMITED} else it uses the
	 * {@link RateLimit#LIMITED} one.
	 */
	public static final RateLimit AUTODETECT = new RateLimit() {
		@Override
		public double maxPacketsPerSecond(SocketAddress s) {
			return (s instanceof InetSocketAddress
					&& ((InetSocketAddress) s).getAddress().getHostAddress()
							.equals("127.0.0.1") ? UNLIMITED : LIMITED)
					.maxPacketsPerSecond(s);
		}

		@Override
		public String toString() {
			return "RateLimit.AUTODETECT";
		}
	};

	/**
	 * Gets the maximum packets allowed every second, or a special value of 0
	 * detonating no maximum.
	 *
	 * @param remoteAddress the remote address of the connection
	 * @return the maximum packets allowed every second.
	 */
	@Nonnegative
	public double maxPacketsPerSecond(SocketAddress remoteAddress);

	/**
	 * A comparator that returns the highest throughput (in terms of commands
	 * per second) {@code SocketAddress}es first
	 *
	 * @return A comparator that returns the best {@code SocketAddress}es first
	 * @see SocketAddress
	 * @see RateLimit#maxPacketsPerSecond(java.net.SocketAddress)
	 */
	public default Comparator<SocketAddress> socketAddressComparator() {
		return (SocketAddress o1, SocketAddress o2) -> {
			double l1 = maxPacketsPerSecond(o1);
			double l2 = maxPacketsPerSecond(o2);
			if (l1 <= 0) {
				l1 = Double.MAX_VALUE;
			}
			if (l2 <= 0) {
				l2 = Double.MAX_VALUE;
			}
			return Double.compare(l1, l2);
		};
	}

}
