/*  PitchFrameTest.java

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for PitchCandidate
 */
public class PitchFrameTest {
  @Test
  public void testConstructorMakesCandidateList() {
    PitchFrame pf = new PitchFrame();

    assertEquals(0, pf.getNumCandidates());
  }

  @Test
  public void testSetAndGetIntensity() {
    PitchFrame pf = new PitchFrame();

    pf.setIntensity(4.5);
    assertEquals(4.5, pf.getIntensity(), 0.0001);
  }

  @Test
  public void testDefaultIntensity() {
    PitchFrame pf = new PitchFrame();

    assertEquals(0.0, pf.getIntensity());
  }

  @Test
  public void testAddCandidate() {
    PitchFrame pf = new PitchFrame();

    assertEquals(0, pf.getNumCandidates());
    pf.addCandidate();
    assertEquals(1, pf.getNumCandidates());
  }

  @Test
  public void testGetCandidate() {
    PitchFrame pf = new PitchFrame();

    pf.addCandidate();
    assertNotNull(pf.getCandidate(0));
  }

  @Test
  public void testSetAndGetCandidate() {
    PitchFrame pf = new PitchFrame();

    pf.addCandidate();
    PitchCandidate pc = new PitchCandidate(4, 5);
    pf.setCandidate(0, pc);
    assertEquals(pc, pf.getCandidate(0));
  }
}

