/*  NegativeSymmetricListTest.java

    Copyright (c) 2011 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI.core;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class PairTest {
  @Test
  public void testEmptyConstructor() {
    Pair<Double, Double> p = new Pair<Double, Double>();
    assertNull(p.first);
    assertNull(p.second);
  }

  @Test
  public void testConstructor() {
    Pair<Double, Double> p = new Pair<Double, Double>(0.1, 3.4);
    assertEquals(0.1, p.first, 0.0001);
    assertEquals(3.4, p.second, 0.0001);
  }

  @Test
  public void testConstructorStrings() {
    Pair<String, String> p = new Pair<String, String>("one", "two");
    assertEquals("one", p.first);
    assertEquals("two", p.second);
  }
}

