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
package me.ferrybig.javacoding.teamspeakconnector.internal.packets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A enum containing all the commands in Teamspeak 3. TODO: Both
 * `servergroupsbyclientid` and `servergroupbyclientid` have the same example,
 * only a bit different usage, is this a mistake in the original documentation?
 * Keeping the one referenced by `servergroupsbyclientid` for now.
 */
public enum Command {
	/**
	 * Adds a new ban rule on the selected virtual server. All parameters are
	 * optional but at least one of the following must be set: ip, name, or uid.
	 *
	 * Example:
	 * <pre>{@code
	 * banadd ip=1.2.3.4 banreason=just\s4\sfun
	 * banid=1
	 * }</pre>
	 *
	 * Usage: banadd [ip={regexp}] [name={regexp}] [uid={clientUID}]
	 * [time={timeInSeconds}] [banreason={text}]
	 */
	BAN_ADD("banadd",
			new HashSet<>(Arrays.asList("ip", "banreason", "name", "uid", "time"))
	),
	/**
	 * Bans the client specified with ID clid from the server. Please note that
	 * this will create two separate ban rules for the targeted clients IP
	 * address and his unique identifier.
	 *
	 * Example:
	 * <pre>{@code
	 * banclient clid=4 time=3600
	 * banid=2
	 * }</pre>
	 *
	 * Usage: banclient clid={clientID} [time={timeInSeconds}]
	 * [banreason={text}]
	 */
	BAN_CLIENT("banclient",
			new HashSet<>(Arrays.asList("clid", "time", "banreason"))
	),
	/**
	 * Deletes the ban rule with ID banid from the server.
	 *
	 * Example:
	 * <pre>{@code
	 * bandel banid=3
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: bandel banid={banID}
	 */
	BAN_DEL("bandel",
			new HashSet<>(Arrays.asList("banid"))
	),
	/**
	 * Deletes all active ban rules from the server.
	 *
	 * Example:
	 * <pre>{@code
	 * bandelall
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: bandelall
	 */
	BAN_DELALL("bandelall"),
	/**
	 * Displays a list of active bans on the selected virtual server.
	 *
	 * Example:
	 * <pre>{@code
	 * banlist
	 * banid=7 ip=1.2.3.4 created=1259444002242 invokername=Sven invokercldbid=56
	 * }</pre>
	 *
	 * Usage: banlist
	 */
	BAN_LIST("banlist"),
	/**
	 * Displays a list of IP addresses used by the server instance on
	 * multi-homed machines.
	 *
	 * Example:
	 * <pre>{@code
	 * bindinglist
	 * ip=0.0.0.0
	 * }</pre>
	 *
	 * Usage: bindinglist
	 */
	BINDING_LIST("bindinglist"),
	/**
	 * Adds a set of specified permissions to a channel. Multiple permissions
	 * can be added by providing the two parameters of each permission. A
	 * permission can be specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channeladdperm cid=16 permsid=i_client_needed_join_power permvalue=50
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channeladdperm cid={channelID} (
	 * permid={permID}|permsid={permName} permvalue={permValue} )...
	 */
	CHANNEL_ADD_PERM("channeladdperm",
			new HashSet<>(Arrays.asList("cid", "permsid", "permvalue", "(", "permid", ")"))
	),
	/**
	 * Adds a set of specified permissions to a client in a specific channel.
	 * Multiple permissions can be added by providing the two parameters of each
	 * permission. A permission can be specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channelclientaddperm cid=12 cldbid=3 permsid=i_icon_id permvalue=100
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelclientaddperm cid={channelID} cldbid={clientDBID} (
	 * permid={permID}|permsid={permName} permvalue={permValue} )...
	 */
	CHANNEL_CLIENT_ADD_PERM("channelclientaddperm",
			new HashSet<>(Arrays.asList("cid", "cldbid", "permsid", "permvalue", "(", "permid", ")"))
	),
	/**
	 * Removes a set of specified permissions from a client in a specific
	 * channel. Multiple permissions can be removed at once. A permission can be
	 * specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channelclientdelperm cid=12 cldbid=3 permsid=i_icon_id|permsid=b_icon_manage
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelclientdelperm cid={channelID} cldbid={clientDBID}
	 * permid={permID}|permsid={permName}...
	 */
	CHANNEL_CLIENT_DEL_PERM("channelclientdelperm",
			new HashSet<>(Arrays.asList("cid", "cldbid", "permsid", "permid"))
	),
	/**
	 * Displays a list of permissions defined for a client in a specific
	 * channel.
	 *
	 * Example:
	 * <pre>{@code
	 * channelclientpermlist cid=12 cldbid=3
	 * cid=12 cldbid=3 permid=4353 permvalue=1 permnegated=0 permskip=0|permid=17276
	 * }</pre>
	 *
	 * Usage: channelclientpermlist cid={channelID} cldbid={clientDBID}
	 * [-permsid]
	 */
	CHANNEL_CLIENT_PERM_LIST("channelclientpermlist",
			new HashSet<>(Arrays.asList("cid", "cldbid")),
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Creates a new channel using the given properties and displays its ID.
	 *
	 * Example:
	 * <pre>{@code
	 * channelcreate channel_name=My\sChannel channel_topic=My\sTopic
	 * cid=16
	 * }</pre>
	 *
	 * Usage: channelcreate channel_name={channelName} [channel_properties...]
	 */
	CHANNEL_CREATE("channelcreate",
			new HashSet<>(Arrays.asList("channel_name", "channel_topic", "channel_properties"))
	),
	/**
	 * Deletes an existing channel by ID. If force is set to 1, the channel will
	 * be deleted even if there are clients within.
	 *
	 * Example:
	 * <pre>{@code
	 * channeldelete cid=16 force=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channeldelete cid={channelID} force={1|0}
	 */
	CHANNEL_DELETE("channeldelete",
			new HashSet<>(Arrays.asList("cid", "force"))
	),
	/**
	 * Removes a set of specified permissions from a channel. Multiple
	 * permissions can be removed at once. A permission can be specified by
	 * permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channeldelperm cid=16 permsid=i_icon_id|permsid=i_client_needed_talk_power
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channeldelperm cid=123 permid={permID}|permsid={permName}...
	 */
	CHANNEL_DEL_PERM("channeldelperm",
			new HashSet<>(Arrays.asList("cid", "permsid", "permid"))
	),
	/**
	 * Changes a channels configuration using given properties.
	 *
	 * Example:
	 * <pre>{@code
	 * channeledit cid=15 channel_codec_quality=3 channel_description=My\stext
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channeledit cid={channelID} [channel_properties...]
	 */
	CHANNEL_EDIT("channeledit",
			new HashSet<>(Arrays.asList("cid", "channel_codec_quality", "channel_description", "channel_properties"))
	),
	/**
	 * Displays a list of channels matching a given name pattern.
	 *
	 * Example:
	 * <pre>{@code
	 * channelfind pattern=default
	 * cid=15 channel_name=Default\sChannel
	 * }</pre>
	 *
	 * Usage: channelfind [pattern={channelName}]
	 */
	CHANNEL_FIND("channelfind",
			new HashSet<>(Arrays.asList("pattern"))
	),
	/**
	 * Creates a new channel group using a given name and displays its ID. The
	 * optional type parameter can be used to create ServerQuery groups and
	 * template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupadd name=Channel\sAdmin
	 * cgid=13
	 * }</pre>
	 *
	 * Usage: channelgroupadd name={groupName} [type={groupDbType}]
	 */
	CHANNEL_GROUP_ADD("channelgroupadd",
			new HashSet<>(Arrays.asList("name", "type"))
	),
	/**
	 * Adds a set of specified permissions to a channel group. Multiple
	 * permissions can be added by providing the two parameters of each
	 * permission. A permission can be specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupaddperm cgid=78 permsid=b_icon_manage permvalue=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelgroupaddperm cgid={groupID} permid={permID}
	 * permvalue={permValue} channelgroupaddperm cgid={groupID}
	 * permsid={permName} permvalue={permValue}
	 */
	CHANNEL_GROUP_ADD_PERM("channelgroupaddperm",
			new HashSet<>(Arrays.asList("cgid", "permsid", "permvalue", "permid"))
	),
	/**
	 * Displays all the client and/or channel IDs currently assigned to channel
	 * groups. All three parameters are optional so you're free to choose the
	 * most suitable combination for your requirements.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupclientlist cid=2 cgid=9
	 * cid=2 cldbid=9 cgid=9|cid=2 cldbid=24 cgid=9|cid=2 cldbid=47 cgid=9
	 * }</pre>
	 *
	 * Usage: channelgroupclientlist [cid={channelID}] [cldbid={clientDBID}]
	 * [cgid={groupID}]
	 */
	CHANNEL_GROUP_CLIENT_LIST("channelgroupclientlist",
			new HashSet<>(Arrays.asList("cid", "cgid", "cldbid"))
	),
	/**
	 * Creates a copy of the channel group specified with ssgid. If tsgid is set
	 * to 0, the server will create a new group. To overwrite an existing group,
	 * simply set tsgid to the ID of a designated target group. If a target
	 * group is set, the name parameter will be ignored.
	 *
	 * The type parameter can be used to create ServerQuery and template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupcopy scgid=4 tcgid=0 name=My\sGroup\s(Copy) type=1
	 * cgid=13
	 * }</pre>
	 *
	 * Usage: channelgroupcopy scgid={sourceGroupID} tcgid={targetGroupID}
	 * name={groupName} type={groupDbType}
	 */
	CHANNEL_GROUP_COPY("channelgroupcopy",
			new HashSet<>(Arrays.asList("scgid", "tcgid", "name", "type"))
	),
	/**
	 * Deletes a channel group by ID. If force is set to 1, the channel group
	 * will be deleted even if there are clients within.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupdel cgid=13
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelgroupdel cgid={groupID} force={1|0}
	 */
	CHANNEL_GROUP_DEL("channelgroupdel",
			new HashSet<>(Arrays.asList("cgid", "force"))
	),
	/**
	 * Removes a set of specified permissions from the channel group. Multiple
	 * permissions can be removed at once. A permission can be specified by
	 * permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgroupdelperm cgid=16 permid=17276|permid=21415
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelgroupdelperm cgid={groupID} permid={permID}|...
	 * channelgroupdelperm cgid={groupID} permsid={permName}|...
	 */
	CHANNEL_GROUP_DEL_PERM("channelgroupdelperm",
			new HashSet<>(Arrays.asList("cgid", "permid", "permsid"))
	),
	/**
	 * Displays a list of channel groups available on the selected virtual
	 * server.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgrouplist
	 * cgid=1 name=Channel\sAdmin type=2 iconid=100 savedb=1|cgid=2 ...
	 * }</pre>
	 *
	 * Usage: channelgrouplist
	 */
	CHANNEL_GROUP_LIST("channelgrouplist"),
	/**
	 * Displays a list of permissions assigned to the channel group specified
	 * with cgid.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgrouppermlist cgid=13
	 * permid=8470 permvalue=1 permnegated=0 permskip=0|permid=8475 ...
	 * }</pre>
	 *
	 * Usage: channelgrouppermlist cgid={groupID} [-permsid]
	 */
	CHANNEL_GROUP_PERM_LIST("channelgrouppermlist",
			new HashSet<>(Arrays.asList("cgid")),
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Changes the name of a specified channel group.
	 *
	 * Example:
	 * <pre>{@code
	 * channelgrouprename cgid=13 name=New\sName
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelgrouprename cgid={groupID} name={groupName}
	 */
	CHANNEL_GROUP_RENAME("channelgrouprename",
			new HashSet<>(Arrays.asList("cgid", "name"))
	),
	/**
	 * Displays detailed configuration information about a channel including ID,
	 * topic, description, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * channelinfo cid=1
	 * channel_name=Default\sChannel channel_topic=No\s[b]topic[\/b]\shere
	 * }</pre>
	 *
	 * Usage: channelinfo cid={channelID}
	 */
	CHANNEL_INFO("channelinfo",
			new HashSet<>(Arrays.asList("cid"))
	),
	/**
	 * Displays a list of channels created on a virtual server including their
	 * ID, order, name, etc. The output can be modified using several command
	 * options.
	 *
	 * Example:
	 * <pre>{@code
	 * channellist -topic
	 * cid=15 pid=0 channel_order=0 channel_name=Default\sChannel
	 * }</pre>
	 *
	 * Usage: channellist [-topic] [-flags] [-voice] [-limits] [-icon]
	 * [-secondsempty]
	 */
	CHANNEL_LIST("channellist",
			Collections.emptySet(),
			new HashSet<>(Arrays.asList("topic", "flags", "voice", "limits", "icon", "secondsempty"))
	),
	/**
	 * Moves a channel to a new parent channel with the ID cpid. If order is
	 * specified, the channel will be sorted right under the channel with the
	 * specified ID. If order is set to 0, the channel will be sorted right
	 * below the new parent.
	 *
	 * Example:
	 * <pre>{@code
	 * channelmove cid=16 cpid=1 order=0
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channelmove cid={channelID} cpid={channelParentID}
	 * [order={channelSortOrder}]
	 */
	CHANNEL_MOVE("channelmove",
			new HashSet<>(Arrays.asList("cid", "cpid", "order"))
	),
	/**
	 * Displays a list of permissions defined for a channel.
	 *
	 * Example:
	 * <pre>{@code
	 * channelpermlist cid=2
	 * cid=2 permid=4353 permvalue=1 permnegated=0 permskip=0|permid=17276...
	 * }</pre>
	 *
	 * Usage: channelpermlist cid={channelID} [-permsid]
	 */
	CHANNEL_PERM_LIST("channelpermlist",
			new HashSet<>(Arrays.asList("cid")),
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Adds a set of specified permissions to a client. Multiple permissions can
	 * be added by providing the three parameters of each permission. A
	 * permission can be specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientaddperm cldbid=16 permsid=i_client_talk_power permvalue=5 permskip=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientaddperm cldbid={clientDBID} permid={permID}
	 * permvalue={permValue} permskip={1|0}|... clientaddperm
	 * cldbid={clientDBID} permsid={permName} permvalue={permValue}
	 * permskip={1|0}|...
	 */
	CLIENT_ADD_PERM("clientaddperm",
			new HashSet<>(Arrays.asList("cldbid", "permsid", "permvalue", "permskip", "permid"))
	),
	/**
	 * Deletes a clients properties from the database.
	 *
	 * Example:
	 * <pre>{@code
	 * clientdbdelete cldbid=56
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientdbdelete cldbid={clientDBID}
	 */
	CLIENTDB_DELETE("clientdbdelete",
			new HashSet<>(Arrays.asList("cldbid"))
	),
	/**
	 * Changes a clients settings using given properties.
	 *
	 * Example:
	 * <pre>{@code
	 * clientdbedit cldbid=56 client_description=Best\sguy\sever!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientdbedit cldbid={clientDBID} [client_properties...]
	 */
	CLIENTDB_EDIT("clientdbedit",
			new HashSet<>(Arrays.asList("cldbid", "client_description", "client_properties"))
	),
	/**
	 * Displays a list of client database IDs matching a given pattern. You can
	 * either search for a clients last known nickname or his unique identity by
	 * using the -uid option.
	 *
	 * Example:
	 * <pre>{@code
	 * clientdbfind pattern=sven
	 * cldbid=56
	 * }</pre>
	 *
	 * Usage: clientdbfind pattern={clientName|clientUID} [-uid]
	 */
	CLIENTDB_FIND("clientdbfind",
			new HashSet<>(Arrays.asList("pattern")),
			new HashSet<>(Arrays.asList("uid"))
	),
	/**
	 * Example:
	 * <pre>{@code
	 * clientdbinfo cldbid=2
	 * client_unique_identifier=5rRxyxEjd+Kk/MvPRfqZdSI0teA= client_nickname=dante696
	 * }</pre>
	 *
	 * Usage: clientdbinfo cldbid={clientDBID}
	 *
	 * Displays detailed database information about a client including unique
	 * ID, creation date, etc.
	 */
	CLIENTDB_INFO("clientdbinfo",
			new HashSet<>(Arrays.asList("cldbid", "Displays", "detailed", "database", "information", "about", "a", "client", "including", "unique", "ID,", "creation", "date,", "etc."))
	),
	/**
	 * Displays a list of client identities known by the server including their
	 * database ID, last nickname, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * clientdblist
	 * cldbid=7 client_unique_identifier=DZhdQU58qyooEK4Fr8Ly738hEmc=
	 * }</pre>
	 *
	 * Usage: clientdblist [start={offset}] [duration={limit}] [-count]
	 */
	CLIENTDB_LIST("clientdblist",
			new HashSet<>(Arrays.asList("start", "duration")),
			new HashSet<>(Arrays.asList("count"))
	),
	/**
	 * Removes a set of specified permissions from a client. Multiple
	 * permissions can be removed at once. A permission can be specified by
	 * permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientdelperm cldbid=16 permsid=i_icon_id|permsid=b_icon_manage
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: channeldelperm cldbid={clientDBID}
	 * permid={permID}|permsid={permName}...
	 */
	CLIENT_DEL_PERM("clientdelperm",
			new HashSet<>(Arrays.asList("cldbid", "permsid", "permid"))
	),
	/**
	 * Changes a clients settings using given properties.
	 *
	 * Example:
	 * <pre>{@code
	 * clientedit clid=10 client_description=Best\sguy\sever!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientedit clid={clientID} [client_properties...]
	 */
	CLIENT_EDIT("clientedit",
			new HashSet<>(Arrays.asList("clid", "client_description", "client_properties"))
	),
	/**
	 * Displays a list of clients matching a given name pattern.
	 *
	 * Example:
	 * <pre>{@code
	 * clientfind pattern=sven
	 * clid=7 client_nickname=Sven
	 * }</pre>
	 *
	 * Usage: clientfind pattern={clientName}
	 */
	CLIENT_FIND("clientfind",
			new HashSet<>(Arrays.asList("pattern"))
	),
	/**
	 * Displays the database ID matching the unique identifier specified by
	 * cluid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientgetdbidfromuid cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM=
	 * cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM= cldbid=32
	 * }</pre>
	 *
	 * Usage: clientgetdbidfromuid cluid={clientUID}
	 */
	CLIENT_GET_DBID_FROM_UID("clientgetdbidfromuid",
			new HashSet<>(Arrays.asList("cluid"))
	),
	/**
	 * Displays all client IDs matching the unique identifier specified by
	 * cluid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientgetids cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM=
	 * cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM= clid=1 name=Janko
	 * }</pre>
	 *
	 * Usage: clientgetids cluid={clientUID}
	 */
	CLIENT_GET_IDS("clientgetids",
			new HashSet<>(Arrays.asList("cluid"))
	),
	/**
	 * Displays the unique identifier and nickname matching the database ID
	 * specified by cldbid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientgetnamefromdbid cldbid=32
	 * cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM= cldbid=32 name=Janko
	 * }</pre>
	 *
	 * Usage: clientgetnamefromdbid cldbid={clientDBID}
	 */
	CLIENT_GET_NAME_FROM_DBID("clientgetnamefromdbid",
			new HashSet<>(Arrays.asList("cldbid"))
	),
	/**
	 * Displays the database ID and nickname matching the unique identifier
	 * specified by cluid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientgetnamefromuid cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM=
	 * cluid=dyjxkshZP6bz0n3bnwFQ1CkwZOM= cldbid=32 name=Janko
	 * }</pre>
	 *
	 * Usage: clientgetnamefromuid cluid={clientUID}
	 */
	CLIENT_GET_NAME_FROM_UID("clientgetnamefromuid",
			new HashSet<>(Arrays.asList("cluid"))
	),
	/**
	 * Displays the unique identifier matching the clientID specified by clid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientgetuidfromclid clid=8
	 * clid=8 cluid=yXM6PUfbCcPU+joxIFek1xOQwwQ= nickname=MuhChy1
	 * }</pre>
	 *
	 * Usage: clientgetuidfromclid clid={clientID}
	 */
	CLIENT_GET_UID_FROM_CLID("clientgetuidfromclid",
			new HashSet<>(Arrays.asList("clid"))
	),
	/**
	 * Displays detailed configuration information about a client including
	 * unique ID, nickname, client version, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * clientinfo clid=6
	 * client_unique_identifier=P5H2hrN6+gpQI4n\/dXp3p17vtY0= client_nickname=Rabe
	 * }</pre>
	 *
	 * Usage: clientinfo clid={clientID}
	 */
	CLIENT_INFO("clientinfo",
			new HashSet<>(Arrays.asList("clid"))
	),
	/**
	 * Kicks one or more clients specified with clid from their currently joined
	 * channel or from the server, depending on reasonid. The reasonmsg
	 * parameter specifies a text message sent to the kicked clients. This
	 * parameter is optional and may only have a maximum of 40 characters.
	 *
	 * Available reasonid values are:
	 *
	 * 4: Kick the client from his current channel into the default channel 5:
	 * Kick the client from the server
	 *
	 * Example:
	 * <pre>{@code
	 * clientkick reasonid=4 reasonmsg=Go\saway! clid=5|clid=6
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientkick reasonid={4|5} [reasonmsg={text}] clid={clientID}...
	 */
	CLIENT_KICK("clientkick",
			new HashSet<>(Arrays.asList("reasonid", "reasonmsg", "clid"))
	),
	/**
	 * Displays a list of clients online on a virtual server including their ID,
	 * nickname, status flags, etc. The output can be modified using several
	 * command options. Please note that the output will only contain clients
	 * which are currently in channels you're able to subscribe to.
	 *
	 * Example:
	 * <pre>{@code
	 * clientlist -away
	 * clid=5 cid=7 client_database_id=40 client_nickname=ScP client_type=0
	 * }</pre>
	 *
	 * Usage: clientlist [-uid] [-away] [-voice] [-times] [-groups] [-info]
	 * [-country] [-ip]
	 */
	CLIENT_LIST("clientlist",
			Collections.emptySet(),
			new HashSet<>(Arrays.asList("away", "uid", "voice", "times", "groups", "info", "country", "ip", "icon"))
	),
	/**
	 * Moves one or more clients specified with clid to the channel with ID cid.
	 * If the target channel has a password, it needs to be specified with cpw.
	 * If the channel has no password, the parameter can be omitted.
	 *
	 * Example:
	 * <pre>{@code
	 * clientmove cid=3 clid=5|clid=6
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientmove cid={channelID} [cpw={channelPassword}]
	 * clid={clientID}...
	 */
	CLIENT_MOVE("clientmove",
			new HashSet<>(Arrays.asList("cid", "clid", "cpw"))
	),
	/**
	 * Displays a list of permissions defined for a client.
	 *
	 * Example:
	 * <pre>{@code
	 * clientpermlist cldbid=2
	 * cldbid=2 permid=4353 permvalue=1 permnegated=0 permskip=0|permid=17276...
	 * }</pre>
	 *
	 * Usage: clientpermlist cldbid={clientDBID} [-permsid]
	 */
	CLIENT_PERM_LIST("clientpermlist",
			new HashSet<>(Arrays.asList("cldbid")),
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Sends a poke message to the client specified with clid.
	 *
	 * Example:
	 * <pre>{@code
	 * clientpoke msg=Wake\sup! clid=5
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientpoke msg={txt} clid={clientID}
	 */
	CLIENT_POKE("clientpoke",
			new HashSet<>(Arrays.asList("msg", "clid"))
	),
	/**
	 * Updates your own ServerQuery login credentials using a specified
	 * username. The password will be auto-generated.
	 *
	 * Example:
	 * <pre>{@code
	 * clientsetserverquerylogin client_login_name=admin
	 * client_login_password=+r\/TQqvR
	 * }</pre>
	 *
	 * Usage: clientsetserverquerylogin client_login_name={username}
	 */
	CLIENT_SET_SERVER_QUERY_LOG_IN("clientsetserverquerylogin",
			new HashSet<>(Arrays.asList("client_login_name"))
	),
	/**
	 * Change your ServerQuery clients settings using given properties.
	 *
	 * Example:
	 * <pre>{@code
	 * clientupdate client_nickname=ScP\s(query)
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: clientupdate [client_properties...]
	 */
	CLIENT_UPDATE("clientupdate",
			new HashSet<>(Arrays.asList("client_nickname", "client_properties"))
	),
	/**
	 * Submits a complaint about the client with database ID tcldbid to the
	 * server.
	 *
	 * Example:
	 * <pre>{@code
	 * complainadd tcldbid=3 message=Bad\sguy!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: complainadd tcldbid={targetClientDBID} message={text}
	 */
	COMPLAIN_ADD("complainadd",
			new HashSet<>(Arrays.asList("tcldbid", "message"))
	),
	/**
	 * Deletes the complaint about the client with database ID tcldbid submitted
	 * by the client with database ID fcldbid from the server.
	 *
	 * Example:
	 * <pre>{@code
	 * complaindel tcldbid=3 fcldbid=4
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: complaindel tcldbid={targetClientDBID} fcldbid={fromClientDBID}
	 */
	COMPLAIN_DEL("complaindel",
			new HashSet<>(Arrays.asList("tcldbid", "fcldbid"))
	),
	/**
	 * Deletes all complaints about the client with database ID tcldbid from the
	 * server.
	 *
	 * Example:
	 * <pre>{@code
	 * complaindelall tcldbid=3
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: complaindelall tcldbid={targetClientDBID}
	 */
	COMPLAIN_DELALL("complaindelall",
			new HashSet<>(Arrays.asList("tcldbid"))
	),
	/**
	 * Displays a list of complaints on the selected virtual server. If tcldbid
	 * is specified, only complaints about the targeted client will be shown.
	 *
	 * Example:
	 * <pre>{@code
	 * complainlist tcldbid=3
	 * tcldbid=3 tname=Julian fcldbid=56 fname=Sven message=Bad\sguy!...
	 * }</pre>
	 *
	 * Usage: complainlist [tcldbid={targetClientDBID}]
	 */
	COMPLAIN_LIST("complainlist",
			new HashSet<>(Arrays.asList("tcldbid"))
	),
	/**
	 * Displays a list of custom properties for the client specified with
	 * cldbid.
	 *
	 * Example:
	 * <pre>{@code
	 * custominfo cldbid=3
	 * cldbid=3 ident=forum_account value=ScP|ident=forum_id value=123
	 * }</pre>
	 *
	 * Usage: custominfo cldbid={clientDBID}
	 */
	CUSTOM_INFO("custominfo",
			new HashSet<>(Arrays.asList("cldbid"))
	),
	/**
	 * Searches for custom client properties specified by ident and value. The
	 * value parameter can include regular characters and SQL wildcard
	 * characters (e.g. %).
	 *
	 * Example:
	 * <pre>{@code
	 * customsearch ident=forum_account pattern=%ScP%
	 * cldbid=2 ident=forum_account value=ScP
	 * }</pre>
	 *
	 * Usage: customsearch ident={ident} pattern={pattern}
	 */
	CUSTOM_SEARCH("customsearch",
			new HashSet<>(Arrays.asList("ident", "pattern"))
	),
	/**
	 * Creates new directory in a channels file repository.
	 *
	 * Example:
	 * <pre>{@code
	 * ftcreatedir cid=2 cpw= dirname=\/My\sDirectory
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: ftcreatedir cid={channelID} cpw={channelPassword}
	 * dirname={dirPath}
	 */
	FTCREATEDIR("ftcreatedir",
			new HashSet<>(Arrays.asList("cid", "cpw", "dirname"))
	),
	/**
	 * Deletes one or more files stored in a channels file repository.
	 *
	 * Example:
	 * <pre>{@code
	 * ftdeletefile cid=2 cpw= name=\/Pic1.PNG|name=\/Pic2.PNG
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: ftdeletefile cid={channelID} cpw={channelPassword}
	 * name={filePath}...
	 */
	FTDELETE_FILE("ftdeletefile",
			new HashSet<>(Arrays.asList("cid", "cpw", "name"))
	),
	/**
	 * Displays detailed information about one or more specified files stored in
	 * a channels file repository.
	 *
	 * Example:
	 * <pre>{@code
	 * ftgetfileinfo cid=2 cpw= name=\/Pic1.PNG|cid=2 cpw= name=\/Pic2.PNG
	 * cid=2 path=\/ name=Stuff size=0 datetime=1259415210 type=0|name=Pic1.PNG
	 * }</pre>
	 *
	 * Usage: ftgetfileinfo cid={channelID} cpw={channelPassword}
	 * name={filePath}...
	 */
	FT_GET_FILE_INFO("ftgetfileinfo",
			new HashSet<>(Arrays.asList("cid", "cpw", "name"))
	),
	/**
	 * Displays a list of files and directories stored in the specified channels
	 * file repository.
	 *
	 * Example:
	 * <pre>{@code
	 * ftgetfilelist cid=2 cpw= path=\/
	 * cid=2 path=\/ name=Stuff size=0 datetime=1259415210 type=0|name=Pic1.PNG
	 * }</pre>
	 *
	 * Usage: ftgetfilelist cid={channelID} cpw={channelPassword}
	 * path={filePath}
	 */
	FT_GET_FILE_LIST("ftgetfilelist",
			new HashSet<>(Arrays.asList("cid", "cpw", "path"))
	),
	/**
	 * Initializes a file transfer download. clientftfid is an arbitrary ID to
	 * identify the file transfer on client-side. On success, the server
	 * generates a new ftkey which is required to start downloading the file
	 * through TeamSpeak 3's file transfer interface.
	 *
	 * Example:
	 * <pre>{@code
	 * ftinitdownload clientftfid=1 name=\/image.iso cid=5 cpw= seekpos=0
	 * clientftfid=1 serverftfid=7 ftkey=NrOga\/4d2GpYC5oKgxuclTO37X83ca\/1 port=...
	 * }</pre>
	 *
	 * Usage: ftinitdownload clientftfid={clientFileTransferID} name={filePath}
	 * cid={channelID} cpw={channelPassword} seekpos={seekPosition}
	 */
	FT_INIT_DOWNLOAD("ftinitdownload",
			new HashSet<>(Arrays.asList("clientftfid", "name", "cid", "cpw", "seekpos"))
	),
	/**
	 * Initializes a file transfer upload. clientftfid is an arbitrary ID to
	 * identify the file transfer on client-side. On success, the server
	 * generates a new ftkey which is required to start uploading the file
	 * through TeamSpeak 3's file transfer interface.
	 *
	 * Example:
	 * <pre>{@code
	 * ftinitupload clientftfid=1 name=\/image.iso cid=5 cpw= size=673460224
	 *  overwrite=1 resume=0
	 * clientftfid=1 serverftfid=6 ftkey=itRNdsIOvcBiBg\/Xj4Ge51ZSrsShHuid port=...
	 * }</pre>
	 *
	 * Usage: ftinitupload clientftfid={clientFileTransferID} name={filePath}
	 * cid={channelID} cpw={channelPassword} size={fileSize} overwrite={1|0}
	 * resume={1|0}
	 */
	FT_INIT_UPLOAD("ftinitupload",
			new HashSet<>(Arrays.asList("clientftfid", "name", "cid", "cpw", "size", "overwrite", "resume"))
	),
	/**
	 * Displays a list of running file transfers on the selected virtual server.
	 * The output contains the path to which a file is uploaded to, the current
	 * transfer rate in bytes per second, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * ftlist
	 * clid=2 path=files\/virtualserver_1\/channel_5 name=image.iso size=673460224
	 * }</pre>
	 *
	 * Usage: ftlist
	 */
	FT_LIST("ftlist"),
	/**
	 * Renames a file in a channels file repository. If the two parameters tcid
	 * and tcpw are specified, the file will be moved into another channels file
	 * repository.
	 *
	 * Example:
	 * <pre>{@code
	 * ftrenamefile cid=2 cpw= tcid=3 tcpw= oldname=\/Pic3.PNG newname=\/Pic3.PNG
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: ftrenamefile cid={channelID} cpw={channelPassword}
	 * [tcid={targetChannelID}] [tcpw={targetChannelPassword}]
	 * oldname={oldFilePath} newname={newFilePath}
	 */
	FT_RENAME_FILE("ftrenamefile",
			new HashSet<>(Arrays.asList("cid", "cpw", "tcid", "tcpw", "oldname", "newname"))
	),
	/**
	 * Stops the running file transfer with server-side ID serverftfid.
	 *
	 * Example:
	 * <pre>{@code
	 * ftstop serverftfid=2 delete=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: ftstop serverftfid={serverFileTransferID} delete={1|0}
	 */
	FTSTOP("ftstop",
			new HashSet<>(Arrays.asList("serverftfid", "delete"))
	),
	/**
	 * Sends a text message to all clients on all virtual servers in the
	 * TeamSpeak 3 Server instance.
	 *
	 * Example:
	 * <pre>{@code
	 * gm msg=Hello\sWorld!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: gm msg={text}
	 */
	GM("gm",
			new HashSet<>(Arrays.asList("msg"))
	),
	// Skipped C:\Users\fernando\Documents\teamspeak3-server_win64\serverquerydocs\help.txt
	/**
	 * Displays detailed configuration information about the server instance
	 * including uptime, number of virtual servers online, traffic information,
	 * etc.
	 *
	 * Example:
	 * <pre>{@code
	 * hostinfo
	 * virtualservers_running_total=3 virtualservers_total_maxclients=384 ...
	 * }</pre>
	 *
	 * Usage: hostinfo
	 */
	HOST_INFO("hostinfo"),
	/**
	 * Changes the server instance configuration using given properties.
	 *
	 * Example:
	 * <pre>{@code
	 * instanceedit serverinstance_filetransfer_port=1337
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: instanceedit [instance_properties...]
	 */
	INSTANCEEDIT("instanceedit",
			new HashSet<>(Arrays.asList("serverinstance_filetransfer_port", "instance_properties"))
	),
	/**
	 * Displays the server instance configuration including database revision
	 * number, the file transfer port, default group IDs, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * instanceinfo
	 * serverinstance_database_version=12 serverinstance_filetransfer_port=30033
	 * }</pre>
	 *
	 * Usage: instanceinfo
	 */
	INSTANCE_INFO("instanceinfo"),
	/**
	 * Writes a custom entry into the servers log. Depending on your
	 * permissions, you'll be able to add entries into the server instance log
	 * and/or your virtual servers log. The loglevel parameter specifies the
	 * type of the entry.
	 *
	 * Example:
	 * <pre>{@code
	 * logadd loglevel=4 logmsg=Informational\smessage!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: logadd loglevel={1-4} logmsg={text}
	 */
	LOG_ADD("logadd",
			new HashSet<>(Arrays.asList("loglevel", "logmsg"))
	),
	/**
	 * Authenticates with the TeamSpeak 3 Server instance using given
	 * ServerQuery login credentials.
	 *
	 * Example:
	 * <pre>{@code
	 * login client_login_name=xyz client_login_password=xyz
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: login client_login_name={username}
	 * client_login_password={password} login {username} {password}
	 */
	LOG_IN("login",
			new HashSet<>(Arrays.asList("client_login_name", "client_login_password"))
	),
	/**
	 * Deselects the active virtual server and logs out from the server
	 * instance.
	 *
	 * Example:
	 * <pre>{@code
	 * logout
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: logout
	 */
	LOG_OUT("logout"),
	/**
	 * Displays a specified number of entries from the servers logfile. If
	 * instance is set to 1, the server will return lines from the master
	 * logfile (ts3server_0) instead of the selected virtual server logfile.
	 *
	 * Example:
	 * <pre>{@code
	 * logview
	 * last_pos=403788 file_size=411980 l=\p\slistening\son\s0.0.0.0:9987 ...
	 * }</pre>
	 *
	 * Usage: logview [lines={1-100}] [reverse={1|0}] [instance={1|0}]
	 * [begin_pos={n}]
	 */
	LOG_VIEW("logview",
			new HashSet<>(Arrays.asList("lines", "reverse", "instance", "begin_pos"))
	),
	/**
	 * Sends an offline message to the client specified by cluid.
	 *
	 * Example:
	 * <pre>{@code
	 * messageadd cluid=oHhi9WzXLNEFQOwAu4JYKGU+C+c= subject=Hi! message=Hello?!?
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: messageadd cluid={clientUID} subject={subject} message={text}
	 */
	MESSAGE_ADD("messageadd",
			new HashSet<>(Arrays.asList("cluid", "subject", "message"))
	),
	/**
	 * Deletes an existing offline message with ID msgid from your inbox.
	 *
	 * Example:
	 * <pre>{@code
	 * messagedel msgid=4
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: messagedel msgid={messageID}
	 */
	MESSAGE_DEL("messagedel",
			new HashSet<>(Arrays.asList("msgid"))
	),
	/**
	 * Displays an existing offline message with ID msgid from your inbox.
	 * Please note that this does not automatically set the flag_read property
	 * of the message.
	 *
	 * Example:
	 * <pre>{@code
	 * messageget msgid=4
	 * msgid=4 cluid=xwEzb5ENOaglVHu9oelK++reUyE= subject=Hi! message=Hello?!?
	 * }</pre>
	 *
	 * Usage: messageget msgid={messageID}
	 */
	MESSAGE_GET("messageget",
			new HashSet<>(Arrays.asList("msgid"))
	),
	/**
	 * Displays a list of offline messages you've received. The output contains
	 * the senders unique identifier, the messages subject, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * messagelist
	 * msgid=4 cluid=xwEzb5ENOaglVHu9oelK++reUyE= subject=Test flag_read=0...
	 * }</pre>
	 *
	 * Usage: messagelist
	 */
	MESSAGE_LIST("messagelist"),
	/**
	 * Updates the flag_read property of the offline message specified with
	 * msgid. If flag is set to 1, the message will be marked as read.
	 *
	 * Example:
	 * <pre>{@code
	 * messageupdateflag msgid=4 flag=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: messageupdateflag msgid={messageID} flag={1|0}
	 */
	MESSAGE_UPDATE_FLAG("messageupdateflag",
			new HashSet<>(Arrays.asList("msgid", "flag"))
	),
	/**
	 * Displays detailed information about all assignments of the permission
	 * specified with permid. The output is similar to permoverview which
	 * includes the type and the ID of the client, channel or group associated
	 * with the permission.
	 *
	 * Example:
	 * <pre>{@code
	 * permfind permid=4353
	 * t=0 id1=1 id2=0 p=4353|t=0 id1=2 id2=0 p=4353
	 * }</pre>
	 *
	 * Usage: permfind permid={permID}
	 */
	PERM_FIND("permfind",
			new HashSet<>(Arrays.asList("permid"))
	),
	/**
	 * Displays the current value of the permission specified with permid or
	 * permsid for your own connection. This can be useful when you need to
	 * check your own privileges.
	 *
	 * Example:
	 * <pre>{@code
	 * permget permid=21174
	 * permsid=i_client_move_power permid=21174 permvalue=100
	 * }</pre>
	 *
	 * Usage: permget permid={permID} permget permsid={permName}
	 */
	PERM_GET("permget",
			new HashSet<>(Arrays.asList("permid", "permsid"))
	),
	/**
	 * Displays the database ID of one or more permissions specified by permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * permidgetbyname permsid=b_serverinstance_help_view
	 * permsid=b_serverinstance_help_view permid=4353
	 * }</pre>
	 *
	 * Usage: permidgetbyname permsid={permName}|permsid={permName}|...
	 */
	PERM_ID_GET_BY_NAME("permidgetbyname",
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Displays a list of permissions available on the server instance including
	 * ID, name and description.
	 *
	 * Example:
	 * <pre>{@code
	 * permissionlist
	 * permid=21413 permname=b_client_channel_textmessage_send permdesc=Send\ste...
	 * }</pre>
	 *
	 * Usage: permissionlist
	 */
	PERMISSION_LIST("permissionlist"),
	/**
	 * Displays all permissions assigned to a client for the channel specified
	 * with cid. If permid is set to 0, all permissions will be displayed. The
	 * output follows the following format:
	 *
	 * t={permType} id1={id1} id2={id2} p={permID} v={permValue} n={permNegated}
	 * s={permSkip}|t={permType} id1={id1} id2={id2} p={permID} v={permValue}
	 * n={permNegated} s={permSkip}|...
	 *
	 * The possible values for t, id1 and id2 are:
	 *
	 * <ul>
	 * <li> 0: Server Group; =&gt; id1={serverGroupID}, id2=0</li>
	 * <li> 1: Global Client;=&gt; id1={clientDBID}, id2=0</li>
	 * <li> 2: Channel;=&gt; id1={channelID}, id2=0</li>
	 * <li> 3: Channel Group;=&gt; id1={channelID}, id2={channelGroupID}</li>
	 * <li> 4: Channel Client; =&gt; id1={channelID}, id2={clientDBID}</li>
	 * </ul>
	 *
	 * Example:
	 * <pre>{@code
	 * permoverview cldbid=57 cid=74 permid=0
	 * t=0 id1=5 id2=0 p=37 v=1 n=0 s=0|t=0 id1=5 id2=0 p=38 v=1 n=0 s=0|...
	 * }</pre>
	 *
	 * Usage: permoverview cid={channelID} cldbid={clientDBID} permid={permID}
	 */
	PERM_OVERVIEW("permoverview",
			new HashSet<>(Arrays.asList("cldbid", "cid", "permid"))
	),
	/**
	 * Restores the default permission settings on the selected virtual server
	 * and creates a new initial administrator token. Please note that in case
	 * of an error during the permreset call - e.g. when the database has been
	 * modified or corrupted - the virtual server will be deleted from the
	 * database.
	 *
	 * Example:
	 * <pre>{@code
	 * permreset
	 * token=MqQbPLLm6jLC+x8j31jUL7GkME1UY0GaDYK+XG5e
	 * }</pre>
	 *
	 * Usage: permreset
	 */
	PERM_RESET("permreset"),
	/**
	 * Create a new token. If tokentype is set to 0, the ID specified with
	 * tokenid1 will be a server group ID. Otherwise, tokenid1 is used as a
	 * channel group ID and you need to provide a valid channel ID using
	 * tokenid2.
	 *
	 * The tokencustomset parameter allows you to specify a set of custom client
	 * properties. This feature can be used when generating tokens to combine a
	 * website account database with a TeamSpeak user. The syntax of the value
	 * needs to be escaped using the ServerQuery escape patterns and has to
	 * follow the general syntax of:
	 *
	 * ident=ident1 value=value1|ident=ident2 value=value2|ident=ident3
	 * value=value3
	 *
	 * Example:
	 * <pre>{@code
	 * privilegekeyadd tokentype=0 tokenid1=6 tokenid2=0 tokendescription=Test
	 *  tokencustomset=ident=forum_user\svalue=dante\pident=forum_id\svalue=123
	 * token=1ayoQOxG8r5Re78zgChvLYBWWaFWCoty0Uh+pUFk
	 * }</pre>
	 *
	 * Usage: privilegekeyadd tokentype={1|0} tokenid1={groupID}
	 * tokenid2={channelID} [tokendescription={description}]
	 * [tokencustomset={customFieldSet}]
	 */
	PRIVILEGEKEY_ADD("privilegekeyadd",
			new HashSet<>(Arrays.asList("tokentype", "tokenid1", "tokenid2", "tokendescription", "tokencustomset"))
	),
	/**
	 * Deletes an existing token matching the token key specified with token.
	 *
	 * Example:
	 * <pre>{@code
	 * privilegekeydelete token=eKnFZQ9EK7G7MhtuQB6+N2B1PNZZ6OZL3ycDp2OW
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: privilegekeydelete token={tokenKey}
	 */
	PRIVILEGEKEY_DELETE("privilegekeydelete",
			new HashSet<>(Arrays.asList("token"))
	),
	/**
	 * Displays a list of tokens available including their type and group IDs.
	 * Tokens can be used to gain access to specified server or channel groups.
	 *
	 * A token is similar to a client with administrator privileges that adds
	 * you to a certain permission group, but without the necessity of a such a
	 * client with administrator privileges to actually exist. It is a long
	 * (random looking) string that can be used as a ticket into a specific
	 * server group.
	 *
	 * Example:
	 * <pre>{@code
	 * privilegekeylist
	 * token=88CVUg\/zkujt+y+WfHdko79UcM4R6uyCL6nEfy3B token_type=0 token_id1=9...
	 * }</pre>
	 *
	 * Usage: privilegekeylist
	 */
	PRIVILEGEKEY_LIST("privilegekeylist"),
	/**
	 * Use a token key gain access to a server or channel group. Please note
	 * that the server will automatically delete the token after it has been
	 * used.
	 *
	 * Example:
	 * <pre>{@code
	 * privilegekeyuse token=eKnFZQ9EK7G7MhtuQB6+N2B1PNZZ6OZL3ycDp2OW
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: privilegekeyuse token={tokenKey}
	 */
	PRIVILEGEKEY_USE("privilegekeyuse",
			new HashSet<>(Arrays.asList("token"))
	),
	/**
	 * Closes the ServerQuery connection to the TeamSpeak 3 Server instance.
	 *
	 * Example:
	 * <pre>{@code
	 * quit
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: quit
	 */
	QUIT("quit"),
	/**
	 * Sends a text message a specified target. The type of the target is
	 * determined by targetmode while target specifies the ID of the recipient,
	 * whether it be a virtual server, a channel or a client.
	 *
	 * Example:
	 * <pre>{@code
	 * sendtextmessage targetmode=2 target=1 msg=Hello\sWorld!
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: sendtextmessage targetmode={1-3}
	 * target={serverID|channelID|clientID} msg={text}
	 */
	SEND_TEXT_MESSAGE("sendtextmessage",
			new HashSet<>(Arrays.asList("targetmode", "target", "msg"))
	),
	/**
	 * Creates a new virtual server using the given properties and displays its
	 * ID and initial administrator token. If virtualserver_port is not
	 * specified, the server will test for the first unused UDP port.
	 *
	 * Example:
	 * <pre>{@code
	 * servercreate virtualserver_name=TeamSpeak\s]\p[\sServer
	 *  virtualserver_port=9988 virtualserver_maxclients=32
	 * sid=7 token=HhPbcMAMdAHGUip1yOma2Tl3sN0DN7B3Y0JVzYv6 virtualserver_port=9988
	 * }</pre>
	 *
	 * Usage: servercreate [virtualserver_properties...]
	 */
	SERVER_CREATE("servercreate",
			new HashSet<>(Arrays.asList("virtualserver_name", "virtualserver_port", "virtualserver_maxclients", "virtualserver_properties"))
	),
	/**
	 * Deletes the virtual server specified with sid. Please note that only
	 * virtual servers in stopped state can be deleted.
	 *
	 * Example:
	 * <pre>{@code
	 * serverdelete sid=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serverdelete sid={serverID}
	 */
	SERVER_DELETE("serverdelete",
			new HashSet<>(Arrays.asList("sid"))
	),
	/**
	 * Changes the selected virtual servers configuration using given
	 * properties.
	 *
	 * Example:
	 * <pre>{@code
	 * serveredit virtualserver_name=TeamSpeak\sServer virtualserver_maxclients=32
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serveredit [virtualserver_properties...]
	 */
	SERVER_EDIT("serveredit",
			new HashSet<>(Arrays.asList("virtualserver_name", "virtualserver_maxclients", "virtualserver_properties"))
	),
	/**
	 * Creates a new server group using the name specified with name and
	 * displays its ID. The optional type parameter can be used to create
	 * ServerQuery groups and template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupadd name=Server\sAdmin
	 * sgid=13
	 * }</pre>
	 *
	 * Usage: servergroupadd name={groupName} [type={groupDbType}]
	 */
	SERVER_GROUP_ADD("servergroupadd",
			new HashSet<>(Arrays.asList("name", "type"))
	),
	/**
	 * Adds a client to the server group specified with sgid. Please note that a
	 * client cannot be added to default groups or template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupaddclient sgid=16 cldbid=3
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupaddclient sgid={groupID} cldbid={clientDBID}
	 */
	SERVER_GROUP_ADD_CLIENT("servergroupaddclient",
			new HashSet<>(Arrays.asList("sgid", "cldbid"))
	),
	/**
	 * Adds a set of specified permissions to the server group specified with
	 * sgid. Multiple permissions can be added by providing the four parameters
	 * of each permission. A permission can be specified by permid or permsid.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupaddperm sgid=13 permid=8470 permvalue=1 permnegated=0
	 *  permskip=0|permid=8475 permvalue=0 permnegated=1 permskip=0
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupaddperm sgid={groupID} permid={permID}
	 * permvalue={permValue} permnegated={1|0} permskip={1|0}|...
	 * servergroupaddperm sgid={groupID} permsid={permName}
	 * permvalue={permValue} permnegated={1|0} permskip={1|0}|...
	 */
	SERVER_GROUP_ADD_PERM("servergroupaddperm",
			new HashSet<>(Arrays.asList("sgid", "permid", "permvalue", "permnegated", "permskip", "permsid"))
	),
	/**
	 * Adds a set of specified permissions to ALL regular server groups on all
	 * virtual servers. The target groups will be identified by the value of
	 * their i_group_auto_update_type permission specified with sgtype. Multiple
	 * permissions can be added at once. A permission can be specified by permid
	 * or permsid.
	 *
	 * The known values for sgtype are:
	 *
	 * 10: Channel Guest 15: Server Guest 20: Query Guest 25: Channel Voice 30:
	 * Server Normal 35: Channel Operator 40: Channel Admin 45: Server Admin 50:
	 * Query Admin
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupautoaddperm sgtype=45 permid=8470 permvalue=1 permnegated=0
	 *  permskip=0|permid=8475 permvalue=0 permnegated=1 permskip=0
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupautoaddperm sgtype={type} permid={permID}
	 * permvalue={permValue} permnegated={1|0} permskip={1|0}|...
	 * servergroupautoaddperm sgtype={type} permsid={permName}
	 * permvalue={permValue} permnegated={1|0} permskip={1|0}|...
	 */
	SERVER_GROUP_AUTO_ADD_PERM("servergroupautoaddperm",
			new HashSet<>(Arrays.asList("sgtype", "permid", "permvalue", "permnegated", "permskip", "permsid"))
	),
	/**
	 * Removes a set of specified permissions from ALL regular server groups on
	 * all virtual servers. The target groups will be identified by the value of
	 * their i_group_auto_update_type permission specified with sgtype. Multiple
	 * permissions can be removed at once. A permission can be specified by
	 * permid or permsid.
	 *
	 * The known values for sgtype are:
	 *
	 * 10: Channel Guest 15: Server Guest 20: Query Guest 25: Channel Voice 30:
	 * Server Normal 35: Channel Operator 40: Channel Admin 45: Server Admin 50:
	 * Query Admin
	 *
	 * Examples:
	 * <pre>{@code
	 * servergroupautodelperm sgtype=45 permid=8470|permid=8475
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupautodelperm sgtype={type}
	 * permid={permID}|permid={permID}|... servergroupautodelperm sgtype={type}
	 * permsid={permName}|...
	 */
	SERVER_GROUP_AUTO_DEL_PERM("servergroupautodelperm",
			new HashSet<>(Arrays.asList("sgtype", "permid", "permsid"))
	),
	/**
	 * Displays all server groups the client specified with cldbid is currently
	 * residing in.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupsbyclientid cldbid=18
	 * name=Server\sAdmin sgid=6 cldbid=18
	 * }</pre>
	 *
	 * Usage: servergroupsbyclientid sgid={clientDBID}
	 */
	SERVER_GROUPS_BY_CLIENT_ID("servergroupsbyclientid",
			new HashSet<>(Arrays.asList("cldbid", "sgid"))
	),
	/**
	 * Displays the IDs of all clients currently residing in the server group
	 * specified with sgid. If you're using the -names option, the output will
	 * also contain the last known nickname and the unique identifier of the
	 * clients.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupclientlist sgid=16
	 * cldbid=7|cldbid=8|cldbid=9|cldbid=11|cldbid=13|cldbid=16|cldbid=18|...
	 * }</pre>
	 *
	 * Usage: servergroupclientlist sgid={groupID} [-names]
	 */
	SERVER_GROUP_CLIENT_LIST("servergroupclientlist",
			new HashSet<>(Arrays.asList("sgid")),
			new HashSet<>(Arrays.asList("names"))
	),
	/**
	 * Creates a copy of the server group specified with ssgid. If tsgid is set
	 * to 0, the server will create a new group. To overwrite an existing group,
	 * simply set tsgid to the ID of a designated target group. If a target
	 * group is set, the name parameter will be ignored.
	 *
	 * The type parameter can be used to create ServerQuery and template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupcopy ssgid=6 tsgid=0 name=My\sGroup\s(Copy) type=1
	 * sgid=21
	 * }</pre>
	 *
	 * Usage: servergroupcopy ssgid={sourceGroupID} tsgid={targetGroupID}
	 * name={groupName} type={groupDbType}
	 */
	SERVER_GROUP_COPY("servergroupcopy",
			new HashSet<>(Arrays.asList("ssgid", "tsgid", "name", "type"))
	),
	/**
	 * Deletes the server group specified with sgid. If force is set to 1, the
	 * server group will be deleted even if there are clients within.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupdel sgid=13
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupdel sgid={groupID} force={1|0}
	 */
	SERVER_GROUP_DEL("servergroupdel",
			new HashSet<>(Arrays.asList("sgid", "force"))
	),
	/**
	 * Removes a client from the server group specified with sgid.
	 *
	 * Example:
	 * <pre>{@code
	 * servergroupdelclient sgid=16 cldbid=3
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupdelclient sgid={groupID} cldbid={clientDBID}
	 */
	SERVER_GROUP_DEL_CLIENT("servergroupdelclient",
			new HashSet<>(Arrays.asList("sgid", "cldbid"))
	),
	/**
	 * Removes a set of specified permissions from the server group specified
	 * with sgid. Multiple permissions can be removed at once. A permission can
	 * be specified by permid or permsid.
	 *
	 * Examples:
	 * <pre>{@code
	 * servergroupdelperm sgid=16 permid=8470|permid=8475
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergroupdelperm sgid={groupID} permid={permID}|permid={permID}
	 * servergroupdelperm sgid={groupID} permsid={permName}
	 */
	SERVER_GROUP_DEL_PERM("servergroupdelperm",
			new HashSet<>(Arrays.asList("sgid", "permid", "permsid"))
	),
	/**
	 * Displays a list of server groups available. Depending on your
	 * permissions, the output may also contain global ServerQuery groups and
	 * template groups.
	 *
	 * Example:
	 * <pre>{@code
	 * servergrouplist
	 * sgid=9 name=Server\sAdmin type=1 iconid=300 savedb=1|sgid=10 name=Normal t...
	 * }</pre>
	 *
	 * Usage: servergrouplist
	 */
	SERVER_GROUP_LIST("servergrouplist"),
	/**
	 * Displays a list of permissions assigned to the server group specified
	 * with sgid. The optional -permsid parameter can be used to get the
	 * permission names instead of their internal ID.
	 *
	 * Example:
	 * <pre>{@code
	 * servergrouppermlist sgid=13
	 * permid=8470 permvalue=1 permnegated=0 permskip=0|permid=8475 permvalue=1|...
	 * }</pre>
	 *
	 * Usage: servergrouppermlist sgid={groupID} [-permsid]
	 */
	SERVER_GROUP_PERM_LIST("servergrouppermlist",
			new HashSet<>(Arrays.asList("sgid")),
			new HashSet<>(Arrays.asList("permsid"))
	),
	/**
	 * Changes the name of the server group specified with sgid.
	 *
	 * Example:
	 * <pre>{@code
	 * servergrouprename sgid=13 name=New\sName
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servergrouprename sgid={groupID} name={groupName}
	 */
	SERVER_GROUP_RENAME("servergrouprename",
			new HashSet<>(Arrays.asList("sgid", "name"))
	),
	/**
	 * Displays the database ID of the virtual server running on the UDP port
	 * specified by virtualserver_port.
	 *
	 * Example:
	 * <pre>{@code
	 * serveridgetbyport virtualserver_port=9987
	 * server_id=1
	 * }</pre>
	 *
	 * Usage: serveridgetbyport virtualserver_port={serverPort}
	 */
	SERVER_ID_GET_BY_PORT("serveridgetbyport",
			new HashSet<>(Arrays.asList("virtualserver_port"))
	),
	/**
	 * Displays detailed configuration information about the selected virtual
	 * server including unique ID, number of clients online, configuration, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * serverinfo
	 * virtualserver_port=9987 virtualserver_name=TeamSpeak\s]I[\sServer virtua...
	 * }</pre>
	 *
	 * Usage: serverinfo
	 */
	SERVER_INFO("serverinfo"),
	/**
	 * Displays a list of virtual servers including their ID, status, number of
	 * clients online, etc. If you're using the -all option, the server will
	 * list all virtual servers stored in the database. This can be useful when
	 * multiple server instances with different machine IDs are using the same
	 * database. The machine ID is used to identify the server instance a
	 * virtual server is associated with.
	 *
	 * The status of a virtual server can be either online, offline, booting up,
	 * shutting down and virtual online. While most of them are
	 * self-explanatory, virtual online is a bit more complicated. Whenever you
	 * select a virtual server which is currently stopped, it will be started in
	 * virtual mode which means you are able to change its configuration, create
	 * channels or change permissions, but no regular TeamSpeak 3 Client can
	 * connect. As soon as the last ServerQuery client deselects the virtual
	 * server, its status will be changed back to offline.
	 *
	 * Example:
	 * <pre>{@code
	 * serverlist
	 * virtualserver_id=1 virtualserver_port=9987 virtualserver_status=online
	 * }</pre>
	 *
	 * Usage: serverlist [-uid] [-all] [-short] [-onlyoffline]
	 */
	SERVER_LIST("serverlist",
			Collections.emptySet(),
			new HashSet<>(Arrays.asList("uid", "all", "short", "onlyoffline"))
	),
	/**
	 * Registers for a specified category of events on a virtual server to
	 * receive notification messages. Depending on the notifications you've
	 * registered for, the server will send you a message on every event in the
	 * view of your ServerQuery client (e.g. clients joining your channel,
	 * incoming text messages, server configuration changes, etc). The event
	 * source is declared by the event parameter while id can be used to limit
	 * the notifications to a specific channel.
	 *
	 * Example:
	 * <pre>{@code
	 * servernotifyregister event=server
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servernotifyregister [id={channelID}]
	 * event={server|channel|textserver|textchannel|textprivate}
	 */
	SERVER_NOTIFY_REGISTER("servernotifyregister",
			new HashSet<>(Arrays.asList("event", "id"))
	),
	/**
	 * Unregisters all events previously registered with servernotifyregister so
	 * you will no longer receive notification messages.
	 *
	 * Example:
	 * <pre>{@code
	 * servernotifyunregister
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servernotifyunregister
	 */
	SERVER_NOTIFY_UNREGISTER("servernotifyunregister",
			new HashSet<>(Arrays.asList("event", "id"))
	),
	/**
	 * Stops the entire TeamSpeak 3 Server instance by shutting down the
	 * process.
	 *
	 * Example:
	 * <pre>{@code
	 * serverprocessstop
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serverprocessstop
	 */
	SERVER_PROCESS_STOP("serverprocessstop"),
	/**
	 * Displays detailed connection information about the selected virtual
	 * server including uptime, traffic information, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * serverrequestconnectioninfo
	 * connection_filetransfer_bandwidth_sent=0 connection_packets_sent_total=0...
	 * }</pre>
	 *
	 * Usage: serverrequestconnectioninfo
	 */
	SERVER_REQUESTCONNECTION_INFO("serverrequestconnectioninfo"),
	/**
	 * Displays a snapshot of the selected virtual server containing all
	 * settings, groups and known client identities. The data from a server
	 * snapshot can be used to restore a virtual servers configuration.
	 *
	 * Example:
	 * <pre>{@code
	 * serversnapshotcreate
	 * hash=bnTd2E1kNITHjJYRCFjgbKKO5P8=|virtualserver_name=TeamSpeak\sServer...
	 * }</pre>
	 *
	 * Usage: serversnapshotcreate
	 */
	SERVER_SNAPSHOT_CREATE("serversnapshotcreate"),
	/**
	 * Restores the selected virtual servers configuration using the data from a
	 * previously created server snapshot. Please note that the TeamSpeak 3
	 * Server does NOT check for necessary permissions while deploying a
	 * snapshot so the command could be abused to gain additional privileges.
	 *
	 * Example:
	 * <pre>{@code
	 * serversnapshotdeploy hash=bnTd2E1kNITHjJYRCFjgbKKO5P8=|virtualserver_...
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serversnapshotdeploy {virtualserver_snapshot}
	 */
	SERVER_SNAPSHOT_DEPLOY("serversnapshotdeploy",
			new HashSet<>(Arrays.asList("hash"))
	),
	/**
	 * Starts the virtual server specified with sid. Depending on your
	 * permissions, you're able to start either your own virtual server only or
	 * any virtual server in the server instance.
	 *
	 * Example:
	 * <pre>{@code
	 * serverstart sid=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serverstart sid={serverID}
	 */
	SERVER_START("serverstart",
			new HashSet<>(Arrays.asList("sid"))
	),
	/**
	 * Stops the virtual server specified with sid. Depending on your
	 * permissions, you're able to stop either your own virtual server only or
	 * all virtual servers in the server instance.
	 *
	 * Example:
	 * <pre>{@code
	 * serverstop sid=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: serverstop sid={serverID}
	 */
	SERVER_STOP("serverstop",
			new HashSet<>(Arrays.asList("sid"))
	),
	/**
	 * Sets a new temporary server password specified with pw. The temporary
	 * password will be valid for the number of seconds specified with duration.
	 * The client connecting with this password will automatically join the
	 * channel specified with tcid. If tcid is set to 0, the client will join
	 * the default channel.
	 *
	 * Example:
	 * <pre>{@code
	 * servertemppasswordadd pw=secret desc=none duration=3600 tcid=117535 tcpw=123
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servertemppasswordadd pw={password} desc={description}
	 * duration={seconds} tcid={channelID} tcpw={channelPW}
	 */
	SERVER_TEMP_PASSWORD_ADD("servertemppasswordadd",
			new HashSet<>(Arrays.asList("pw", "desc", "duration", "tcid", "tcpw"))
	),
	/**
	 * Deletes the temporary server password specified with pw.
	 *
	 * Example:
	 * <pre>{@code
	 * servertemppassworddel pw=secret
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: servertemppassworddel pw={password}
	 */
	SERVER_TEMP_PASSWORD_DEL("servertemppassworddel",
			new HashSet<>(Arrays.asList("pw"))
	),
	/**
	 * Returns a list of active temporary server passwords. The output contains
	 * the clear-text password, the nickname and unique identifier of the
	 * creating client.
	 *
	 * Example:
	 * <pre>{@code
	 * servertemppasswordlist
	 * nickname=serveradmin uid=serveradmin desc=none pw_clear=secret
	 * }</pre>
	 *
	 * Usage: servertemppasswordlist
	 */
	SERVER_TEMP_PASSWORD_LIST("servertemppasswordlist"),
	/**
	 * Sets the channel group of a client to the ID specified with cgid.
	 *
	 * Example:
	 * <pre>{@code
	 * setclientchannelgroup cgid=13 cid=15 cldbid=20
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: setclientchannelgroup cgid={groupID} cid={channelID}
	 * cldbid={clientDBID}
	 */
	SET_CLIENT_CHANNEL_GROUP("setclientchannelgroup",
			new HashSet<>(Arrays.asList("cgid", "cid", "cldbid"))
	),
	/**
	 * Create a new token. If tokentype is set to 0, the ID specified with
	 * tokenid1 will be a server group ID. Otherwise, tokenid1 is used as a
	 * channel group ID and you need to provide a valid channel ID using
	 * tokenid2.
	 *
	 * The tokencustomset parameter allows you to specify a set of custom client
	 * properties. This feature can be used when generating tokens to combine a
	 * website account database with a TeamSpeak user. The syntax of the value
	 * needs to be escaped using the ServerQuery escape patterns and has to
	 * follow the general syntax of:
	 *
	 * ident=ident1 value=value1|ident=ident2 value=value2|ident=ident3
	 * value=value3
	 *
	 * Example:
	 * <pre>{@code
	 * tokenadd tokentype=0 tokenid1=6 tokenid2=0 tokendescription=Test
	 * tokencustomset=ident=forum_user\svalue=ScP\pident=forum_id\svalue=123
	 * }</pre>
	 *
	 * Usage: tokenadd tokentype={1|0} tokenid1={groupID} tokenid2={channelID}
	 * [tokendescription={description}] [tokencustomset={customFieldSet}]
	 */
	TOKEN_ADD("tokenadd",
			new HashSet<>(Arrays.asList("tokentype", "tokenid1", "tokenid2", "tokendescription", "tokencustomset"))
	),
	/**
	 * Deletes an existing token matching the token key specified with token.
	 *
	 * Example:
	 * <pre>{@code
	 * tokendelete token=eKnFZQ9EK7G7MhtuQB6+N2B1PNZZ6OZL3ycDp2OW
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: tokendelete token={tokenKey}
	 */
	TOKEN_DELETE("tokendelete",
			new HashSet<>(Arrays.asList("token"))
	),
	/**
	 * Displays a list of tokens available including their type and group IDs.
	 * Tokens can be used to gain access to specified server or channel groups.
	 *
	 * A token is similar to a client with administrator privileges that adds
	 * you to a certain permission group, but without the necessity of a such a
	 * client with administrator privileges to actually exist. It is a long
	 * (random looking) string that can be used as a ticket into a specific
	 * server group.
	 *
	 * Example:
	 * <pre>{@code
	 * tokenlist
	 * token=88CVUg\/zkujt+y+WfHdko79UcM4R6uyCL6nEfy3B token_type=0 token_id1=9...
	 * }</pre>
	 *
	 * Usage: tokenlist
	 */
	TOKEN_LIST("tokenlist"),
	/**
	 * Use a token key gain access to a server or channel group. Please note
	 * that the server will automatically delete the token after it has been
	 * used.
	 *
	 * Example:
	 * <pre>{@code
	 * tokenuse token=eKnFZQ9EK7G7MhtuQB6+N2B1PNZZ6OZL3ycDp2OW
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: tokenuse token={tokenKey}
	 */
	TOKEN_USE("tokenuse",
			new HashSet<>(Arrays.asList("token"))
	),
	/**
	 * Selects the virtual server specified with sid or port to allow further
	 * interaction. The ServerQuery client will appear on the virtual server and
	 * acts like a real TeamSpeak 3 Client, except it's unable to send or
	 * receive voice data.
	 *
	 * If your database contains multiple virtual servers using the same UDP
	 * port, use will select a random virtual server using the specified port.
	 *
	 * Examples:
	 * <pre>{@code
	 * use sid=1
	 * error id=0 msg=ok
	 * }</pre>
	 *
	 * Usage: use [sid={serverID}] [port={serverPort}] [-virtual] use {serverID}
	 */
	USE("use",
			new HashSet<>(Arrays.asList("sid", "port")),
			new HashSet<>(Arrays.asList("virtual"))
	),
	/**
	 * Displays the servers version information including platform and build
	 * number.
	 *
	 * Example:
	 * <pre>{@code
	 * version
	 * version=3.0.0-beta16 build=9929 platform=Linux
	 * }</pre>
	 *
	 * Usage: version
	 */
	VERSION("version"),
	/**
	 * Displays information about your current ServerQuery connection including
	 * the ID of the selected virtual server, your loginname, etc.
	 *
	 * Example:
	 * <pre>{@code
	 * whoami
	 * virtualserver_status=online virtualserver_id=1 client_channel_id=2 ...
	 * }</pre>
	 *
	 * Usage: whoami
	 */
	WHOAMI("whoami");

	private static final Map<String, Command> BY_NAME;

	private final Set<String> options;
	private final Set<String> flags;
	private final String cmd;

	private Command(String cmd) {
		this(cmd, Collections.emptySet(), Collections.emptySet());
	}

	private Command(String cmd, Set<String> options) {
		this(cmd, options, Collections.emptySet());
	}

	private Command(String cmd, Set<String> options, Set<String> flags) {
		if (options == Collections.EMPTY_SET) {
			this.options = options;
		} else {
			this.options = Collections.unmodifiableSet(options);
		}
		if (flags == Collections.EMPTY_SET) {
			this.flags = flags;
		} else {
			this.flags = Collections.unmodifiableSet(flags);
		}
		this.cmd = cmd;
	}

	public String getCmd() {
		return cmd;
	}

	public Set<String> getOptions() {
		return options;
	}

	public Set<String> getFlags() {
		return flags;
	}

	public boolean isValidOption(String key) {
		return options.contains(key);
	}

	public boolean isValidFlag(String key) {
		return flags.contains(key);
	}

	public ComplexRequestBuilder buildUsing() {
		return new ComplexRequestBuilder(this);
	}

	public ComplexRequest build() {
		return buildUsing().build();
	}

	public ComplexRequestBuilder addData(String key, Object value) {
		return buildUsing().addData(key, value);
	}

	public ComplexRequestBuilder addData(String key, String value) {
		return buildUsing().addData(key, value);
	}

	public ComplexRequestBuilder addOption(String key) {
		return buildUsing().addOption(key);
	}

	static {
		Map<String, Command> map = new HashMap<>();
		for (Command c : values()) {
			map.put(c.name().toLowerCase(), c);
			map.put(c.getCmd(), c);
		}
		BY_NAME = Collections.unmodifiableMap(map);
	}

	public static Command byName(String cmd) {
		Command c = BY_NAME.get(cmd);
		if (c == null) {
			throw new IllegalArgumentException("Command '" + cmd + "' not known");
		}
		return c;
	}
}
