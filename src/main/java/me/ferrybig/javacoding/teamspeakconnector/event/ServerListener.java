/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.event;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
//notifyclientleftview cfid=1 ctid=0 reasonid=8 reasonmsg=leaving clid=1
//notifycliententerview cfid=0 ctid=1 reasonid=0 clid=4 client_unique_identifier=P+m\/uXn4o2nLwN5gOimuQcfQZIQ= client_nickname=Ferrybig client_input_muted=0 client_output_muted=0 client_outputonly_muted=0 client_input_hardware=0 client_output_hardware=1 client_meta_data client_is_recording=0 client_database_id=2 client_channel_group_id=5 client_servergroups=6,10 client_away=0 client_away_message client_type=0 client_flag_avatar=12f359409d033f5eebcc821a5dbcecf5 client_talk_power=75 client_talk_request=0 client_talk_request_msg client_description client_is_talker=0 client_is_priority_speaker=0 client_unread_messages=0 client_nickname_phonetic=Ferrybig client_needed_serverquery_view_power=75 client_icon_id=0 client_is_channel_commander=0 client_country client_channel_group_inherited_channel_id=1 client_badges=Overwolf=0
//notifyclientleftview cfid=72 ctid=0 reasonid=8 reasonmsg=leaving clid=4
//notifycliententerview cfid=0 ctid=1 reasonid=0 clid=5 client_unique_identifier=P+m\/uXn4o2nLwN5gOimuQcfQZIQ= client_nickname=Ferrybig client_input_muted=0 client_output_muted=0 client_outputonly_muted=0 client_input_hardware=0 client_output_hardware=1 client_meta_data client_is_recording=0 client_database_id=2 client_channel_group_id=5 client_servergroups=6,10 client_away=0 client_away_message client_type=0 client_flag_avatar=12f359409d033f5eebcc821a5dbcecf5 client_talk_power=75 client_talk_request=0 client_talk_request_msg client_description client_is_talker=0 client_is_priority_speaker=0 client_unread_messages=0 client_nickname_phonetic=Ferrybig client_needed_serverquery_view_power=75 client_icon_id=0 client_is_channel_commander=0 client_country client_channel_group_inherited_channel_id=1 client_badges=Overwolf=0

public interface ServerListener {
	public default void onClientEnterView(ClientEnterViewEvent event) {
	}
	
	public default void onClientLeaveView(ClientLieveViewEvent event) {
	}
	
	public default void onEditServer(EditServerEvent event) {
	}
}
