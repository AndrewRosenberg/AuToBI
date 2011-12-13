/*  ContourIteratorTest.java

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
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class ContourIteratorTest {

  @Test
  public void testConstructorInitialization() {
    Contour c = new Contour(2.0, 0.001, new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5});
    ContourIterator ci = new ContourIterator(c);

    assertTrue(ci.hasNext());
  }

  @Test
  public void testIteration() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);
    ContourIterator ci = new ContourIterator(c);


    int i = 0;
    while (ci.hasNext()) {
      Pair<Double, Double> tvp = ci.next();
      assertEquals(2.0 + 0.001 * i, tvp.first, 0.0001);
      assertEquals(0.1 * i, tvp.second, 0.0001);
      ++i;
    }
    assertEquals(6, i);
  }

  @Test
  public void testHasNextSkipsEmptyEntries() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);
    ContourIterator ci = new ContourIterator(c);

    c.setEmpty(2);
    c.setEmpty(3);

    int i = 0;
    while (ci.hasNext()) {
      Pair<Double, Double> tvp = ci.next();
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

  @Test
  public void testNextEmptyEntries() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);
    ContourIterator ci = new ContourIterator(c);

    c.setEmpty(2);
    c.setEmpty(3);

    int i = 0;
    while (i < 4) {
      Pair<Double, Double> tvp = ci.next();
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

  @Test
  public void testRemove() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);
    ContourIterator ci = new ContourIterator(c);

    ci.next();
    ci.next();
    ci.remove();

    assertTrue(c.isEmpty(2));
  }

}
