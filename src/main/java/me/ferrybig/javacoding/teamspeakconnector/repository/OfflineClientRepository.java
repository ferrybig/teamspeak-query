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
package me.ferrybig.javacoding.teamspeakconnector.repository;

import io.netty.util.concurrent.Future;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.OfflineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.ShallowOfflineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedOfflineClient;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedOfflineClientWithUid;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout
 */
@ParametersAreNonnullByDefault
@ThreadSafe
public class OfflineClientRepository extends AbstractIntResolvableRepository<UnresolvedOfflineClient, OfflineClient> {

	public OfflineClientRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public UnresolvedOfflineClient unresolved(int id) {
		return new UnresolvedOfflineClient(this, id);
	}

	@Nonnull
	public UnresolvedOfflineClientWithUid unresolvedByUniqueId(String uid) {
		return new UnresolvedOfflineClientWithUidImpl(uid);
	}

	@Override
	protected int getId(UnresolvedOfflineClient value) {
		return value.getDatabaseId();
	}

	@Nonnull
	protected ShallowOfflineClient readEntityShallow(Map<String, String> data) {
		return new ShallowOfflineClient(this,
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_unique_identifier"),
				data.get("client_nickname"),
				Long.parseLong(data.get("client_created")),
				// Long.parseLong(data.get("client_lastconnected")),
				connection.mapping().tryConvertAddress(data.get("connection_client_ip")));
	}

	@Override
	protected OfflineClient readEntity(Map<String, String> data) {
		return new OfflineClient(this,
				Integer.parseInt(data.get("client_database_id")),
				data.get("client_unique_identifier"),
				data.get("client_nickname"),
				Long.parseLong(data.get("client_created")),
				Long.parseLong(data.get("client_lastconnected")),
				Integer.parseInt(data.get("client_totalconnections")),
				data.get("client_description"),
				connection.mapping().tryConvertAddress(data.get("connection_client_ip")));
	}

	@Override
	protected ComplexRequest requestList() {
		return Command.CLIENTDB_LIST.build();
	}

	@Override
	protected ComplexRequest requestGet(UnresolvedOfflineClient unresolved) {
		return null;
	}

	@Override
	protected ComplexRequest requestDelete(UnresolvedOfflineClient unresolved, boolean force) {
		return Command.CLIENTDB_DELETE
				.addData("cldbid", unresolved.getDatabaseId())
				.build();
	}

	private final class UnresolvedOfflineClientWithUidImpl
			implements UnresolvedOfflineClientWithUid {

		private final String uniqueIdentifier;

		public UnresolvedOfflineClientWithUidImpl(String uniqueIdentifier) {
			this.uniqueIdentifier = uniqueIdentifier;
		}

		@Override
		public String getUniqueIdentifier() {
			return uniqueIdentifier;
		}

		@Override
		public Future<OfflineClient> resolve() {
			return connection.io().chainFutureFlat(
					resolveTillDatabaseId(),
					UnresolvedOfflineClient::resolve
			);
		}

		@Override
		public Future<OfflineClient> forceResolve() {
			return connection.io().chainFutureFlat(
					resolveTillDatabaseId(),
					UnresolvedOfflineClient::forceResolve
			);
		}

		@Override
		public boolean isResolved() {
			return false;
		}

		@Override
		public Future<UnresolvedOfflineClient> resolveTillDatabaseId() {
			return connection.io().chainFuture(connection.io().sendPacket(Command.CLIENT_GET_DBID_FROM_UID
					.addData("cluid", getUniqueIdentifier())
					.build()),
					p -> new UnresolvedOfflineClient(OfflineClientRepository.this,
							Integer.parseInt(p.getCommands().get(0).get("cldbid")))
			);

		}

	}

}
