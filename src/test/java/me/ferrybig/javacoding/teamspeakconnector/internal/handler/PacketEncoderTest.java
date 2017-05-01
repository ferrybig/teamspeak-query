/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.teamspeakconnector.internal.handler;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fernando van Loenhout <mailmehere@ferrybig.me>
 */
public class PacketEncoderTest {

	private static final PacketEncoder ENCODER = new PacketEncoder();

	public PacketEncoderTest() {
	}

	@Test
	public void testEncodeTeamspeakCodeNoEncoding() {
		Assert.assertEquals("test", ENCODER.encodeTeamspeakCode("test"));
		Assert.assertEquals("testEncodeTeamspeakCodeNoEncoding", ENCODER.encodeTeamspeakCode("testEncodeTeamspeakCodeNoEncoding"));
	}

	@Test
	public void testEncodeTeamspeakCodeEncoding() {
		Assert.assertEquals("\\s", ENCODER.encodeTeamspeakCode(" "));
		Assert.assertEquals("Teamspeak\\sServer", ENCODER.encodeTeamspeakCode("Teamspeak Server"));
		Assert.assertEquals("TeamSpeak\\s]\\p[\\sServer", ENCODER.encodeTeamspeakCode("TeamSpeak ]|[ Server"));
	}

	@Test
	public void testEncodeTeamspeakCodeManyCharacterEncoding() {
		Assert.assertEquals("\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s\\s",
				ENCODER.encodeTeamspeakCode("                                "));
	}

}
