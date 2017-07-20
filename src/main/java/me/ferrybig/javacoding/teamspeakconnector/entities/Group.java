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
package me.ferrybig.javacoding.teamspeakconnector.entities;

import io.netty.util.concurrent.Future;
import java.util.Objects;
import me.ferrybig.javacoding.teamspeakconnector.TeamspeakConnection;

/**
 * This class represents a Teamspeak server group. A server group has a type,
 * this type says what purpose the the group has. Most implementation should
 * filter on the first type, as its used by normal clients.
 *
 * @see Type
 */
public class Group extends UnresolvedGroup {

	private final int sortId;
	private final int icon;
	private final boolean savedb;
	private final String name;
	private final int memberRemovePrivilege;
	private final int memberAddPrivilege;
	private final int modifyPrivilege;
	private final int namemode;
	private final Type type;

	/**
	 * Creates a new Teamspeak group, should only be used by the internal api.
	 *
	 * @param con Teamspeak connection that created this object
	 * @param serverGroupId The id of this group
	 * @param sortId The sort id
	 * @param icon Icon id of this group, or -1 for no icon
	 * @param savedb unknown
	 * @param name Name of the group
	 * @param memberRemovePrivilege Privilege to remove a member to this group
	 * @param memberAddPrivilege Privilege to add a member to this group
	 * @param modifyPrivilege Privilege to modify this group
	 * @param namemode unknown
	 * @param type Type of the group
	 */
	public Group(TeamspeakConnection con, int serverGroupId, int sortId, int icon, boolean savedb, String name, int memberRemovePrivilege, int memberAddPrivilege, int modifyPrivilege, int namemode, Type type) {
		super(con, serverGroupId);
		this.sortId = sortId;
		this.icon = icon;
		this.savedb = savedb;
		this.name = Objects.requireNonNull(name, "name");
		this.memberRemovePrivilege = memberRemovePrivilege;
		this.memberAddPrivilege = memberAddPrivilege;
		this.modifyPrivilege = modifyPrivilege;
		this.namemode = namemode;
		this.type = type;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	/**
	 * Returns the sort id, if displaying server groups, this should be first
	 * sorted on, before sorting on the id.
	 *
	 * @return the sort id
	 */
	public int getSortId() {
		return sortId;
	}

	/**
	 * Returns the id of the icon used
	 *
	 * @return the id of the icon
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * Unknown (probability related to default group)
	 *
	 * @return an unknown value observed to be 0 or 1, and it seams to be related to default group
	 */
	public boolean isSavedb() {
		return savedb;
	}

	/**
	 * Returns the name of the group
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the privilege to remove a member of the group
	 *
	 * @return the value of privilege needed to remove members
	 */
	public int getMemberRemovePrivilege() {
		return memberRemovePrivilege;
	}

	/**
	 * Gets the privilege level needed to add a member to the group
	 *
	 * @return the value of privilege needed to add a member to the group
	 */
	public int getMemberAddPrivilege() {
		return memberAddPrivilege;
	}

	/**
	 * Gets the privilege needed to modify this group
	 *
	 * @return the value of privilege needed to modify the group
	 */
	public int getModifyPrivilege() {
		return modifyPrivilege;
	}

	/**
	 * Unknown
	 *
	 * @return unknown
	 */
	public int getNamemode() {
		return namemode;
	}

	/**
	 * Returns the type of the group
	 *
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Group{" + "serverGroupId=" + getServerGroupId() + ", icon=" + icon + ", savedb=" + savedb + ", name=" + name + ", memberRemovePrivilege=" + memberRemovePrivilege + ", memberAddPrivilege=" + memberAddPrivilege + ", modifyPrivilege=" + modifyPrivilege + ", namemode=" + namemode + ", type=" + type + '}';
	}

	@Override
	public Future<Group> resolve() {
		return con.io().getCompletedFuture(this);
	}

	/**
	 * Type of server groups observed in Teamspeak
	 */
	public enum Type {
		/**
		 * 0: template group (used for new virtual servers)
		 */
		TEMPLATE(0),
		/**
		 * 1: regular group (used for regular clients)
		 */
		REGULAR(1),
		/**
		 * 2: global query group (used for ServerQuery clients)
		 */
		SERVERQUERY(2);

		private static final int BY_ID_LENGTH = 3;
		private static final Type[] BY_ID;

		static {
			BY_ID = new Type[BY_ID_LENGTH];
			for (Type type : values()) {
				BY_ID[type.id] = type;
			}
		}

		private final int id;

		private Type(int id) {
			this.id = id;
		}

		/**
		 * Gets the internal id of the type
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Gets a type y its id
		 *
		 * @param id the id to look for
		 * @return the type that matches the id
		 * @throws IllegalArgumentException if the id isn't mapped to a type
		 */
		public static Type getById(int id) {
			if (id >= BY_ID_LENGTH || id < 0 || BY_ID[id] == null) {
				throw new IllegalArgumentException("No type found for id " + id);
			}
			return BY_ID[id];
		}

	};
}
