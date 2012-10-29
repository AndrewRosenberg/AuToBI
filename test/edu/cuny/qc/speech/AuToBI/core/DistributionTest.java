/*  DistributionTest.java

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

import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class DistributionTest {

  @Test
  public void testAdd() {
    Distribution d = new Distribution();

    d.add("hello");

    assertEquals(1.0, d.get("hello"), 0.0001);
  }

  @Test
  public void testAddWithWeights() {
    Distribution d = new Distribution();

    d.add("hello", 0.5);

    assertEquals(0.5, d.get("hello"), 0.0001);
  }

  @Test
  public void testGetKeyWithMaxValue() {
    Distribution d = new Distribution();

    d.add("hello", 0.5);
    d.add("two", 0.2);
    d.add("three", 0.2);

    assertEquals("hello", d.getKeyWithMaximumValue());
  }

  @Test
  public void testGetKeyWithMaxValueWhenNoneExists() {
    Distribution d = new Distribution();

    assertNull(d.getKeyWithMaximumValue());
  }

  @Test
  public void testNormalize() {
    Distribution d = new Distribution();

    d.add("hello", 1.0);
    d.add("two", 1.0);
    d.add("three", 1.0);

    try {
      d.normalize();
    } catch (AuToBIException e) {
      e.printStackTrace();
    }

    assertEquals(0.33333, d.get("hello"), 0.0001);
  }

  @Test
  public void testNormalizeDoesNothingWhenSumEqualsZero() {
    Distribution d = new Distribution();

    d.add("hello", 0.5);
    d.add("two", -0.5);

    try {
      d.normalize();
    } catch (AuToBIException ignored) {
    }

    assertEquals(-0.5, d.get("two"), 0.0001);
  }

  @Test
  public void testNormalizeThrowsAnExceptionWhenSumEqualsZero() {
    Distribution d = new Distribution();

    d.add("hello", 0.5);
    d.add("two", -0.5);

    try {
      d.normalize();
      fail();
    } catch (AuToBIException ignored) {
    }
  }
}
