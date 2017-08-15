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

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;

/**
 *
 * @author Fernando van Loenhout
 */
public abstract class AbstractConnectionBasedIT {

	private static final Logger LOG = Logger.getLogger(TeamspeakConnectionIT.class.getName());

	private NioEventLoopGroup group;

	@Before
	public void before() {
		group = new NioEventLoopGroup(1);
	}

	@After
	public void after() {
		group.shutdownGracefully();
	}

	protected TeamspeakBootstrap creatBootstrap() {
		TeamspeakBootstrap ts = new TeamspeakBootstrap(group);

		String username = System.getProperty("teamspesk3.username", "");
		if (username.isEmpty()) {
			username = "serveradmin";
		}
		String password = System.getProperty("teamspeak3.password", "");
		if (password.isEmpty()) {
			password = "test1234";
		}
		ts.login(username, password);
		ts.selectServerID(1);
		ts.clientName("TestingBot");
		return ts;
	}

	protected Future<TeamspeakConnection> createConnection() {
		return createConnection("TestingBot");
	}

	protected Future<TeamspeakConnection> createConnection(String clientname) {
		String hostname = System.getProperty("teamspeak3.hostname", "");
		if (hostname.isEmpty()) {
			hostname = "127.0.0.1";
		}
		int port = Integer.parseInt(System.getProperty("teamspeak3.port", "").isEmpty()
				? String.valueOf(TeamspeakBootstrap.DEFAULT_QUERY_PORT)
				: System.getProperty("teamspeak3.port"));
		return creatBootstrap().clientName(clientname).connect(hostname, port);
	}

	@SafeVarargs
	protected final void assumeConnectionWorking(Future<TeamspeakConnection>... cons)
			throws InterruptedException {
		if (Boolean.valueOf(System.getProperty("teamspeak3.required"))) {
			return;
		}
		for (Future<TeamspeakConnection> con : cons) {
			con.await();
			if (!con.isSuccess()) {
				if (con.cause().getCause() instanceof ConnectException) {
					Assume.assumeFalse("Cannot connect to teamspeak server, "
							+ "check the maven vales teamspeak3.hostname, "
							+ "teamspeak3.port, teamspeak3.username, "
							+ "teamspeak3.password", true);
				}
			}
		}
	}
}
