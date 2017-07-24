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
import java.util.Collection;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CommandTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.stream(Command.values()).map(o -> new Object[]{o}).collect(Collectors.toList());
	}

	@Parameterized.Parameter
	public Command command;

	@Test
	public void testGetCmdMatchesName() {
		assertEquals(command.getCmd(), command.name().replaceAll("_", "").toLowerCase());
	}

	@Test
	public void testNameUppercase() {
		for(char c : command.name().toCharArray()) {
			assertTrue(c == '_' || Character.isUpperCase(c));
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetOptionsIsImmutableAdd() {
		command.getOptions().add("foorbarbaz");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetFlagsIsImmutableAdd() {
		command.getFlags().add("foorbarbaz");
	}

	@Test
	public void testOptionsIsValid() {
		for(String s : command.getOptions()) {
			assertTrue(command.isValidOption(s));
		}
		assertFalse(command.isValidOption("foorbarbaz"));
	}

	@Test
	public void testFlagIsValid() {
		for(String s : command.getFlags()) {
			assertTrue(command.isValidFlag(s));
		}
		assertFalse(command.isValidFlag("foorbarbaz"));
	}

	@Test
	public void testBuildUsing() {
		assertEquals(command, command.buildUsing().getCmd());
	}

	@Test
	public void testBuild() {
		assertEquals(command.getCmd(), command.build().getCmd());
	}

	@Test
	public void testByName() {
		assertEquals(command, Command.byName(command.getCmd()));
	}

}
