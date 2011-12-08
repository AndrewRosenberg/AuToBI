/*  ContextDescTest.java

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

import static org.junit.Assert.assertEquals;

/**
 * Test Class for ContextDesc.
 *
 * @see ContextDesc
 */
public class ContextDescTest {

  @Test
  public void testConstruction() {
    ContextDesc cd = new ContextDesc("label", 1, 2);
    assertEquals("label", cd.getLabel());
    assertEquals(1, (long) cd.getForward());
    assertEquals(1, (long) cd.getForward());
    assertEquals(2, (long) cd.getBack());
  }

  @Test
  public void testGetAndSetLabel() {
    ContextDesc cd = new ContextDesc("label", 1, 2);

    cd.setLabel("new");
    assertEquals("new", cd.getLabel());
  }

  @Test
  public void testGetAndSetBackContext() {
    ContextDesc cd = new ContextDesc("label", 1, 2);

    cd.setBack(5);
    assertEquals(5, (long) cd.getBack());
  }

  @Test
  public void testGetAndSetForwardContext() {
    ContextDesc cd = new ContextDesc("label", 1, 2);

    cd.setForward(5);
    assertEquals(5, (long) cd.getForward());
  }

}



