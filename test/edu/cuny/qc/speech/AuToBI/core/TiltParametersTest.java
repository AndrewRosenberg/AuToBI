/*  TiltParametersTest.java

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Test class for TiltParameters.
 *
 * @see TiltParameters
 */
public class TiltParametersTest {
  @Test
  public void testEmptyConstructor() {
    TiltParameters tp = new TiltParameters();

    assertEquals(0.0, tp.getTilt());
  }

  @Test
  public void testContourConstructor() {
    Contour c = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(1.0, tp.getTilt());
  }

  @Test
  public void testCalculateTiltOnEmptyContour() {
    Contour c = new Contour(0.0, 1.0, new double[]{});

    TiltParameters tp = new TiltParameters();
    tp.calculateTilt(c);
    assertEquals(0.0, tp.getTilt());
  }

  @Test
  public void testCalculateTiltOnNullContour() {
    TiltParameters tp = new TiltParameters();
    tp.calculateTilt(null);
    assertEquals(0.0, tp.getTilt());
  }

  @Test
  public void testGetDurationTiltRising() {
    Contour c = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(1.0, tp.getDurationTilt());
  }

  @Test
  public void testGetAmplitudeTiltRising() {
    Contour c = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(1.0, tp.getAmplitudeTilt());
  }

  @Test
  public void testGetTiltRising() {
    Contour c = new Contour(0.0, 1.0, new double[]{1.0, 2.0, 3.0});

    TiltParameters tp = new TiltParameters(c);

    assertEquals(1.0, tp.getTilt());
  }

  @Test
  public void testGetDurationTiltFalling() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 2.0, 1.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(-1.0, tp.getDurationTilt());
  }

  @Test
  public void testGetAmplitudeTiltFalling() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 2.0, 1.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(-1.0, tp.getAmplitudeTilt());
  }

  @Test
  public void testGetTiltFalling() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 2.0, 1.0});

    TiltParameters tp = new TiltParameters(c);

    assertEquals(-1.0, tp.getTilt());
  }


  @Test
  public void testGetDurationTiltPeak() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 4.0, 1.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(0.0, tp.getDurationTilt());
  }

  @Test
  public void testGetAmplitudeTiltPeak() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 4.0, 1.0});

    TiltParameters tp = new TiltParameters(c);
    assertEquals(-0.5, tp.getAmplitudeTilt(), 0.0001);
  }

  @Test
  public void testGetTiltPeak() {
    Contour c = new Contour(0.0, 1.0, new double[]{3.0, 4.0, 1.0});

    TiltParameters tp = new TiltParameters(c);

    assertEquals(-0.25, tp.getTilt(), 0.0001);
  }


}
