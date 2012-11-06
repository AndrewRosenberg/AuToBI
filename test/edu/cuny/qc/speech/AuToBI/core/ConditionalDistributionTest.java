/*  ConditionalDistributionTest.java

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

import static org.junit.Assert.*;

/**
 * Test Class for ConditionalDistribution.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.ConditionalDistribution
 */
public class ConditionalDistributionTest {

  @Test
  public void testConstruction() {
    ConditionalDistribution cd = new ConditionalDistribution();

    assertEquals(0, cd.size());
  }

  @Test
  public void testAdd() {
    ConditionalDistribution cd = new ConditionalDistribution();

    cd.add("one", "a");
    cd.add("two", "a");
    cd.add("one", "a");
    cd.add("one", "b");

    assertEquals(2, cd.size());
    assertEquals(2, cd.get("one").size());
    assertEquals(2.0, cd.get("one").get("a"), 0.0001);
  }

  @Test
  public void testAddWithWeight() {
    ConditionalDistribution cd = new ConditionalDistribution();

    cd.add("one", "a", 1);
    cd.add("two", "a");
    cd.add("one", "a", 3);
    cd.add("one", "b");

    assertEquals(2, cd.size());
    assertEquals(2, cd.get("one").size());
    assertEquals(4.0, cd.get("one").get("a"), 0.0001);
  }


  @Test
  public void testNormalize() {
    ConditionalDistribution cd = new ConditionalDistribution();

    cd.add("one", "a", 1);
    cd.add("two", "a");
    cd.add("one", "a", 3);
    cd.add("one", "b");

    try {
      cd.normalize();
    } catch (AuToBIException e) {
      fail();
    }
    assertEquals(2, cd.size());
    assertEquals(2, cd.get("one").size());
    assertEquals(0.8, cd.get("one").get("a"), 0.0001);
    assertEquals(0.2, cd.get("one").get("b"), 0.0001);
  }

  @Test
  public void testNormalizeWithZeroMass() {
    ConditionalDistribution cd = new ConditionalDistribution();

    cd.add("one", "a", 0);
    cd.add("two", "a");
    cd.add("one", "a", 0);
    cd.add("one", "b");

    assertEquals(2, cd.size());
    assertEquals(2, cd.get("one").size());
    assertEquals(0.0, cd.get("one").get("a"), 0.0001);

    try {
      cd.normalize();
    } catch (AuToBIException e) {
      fail();
    }
    // With zero mass, normalization should not change the value.
    assertEquals(0.0, cd.get("one").get("a"), 0.0001);
  }

}



