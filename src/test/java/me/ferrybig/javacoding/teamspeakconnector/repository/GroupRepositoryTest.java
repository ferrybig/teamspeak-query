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
import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import me.ferrybig.javacoding.teamspeakconnector.internal.Mapper;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Fernando
 */
public class GroupRepositoryTest {

	@Test
	public void testReadEntity() throws NoSuchFieldException, IllegalAccessException {
		Map<String, String> data = new HashMap<>();
		data.put("iconid", "0");
		data.put("savedb", "0");
		data.put("sortid", "0");
		data.put("name", "Guest Server Query");
		data.put("n_member_removep", "0");
		data.put("sgid", "1");
		data.put("type", "2");
		data.put("n_member_addp", "0");
		data.put("namemode", "0");
		data.put("n_modifyp", "100");
		TeamspeakConnection con = mock(TeamspeakConnection.class);

		Field mapper = TeamspeakConnection.class.getDeclaredField("mapper");
		mapper.setAccessible(true);
		mapper.set(con, new Mapper(con));

		GroupRepository repo = new GroupRepository(con);

		Group group = repo.readEntityChecked(data);

		assertNotNull(group);
	}

	@Ignore
	public void testReadEntity1() throws NoSuchFieldException, IllegalAccessException {
		Map<String, String> data = new HashMap<>();
		data.put("iconid", "0");
		data.put("savedb", "0");
		data.put("sortid", "0");
		data.put("name", "Guest");
		data.put("n_member_removep", "0");
		data.put("sgid", "8");
		data.put("type", "1");
		data.put("n_member_addp", "0");
		data.put("namemode", "0");
		data.put("n_modifyp", "75");
		TeamspeakConnection con = mock(TeamspeakConnection.class);

		Field mapper = TeamspeakConnection.class.getDeclaredField("mapper");
		mapper.setAccessible(true);
		mapper.set(con, new Mapper(con));

		GroupRepository repo = new GroupRepository(con);

		Group group = repo.readEntityChecked(data);

		assertNotNull(group);
	}

}
