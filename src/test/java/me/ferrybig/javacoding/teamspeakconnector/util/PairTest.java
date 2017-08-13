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
package me.ferrybig.javacoding.teamspeakconnector.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PairTest {

	@Test
	public void testEquals() {
		Object a = new Object();
		Object b = new Object();
		Object c = null;

		Pair<Object, Object> pab1 = new Pair<>(a, b);
		Pair<Object, Object> pab2 = new Pair<>(a, b);
		Pair<Object, Object> pac = new Pair<>(a, c);
		Pair<Object, Object> pba = new Pair<>(b, a);

		assertTrue(pab1.equals(pab1));
		assertTrue(pab1.equals(pab2));
		assertFalse(pab1.equals(pac));
		assertFalse(pab1.equals(pba));

		assertTrue(pab2.equals(pab1));
		assertTrue(pab2.equals(pab2));
		assertFalse(pab2.equals(pac));
		assertFalse(pab2.equals(pba));

		assertFalse(pac.equals(pab1));
		assertFalse(pac.equals(pab2));
		assertTrue(pac.equals(pac));
		assertFalse(pac.equals(pba));

		assertFalse(pba.equals(pab1));
		assertFalse(pba.equals(pab2));
		assertFalse(pba.equals(pac));
		assertTrue(pba.equals(pba));
	}

	@Test
	public void testGetFirst() {
		Object expResult = new Object();
		Pair<Object, Object> instance = new Pair<>(expResult, null);

		Object result = instance.getFirst();

		assertEquals(expResult, result);
	}

	@Test
	public void testGetSecond() {
		Object expResult = new Object();
		Pair<Object, Object> instance = new Pair<>(null, expResult);

		Object result = instance.getSecond();

		assertEquals(expResult, result);
	}

	@Test
	public void testHashCode() {
		Object a = new Object();
		Object b = new Object();
		Object c = null;

		Pair<Object, Object> pab1 = new Pair<>(a, b);
		Pair<Object, Object> pab2 = new Pair<>(a, b);
		Pair<Object, Object> pac = new Pair<>(a, c);
		Pair<Object, Object> pba = new Pair<>(b, a);

		assertEquals(pab1.hashCode(), pab1.hashCode());
		assertEquals(pab1.hashCode(), pab2.hashCode());
		assertEquals(pab2.hashCode(), pab2.hashCode());
		assertEquals(pac.hashCode(), pac.hashCode());
		assertEquals(pba.hashCode(), pba.hashCode());
	}

	@Test
	public void testToString() {
		Pair<Object, Object> instance = new Pair<>(null, null);
		String expResult = "Pair{first=null, second=null}";

		String result = instance.toString();

		assertEquals(expResult, result);
	}

	@Test
	public void testToStringWithString() {
		Pair<Object, Object> instance = new Pair<>("foo", "bar");
		String expResult = "Pair{first=foo, second=bar}";

		String result = instance.toString();

		assertEquals(expResult, result);
	}

}
