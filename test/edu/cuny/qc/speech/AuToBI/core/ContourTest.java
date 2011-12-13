/*  ContourTest.java

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

import edu.cuny.qc.speech.AuToBI.featureextractor.NormalizedContourFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Contour.
 *
 * @see Contour
 */
public class ContourTest {

  @Test
  public void testConstructor() {
    Contour c = new Contour(0.0, 0.001, 6);
    assertEquals(6, c.size());
  }

  @Test
  public void testConstructorWithValues() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    assertEquals(6, c.size());
  }

  @Test
  public void testTimeFromIndex() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertEquals(2.002, c.timeFromIndex(2), 0.00001);
  }

  @Test
  public void testIndexFromTime() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertEquals(4, c.indexFromTime(2.004));
  }

  @Test
  public void testGet() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertEquals(0.4, c.get(2.004), 0.0001);
  }

  @Test
  public void testGetPair() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    Pair<Double, Double> pair = c.getPair(2);
    assertEquals(2.002, pair.first, 0.0001);
    assertEquals(0.2, pair.second, 0.0001);
  }

  @Test
  public void testGetPairOutOfRange() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    Pair<Double, Double> pair = c.getPair(8);
    assertNull(pair);
  }

  @Test
  public void testGetOutOfRange() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertTrue(Double.isNaN(c.get(10)));
  }

  @Test
  public void testSetAboveRange() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.set(7, 0.7);
    assertTrue(c.isEmpty(6));
    assertEquals(0.7, c.get(2.007), 0.0001);
  }

  @Test
  public void testSetEmpty() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.setEmpty(2);
    assertTrue(c.isEmpty(2));
  }

  @Test
  public void testSetEmptyByTime() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.setEmpty(2.002);
    assertTrue(c.isEmpty(2));
  }

  @Test
  public void testContentSize() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.setEmpty(2);
    assertEquals(5, c.contentSize());
  }

  @Test
  public void testContourForEach() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);


    int i = 0;
    for (Pair<Double, Double> tvp : c) {
      assertEquals(2.0 + 0.001 * i, tvp.first, 0.0001);
      assertEquals(0.1 * i, tvp.second, 0.0001);
      ++i;
    }
    assertEquals(6, i);
  }

  @Test
  public void testContourForEachSkipsEmptyEntries() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);

    c.setEmpty(2);
    c.setEmpty(3);

    int i = 0;
    for (Pair<Double, Double> tvp : c) {
      if (i < 2) {
        assertEquals(2.0 + 0.001 * i, tvp.first, 0.0001);
        assertEquals(0.1 * i, tvp.second, 0.0001);
      } else {
        assertEquals(2.0 + 0.001 * (i + 2), tvp.first, 0.0001);
        assertEquals(0.1 * (i + 2), tvp.second, 0.0001);
      }
      ++i;
    }
    assertEquals(4, i);
  }
}