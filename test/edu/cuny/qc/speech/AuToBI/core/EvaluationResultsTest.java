/*  EvaluationResultsTest.java

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
import weka.classifiers.Evaluation;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class EvaluationResultsTest {

  @Test
  public void testConstructorWithListClassNames() {
    List<String> classes = new ArrayList<String>();
    classes.add("class_one");
    classes.add("class_two");

    EvaluationResults eval = new EvaluationResults(classes);

    Assert.assertEquals(2, eval.getNumClasses());
  }

  @Test
  public void testConstructorWithArrayClassNames() {
    String[] classes = new String[]{"one", "two"};

    EvaluationResults eval = new EvaluationResults(classes);

    Assert.assertEquals(2, eval.getNumClasses());
  }

  @Test
  public void testConstructorWithContingencyMatrix() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);
      Assert.assertEquals(2, eval.getNumClasses());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testConstructorWithContingencyMatrixFailsWithBadClassLengths() {
    String[] classes = new String[]{"one"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testConstructorWithContingencyMatrixFailsWithNonSquareCM() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetClassNames() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);
      assertEquals(2, eval.getClassNames().length);
      assertEquals("one", eval.getClassNames()[0]);
      assertEquals("two", eval.getClassNames()[1]);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAddInstance() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      eval.addInstance("one", "one");

      assertEquals(1.0, eval.getInstances("one", "one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAddTwoEvaluationResults() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      double[][] cm_b = new double[][]{{1.0, 1.0}, {1.0, 1.0}};
      EvaluationResults eval_b = new EvaluationResults(classes, cm_b);

      eval.add(eval_b);

      assertEquals(1.1, eval.getInstances("two", "one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testAddTwoEvaluationResultsFailsWhenClassesAreNotAvailable() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      double[][] cm_b = new double[][]{{1.0, 1.0}, {1.0, 1.0}};
      EvaluationResults eval_b = new EvaluationResults(new String[]{"two", "three"}, cm_b);

      eval.add(eval_b);

      fail();
    } catch (AuToBIException e) {
      // Expected
    }
  }

  @Test
  public void testAddMultipleInstances() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      eval.addInstances("one", "one", 4);

      assertEquals(4.0, eval.getInstances("one", "one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }


  @Test
  public void testGetInstancesWithBadClassName() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.1}, {1.0, 0.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      eval.getInstances("false", "two");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetNumCorrrect() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 0.1}, {1.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(7.0, eval.getNumCorrect(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetPrecision() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 1.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.5, eval.getPrecision("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetPrecisionWorksIfNoneClassified() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 1.0}, {0.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.0, eval.getPrecision("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetRecall() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 1.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.666666666, eval.getRecall("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }


  @Test
  public void testGetRecallWorksWithEmptyClasses() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.0, eval.getRecall("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFMeasure() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 1.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.5714285714285715, eval.getFMeasure("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFMeasureWorksWithPotentialZeroDenominator() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.0}, {0.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.0, eval.getFMeasure("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFalsePositiveRate() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 1.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(2.0 / 7.0, eval.getFalsePositiveRate("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFalseNegativeRate() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{2.0, 1.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.3333333, eval.getFalseNegativeRate("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetFalseNegativeRateWorksWithZeroPositiveInstances() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{0.0, 0.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.0, eval.getFalseNegativeRate("one"), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetBalancedErrorRate() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{1.0, 2.0}, {2.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.4761904761904762, eval.getBalancedErrorRate(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetMutualInformationWithCorrectClassification() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{1.0, 0.0}, {0.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.45056120886630463, eval.getMutualInformation(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetMutualInformationWithIncorrectClassification() {
    String[] classes = new String[]{"one", "two"};

    double[][] cm = new double[][]{{5.0, 5.0}, {5.0, 5.0}};

    EvaluationResults eval = null;
    try {
      eval = new EvaluationResults(classes, cm);

      assertEquals(0.0, eval.getMutualInformation(), 0.0001);
    } catch (AuToBIException e) {
      fail();
    }
  }
}

