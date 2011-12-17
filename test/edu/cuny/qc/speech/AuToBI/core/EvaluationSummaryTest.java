/*  EvaluationSummaryTest.java

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

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class EvaluationSummaryTest {

  @Test
  public void testEmptyConstructor() {
    EvaluationSummary es = new EvaluationSummary();
  }

  @Test
  public void testOneEvalConstructor() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      Assert.assertEquals(1, es.results.size());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAdd() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval);
      Assert.assertEquals(2, es.results.size());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAddFailsOnInconsistentEvaluationResultsClassNames() {
    EvaluationResults eval = null;
    EvaluationResults eval2 = null;
    try {
      eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});
      eval2 = new EvaluationResults(new String[]{"two", "three"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});
    } catch (AuToBIException e) {
      fail();
    }

    EvaluationSummary es = new EvaluationSummary(eval);

    try {
      es.add(eval2);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testAddFailsOnInconsistentEvaluationResultsClassSizes() {
    EvaluationResults eval = null;
    EvaluationResults eval2 = null;
    try {
      eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});
      eval2 = new EvaluationResults(new String[]{"two"},
          new double[][]{{1.0}});
    } catch (AuToBIException e) {
      fail();
    }

    EvaluationSummary es = new EvaluationSummary(eval);

    try {
      es.add(eval2);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetAccuracy() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.66666, es.getAccuracy(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetStdevAccuracy() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.1767766952966369, es.getStdevAccuracy(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFMeasure() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.6666666, es.getFMeasure("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetPrecision() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.666666, es.getPrecision("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetRecall() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.666666, es.getRecall("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetMeanMutualInformation() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(0.06540601797056848, es.getMutualInformation(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetNumClassInstances() {
    try {
      EvaluationResults eval = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{1.0, 1.0}, {1.0, 1.0}});

      EvaluationResults eval2 = new EvaluationResults(new String[]{"one", "two"},
          new double[][]{{3.0, 1.0}, {1.0, 3.0}});

      EvaluationSummary es = new EvaluationSummary(eval);

      es.add(eval2);
      assertEquals(6, (long) es.getNumClassInstances("one"));
    } catch (AuToBIException e) {
      fail();
    }
  }
}

