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
  public void testIterationSkipsEmptyEntries() {
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
        assertEquals(2.0 + 0.001 * (i+2), tvp.first, 0.0001);
        assertEquals(0.1 * (i+2), tvp.second, 0.0001);
      }
      ++i;
    }
    assertEquals(4, i);
  }
}
