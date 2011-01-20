/*  ContourQuantizerTest.java

    Copyright 2009-2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import org.junit.*;

import static junit.framework.Assert.*;


/**
 * A Test class for ContourQuantizer.
 *
 * @see ContourQuantizer
 */
public class ContourQuantizerTest {

  @Test
  public void testConstructor() {
    ContourQuantizer cq = new ContourQuantizer(5, 2, 0.0, 0.6);

    assertEquals(5, cq.time_bins);
    assertEquals(2, cq.value_bins);
  }

  @Test
  public void testQuantizedSize() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    ContourQuantizer cq = new ContourQuantizer(6, 2, 0.0, 0.6);
    try {
      int[] quantized = cq.quantize(c);
      assertEquals(cq.time_bins, quantized.length);
    } catch (ContourQuantizerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testQuantizedValues() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    ContourQuantizer cq = new ContourQuantizer(6, 2, 0.0, 0.6);
    try {
      int[] quantized = cq.quantize(c);
      for (int i = 0; i < values.length; ++i) {        
        if (values[i] < 0.3) {
          assertEquals(0, quantized[i]);
        } else {
          assertEquals(1, quantized[i]);
        }        
      }
    } catch (ContourQuantizerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testQuantizedValuesOutsideLimits() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    ContourQuantizer cq = new ContourQuantizer(6, 2, 0.25, 0.26);
    try {
      int[] quantized = cq.quantize(c);
      for (int i = 0; i < values.length; ++i) {
        if (values[i] < 0.25) {
          assertEquals(0, quantized[i]);
        } else {
          assertEquals(1, quantized[i]);
        }
      }
    } catch (ContourQuantizerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testQuantizedValuesCollapseValues() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    ContourQuantizer cq = new ContourQuantizer(3, 2, 0.0, 0.6);
    try {
      int[] quantized = cq.quantize(c);
      assertEquals(0, quantized[0]);
      assertEquals(0, quantized[1]);
      assertEquals(1, quantized[2]);      
    } catch (ContourQuantizerException e) {
      e.printStackTrace();
    }
  }
  @Test
  public void testQuantizeThrowsExceptionOnSmallValues() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    ContourQuantizer cq = new ContourQuantizer(10, 2, 0.0, 0.6);
    try {
      int[] quantized = cq.quantize(c);
      fail();
    } catch (ContourQuantizerException e) {
      assertTrue(true);
    }
  }

}
