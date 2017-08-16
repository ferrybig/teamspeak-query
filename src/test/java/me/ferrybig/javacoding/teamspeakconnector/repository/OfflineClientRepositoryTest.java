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
import me.ferrybig.javacoding.teamspeakconnector.entities.OfflineClient;
import me.ferrybig.javacoding.teamspeakconnector.internal.Mapper;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Fernando
 */
public class OfflineClientRepositoryTest {

	@Test
	public void testReadEntity() throws NoSuchFieldException, IllegalAccessException {
		Map<String, String> data = new HashMap<>();

		data.put("cldbid", "2");
		data.put("client_lastconnected", "1500920715");
		data.put("client_totalconnections", "195");
		data.put("client_nickname", "Ferrybig");
		data.put("client_created", "1397863056");
		data.put("client_unique_identifier", "P+m/uXn4o2nLwN5gOimuQcfQZIQ=");
		data.put("client_lastip", "127.0.0.1");
		data.put("client_description", "");
		TeamspeakConnection con = mock(TeamspeakConnection.class);

		Field mapper = TeamspeakConnection.class.getDeclaredField("mapper");
		mapper.setAccessible(true);
		mapper.set(con, new Mapper(con));

		OfflineClientRepository repo = new OfflineClientRepository(con);

		OfflineClient user = repo.readEntityChecked(data);

		assertNotNull(user);

	}

}
