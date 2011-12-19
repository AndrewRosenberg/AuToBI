/*  PitchCandidateTest.java

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
 * Tests for PitchCandidate
 */
public class PitchCandidateTest {
  @Test
  public void testEmptyConstructor() {
    PitchCandidate pc = new PitchCandidate();

    Assert.assertEquals(0.0, pc.frequency);
    Assert.assertEquals(0.0, pc.strength);
  }

  @Test
  public void testConstructor() {
    PitchCandidate pc = new PitchCandidate(1.2, 3.4);

    Assert.assertEquals(1.2, pc.frequency);
    Assert.assertEquals(3.4, pc.strength);
  }
}

