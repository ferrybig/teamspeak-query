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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;
import me.ferrybig.javacoding.teamspeakconnector.entities.PrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.entities.UnresolvedPrivilegeKey;
import me.ferrybig.javacoding.teamspeakconnector.internal.handler.PacketDecoder;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.Command;
import me.ferrybig.javacoding.teamspeakconnector.internal.packets.ComplexRequest;

/**
 *
 * @author Fernando van Loenhout
 */
public class PrivilegeKeyRepository
		extends AbstractObjectResolvableRepository<UnresolvedPrivilegeKey, PrivilegeKey, String> {

	public PrivilegeKeyRepository(TeamspeakConnection connection) {
		super(connection);
	}

	@Override
	public UnresolvedPrivilegeKey unresolved(String id) {
		return new UnresolvedPrivilegeKey(this, id);
	}

	@Override
	protected String getId(UnresolvedPrivilegeKey value) {
		return value.getToken();
	}

	@Nonnull
	public PrivilegeKey readEntityFromEvent(Map<String, String> data) {
		data.put("token_id1", data.get("token1"));
		data.put("token_id2", data.get("token2"));
		return readEntity(data);
	}

	@Override
	protected PrivilegeKey readEntity(Map<String, String> data) {
		Map<String, String> customSet = new HashMap<>();
		final String tokenCustomSetString = data.get("tokencustomset");
		if (tokenCustomSetString != null && !tokenCustomSetString.isEmpty()) {
			Map<String, String> customSetCache = new HashMap<>(2);
			for (String part : tokenCustomSetString.split("\\|")) {
				PacketDecoder.singleDecode(part, customSetCache, true, true);
				customSet.put(
						customSetCache.get("ident"),
						customSetCache.get("value"));
			}
		}
		return new PrivilegeKey(this,
				data.get("token"),
				customSet,
				data.getOrDefault("tokendescription", ""),
				PrivilegeKey.Type.getById(Integer.parseInt(
						data.getOrDefault("token_type",
								"0".equals(data.get("tokenid2")) ? "0" : "1"))),
				Integer.parseInt(data.get("token_id1")),
				Integer.parseInt(data.get("token_id2")));
	}

	@Override
	protected ComplexRequest requestList() {
		return Command.PRIVILEGEKEY_LIST.build();
	}

	@Override
	protected ComplexRequest requestGet(UnresolvedPrivilegeKey unresolved) {
		return null;
	}

	@Override
	protected ComplexRequest requestDelete(
			UnresolvedPrivilegeKey unresolved, boolean force) {
		return Command.PRIVILEGEKEY_DELETE
				.addData("token", getId(unresolved))
				.build();
	}

}
