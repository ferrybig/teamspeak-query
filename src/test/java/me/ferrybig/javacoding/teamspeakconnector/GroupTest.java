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

import me.ferrybig.javacoding.teamspeakconnector.entities.Group;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Fernando van Loenhout
 */
public class GroupTest {

	@Test
	public void testIsResolved() {
		Group group = new Group(null, 0, 0, 0, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertTrue(group.isResolved());
	}

	@Test
	public void testGetSortId() {
		Group group = new Group(null, 0, 20, 0, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(20, group.getSortId());
	}

	@Test
	public void testGetSortIdAlternative() {
		Group group = new Group(null, 0, 10, 0, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(10, group.getSortId());
	}

	@Test
	public void testGetIcon() {
		Group group = new Group(null, 0, 0, 30, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(30, group.getIcon());
	}

	@Test
	public void testGetIconAlternative() {
		Group group = new Group(null, 0, 0, 40, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(40, group.getIcon());
	}

	@Test
	public void testIsSavedb() {
		Group group = new Group(null, 0, 0, 30, false, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(false, group.isSavedb());
	}

	@Test
	public void testIsSavedbTrue() {
		Group group = new Group(null, 0, 0, 30, true, "", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(true, group.isSavedb());
	}

	@Test
	public void testGetName() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals("bar", group.getName());
	}

	@Test
	public void testGetNameAlternative() {
		Group group = new Group(null, 0, 0, 30, true, "foo", 0, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals("foo", group.getName());
	}

	@Test(expected = NullPointerException.class)
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void testGetNameNull() {
		new Group(null, 0, 0, 30, true, null, 0, 0, 0, 0, Group.Type.TEMPLATE);
	}

	@Test
	public void testGetMemberRemovePrivilege() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 10, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(10, group.getMemberRemovePrivilege());
	}

	@Test
	public void testGetMemberRemovePrivilegeAkternate() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 90, 0, 0, 0, Group.Type.TEMPLATE);
		assertEquals(90, group.getMemberRemovePrivilege());
	}

	@Test
	public void testGetMemberAddPrivilege() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 70, 0, 0, Group.Type.TEMPLATE);
		assertEquals(70, group.getMemberAddPrivilege());
	}

	@Test
	public void testGetMemberAddPrivilegeAlternate() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 50, 0, 0, Group.Type.TEMPLATE);
		assertEquals(50, group.getMemberAddPrivilege());
	}

	@Test
	public void testGetModifyPrivilege() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 0, 10, 0, Group.Type.TEMPLATE);
		assertEquals(10, group.getModifyPrivilege());
	}

	@Test
	public void testGetModifyPrivilegeAlternate() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 0, 109, 0, Group.Type.TEMPLATE);
		assertEquals(109, group.getModifyPrivilege());
	}

	@Test
	public void testGetType() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 0, 109, 0, Group.Type.TEMPLATE);
		assertEquals(Group.Type.TEMPLATE, group.getType());
	}

	@Test
	public void testGetTypeAlternate() {
		Group group = new Group(null, 0, 0, 30, true, "bar", 0, 0, 109, 0, Group.Type.REGULAR);
		assertEquals(Group.Type.REGULAR, group.getType());
	}

}
