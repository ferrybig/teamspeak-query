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

/**
 *
 * @author Fernando
 */
public class Group extends UnresolvedGroup {

	private final int icon;
	private final boolean savedb;
	private final String name;
	private final int memberRemovePrivilege;
	private final int memberAddPrivilege;
	private final int modifyPrivilege;
	private final int namemode;

	public Group(int icon, boolean savedb, String name, int memberRemovePrivilege, int memberAddPrivilege, int modifyPrivilege, int namemode, TeamspeakConnection con, int serverGroupId) {
		super(con, serverGroupId);
		this.icon = icon;
		this.savedb = savedb;
		this.name = name;
		this.memberRemovePrivilege = memberRemovePrivilege;
		this.memberAddPrivilege = memberAddPrivilege;
		this.modifyPrivilege = modifyPrivilege;
		this.namemode = namemode;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	public enum Type {
		TEMPLATE(0), // 0: template group (used for new virtual servers)
		REGULAR(1), // 1: regular group (used for regular clients)
		SERVERQUERY(2); // 2: global query group (used for ServerQuery clients)

		private final int id;

		private Type(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

	};
}
