/*  SpectrumTest.java

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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Test class for Spectrum.
 *
 * @see Spectrum
 */
public class SpectrumTest {

  @Test
  public void testStartTime() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertEquals(1.0, s.getStartingTime(), 0.0001);
  }

  @Test
  public void testFrameSize() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertEquals(0.01, s.getFrameSize(), 0.0001);
  }

  @Test
  public void testNumFrames() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertEquals(20, s.numFrames());
  }

  @Test
  public void testNumFreqs() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertEquals(10, s.numFreqs());
  }

  @Test
  public void testGet() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertEquals(4.0, s.get(7, 4), 0.0001);
  }

  @Test
  public void testGetTimeSlice() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    assertArrayEquals(new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, s.get(7), 0.0001);
  }

  @Test
  public void testGetEmptySliceBelowRange() {
    double[][] data = new double[10][10];

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = 0;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(0.1, 0.4);
      assertEquals(0, sub_s.numFrames());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSliceThrowsExceptionOnNegativeSize() {
    double[][] data = new double[10][10];

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = 0;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(0.6, 0.4);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetSliceIsNullOnEmptyData() {
    double[][] data = new double[0][0];

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(0.4, 0.5);
      assertNull(sub_s);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSliceGetsCorrectNumberOfFrames() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(1.034, 1.056);
      assertEquals(2, sub_s.numFrames());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSliceGetsCorrectNumberOfFramesWithOverhang() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(1.034, 2.0);
      assertEquals(16, sub_s.numFrames());
    } catch (AuToBIException e) {
      fail();
    }
  }


  @Test
  public void testGetSliceGetsCorrectNumberOfFramesWithUnderhang() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(0.00, 1.034);
      assertEquals(4, sub_s.numFrames());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSliceSetsParamsCorrectly() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(1.034, 1.056);
      assertEquals(0.01, sub_s.getFrameSize());
      assertEquals(10, sub_s.numFreqs());
      assertEquals(1.04, sub_s.getStartingTime());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetEmptySliceAboveRange() {
    double[][] data = new double[10][10];

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = 0;
      }
    }
    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Spectrum sub_s = s.getSlice(10.1, 10.4);
      assertEquals(0, sub_s.numFrames());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetPowerLength() {
    double[][] data = new double[20][10];

    for (int i = 0; i < 20; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPower(false);
      assertEquals(20, power.length);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerValues() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPower(false);
      assertArrayEquals(new double[]{45.0, 45.0, 45.0, 45.0, 45.0}, power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerLogValues() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPower(true);
      assertArrayEquals(new double[]{Math.log(45.0), Math.log(45.0), Math.log(45.0), Math.log(45.0), Math.log(45.0)},
          power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandValuesFullBand() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(0.0, 5000, false);
      assertArrayEquals(new double[]{45.0, 45.0, 45.0, 45.0, 45.0}, power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandValuesPartialBand() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(15, 34, false);
      assertArrayEquals(new double[]{5.0, 5.0, 5.0, 5.0, 5.0}, power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandValuesPartialBandLogValues() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(15, 34, true);
      assertArrayEquals(new double[]{Math.log(5.0), Math.log(5.0), Math.log(5.0), Math.log(5.0), Math.log(5.0)}, power,
          0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandValuesPartialUnderrunBand() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(-15, 34, false);
      assertArrayEquals(new double[]{6.0, 6.0, 6.0, 6.0, 6.0}, power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandValuesPartialOverrunBand() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(75, 340, false);
      assertArrayEquals(new double[]{17.0, 17.0, 17.0, 17.0, 17.0}, power, 0.0001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerInBandThrowsAnErrorOnBadBounds() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      double[] power = s.getPowerInBand(750, 340, false);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetPowerContour() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Contour power = s.getPowerContour(15, 34, true);
      assertEquals(5, power.size());
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetPowerTiltContour() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    try {
      Contour power = s.getPowerTiltContour(15, 34, false);
      assertEquals(5, power.size());
      assertEquals(5.0 / 45.0, power.get(0));
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetSpectralTiltContour() {
    double[][] data = new double[5][10];

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 10; ++j) {
        data[i][j] = j;
      }
    }

    Spectrum s = new Spectrum(data, 1.0, 0.01, 10);

    Contour tilt = s.getSpectralTiltContour();
    assertEquals(5, tilt.size());
    assertEquals(0.025998492074700428, tilt.get(0));

  }
}
