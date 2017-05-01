/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector;

/**
 *
 * @author Fernando
 */

//clientlist -uid -away -voice -times -groups -info -icon -country
// clid=1 cid=1 client_database_id=1 client_nickname=serveradmin\sfrom\s127.0.0.1:45824 client_type=1 client_away=0 client_away_message client_flag_talking=0 client_input_muted=0 client_output_muted=0 client_input_hardware=0 client_output_hardware=0 client_talk_power=0 client_is_talker=0 client_is_priority_speaker=0 client_is_recording=0 client_is_channel_commander=0 client_unique_identifier=serveradmin client_servergroups=2 client_channel_group_id=8 client_channel_group_inherited_channel_id=1 client_version=ServerQuery client_platform=ServerQuery client_idle_time=67206 client_created=0 client_lastconnected=0 client_icon_id=0 client_country
public class User extends UnresolvedUser {
	
	public User(int id) {
		super(id);
	}
	
}
