/*  ContextFrameTest.java

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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test Class for ContextFrame.
 *
 * @see ContextFrame
 */
public class ContextFrameTest {

  @Test
  public void testIncrementWorksWithNullAttributes() {
    List<Region> words = new ArrayList<Region>();
    words.add(new Word(0, 1, "one"));
    words.add(new Word(0, 1, "two"));
    words.add(new Word(0, 1, "three"));
    words.add(new Word(0, 1, "four"));
    words.add(new Word(0, 1, "five"));

    ContextFrame frame = new ContextFrame(words, "test_feature", 1, 0);

    frame.init();
    try {
      frame.increment();
    } catch (NullPointerException e) {
      fail();
    }
  }

  @Test
  public void testInit() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();
  }

  @Test
  public void testInitWithContours() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();
  }


  @Test
  public void testIncrementToTheEnd() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();

    for (int i = 0; i < words.size(); ++i) {
      try {
        frame.increment();
      } catch (NullPointerException e) {
        fail();
      }
    }
  }

  @Test
  public void testIncrementToTheEndWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();

    for (int i = 0; i < words.size(); ++i) {
      try {
        frame.increment();
      } catch (NullPointerException e) {
        fail();
      }
    }
  }

  @Test
  public void testIncrementPassingTheEnd() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();

    for (int i = 0; i < words.size() + 2; ++i) {
      try {
        frame.increment();
      } catch (NullPointerException e) {
        fail();
      }
    }
  }

  @Test
  public void testIncrementPassingTheEndWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    frame.init();

    for (int i = 0; i < words.size() + 2; ++i) {
      try {
        frame.increment();
      } catch (NullPointerException e) {
        fail();
      }
    }
  }

  @Test
  public void testIncrementFrameSize() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(2, frame.getSize());
    frame.increment();
    // centered on word 2
    assertEquals(3, frame.getSize());
    frame.increment();
    // centered on word 3
    assertEquals(3, frame.getSize());
    frame.increment();
    // centered on word 4
    assertEquals(3, frame.getSize());
    frame.increment();
    // centered on word 5
    assertEquals(2, frame.getSize());
    frame.increment();
    // centered on (nonexistent) word 6
    assertEquals(0, frame.getSize());
  }

  @Test
  public void testIncrementFrameSizeWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(4, frame.getSize());
    frame.increment();
    // centered on word 2
    assertEquals(6, frame.getSize());
    frame.increment();
    // centered on word 3
    assertEquals(6, frame.getSize());
    frame.increment();
    // centered on word 4
    assertEquals(6, frame.getSize());
    frame.increment();
    // centered on word 5
    assertEquals(4, frame.getSize());
    frame.increment();
    // centered on (nonexistent) word 6
    assertEquals(0, frame.getSize());
  }

  @Test
  public void testIncrementMean() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(1.5, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(2.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(3.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(4.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(4.5, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(0.0, frame.getMean(), 0.0001);
  }

  @Test
  public void testIncrementMeanWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(1.5, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(2.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(3.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(4.0, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(4.5, frame.getMean(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(0.0, frame.getMean(), 0.0001);
  }

  @Test
  public void testIncrementStdev() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(0.7071067811865476, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(1.0, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(1.0, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(1.0, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(0.7071067811865476, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(0.0, frame.getStdev(), 0.0001);
  }

  @Test
  public void testIncrementStdevWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{3.0, 3.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{4.0, 4.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(0.5773502691896257, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(0.8944271909999159, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(0.8944271909999159, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(0.8944271909999159, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(0.5773502691896257, frame.getStdev(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(0.0, frame.getStdev(), 0.0001);
  }

  @Test
  public void testIncrementMax() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 1);
    w4.setAttribute("feature", 0);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(5.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(5.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(-Double.MAX_VALUE, frame.getMax(), 0.0001);
  }

  @Test
  public void testIncrementMaxWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{1.0, 1.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{0.0, 0.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(2.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(5.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(5.0, frame.getMax(), 0.0001);
    frame.increment();
    // centered on word 6
    assertEquals(-Double.MAX_VALUE, frame.getMax(), 0.0001);
  }

  @Test
  public void testIncrementMin() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 1);
    w4.setAttribute("feature", -1);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 6
    Assert.assertEquals(Double.MAX_VALUE, frame.getMin(), 0.0001);
  }

  @Test
  public void testIncrementMinWithContour() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", new Contour(0, 0.5, new double[]{1.0, 1.0}));
    w2.setAttribute("feature", new Contour(1, 0.5, new double[]{2.0, 2.0}));
    w3.setAttribute("feature", new Contour(2, 0.5, new double[]{1.0, 1.0}));
    w4.setAttribute("feature", new Contour(3, 0.5, new double[]{-1.0, -1.0}));
    w5.setAttribute("feature", new Contour(4, 0.5, new double[]{5.0, 5.0}));
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);

    // centered on word 1
    assertEquals(1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 2
    assertEquals(1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 3
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 4
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 5
    assertEquals(-1.0, frame.getMin(), 0.0001);
    frame.increment();
    // centered on word 6
    Assert.assertEquals(Double.MAX_VALUE, frame.getMin(), 0.0001);
  }

  @Test
  public void testMultipleCallsToInit() {
    List<Region> words = new ArrayList<Region>();
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(1, 2, "two");
    Word w3 = new Word(2, 3, "three");
    Word w4 = new Word(3, 4, "four");
    Word w5 = new Word(4, 5, "five");
    w1.setAttribute("feature", 1);
    w2.setAttribute("feature", 2);
    w3.setAttribute("feature", 3);
    w4.setAttribute("feature", 4);
    w5.setAttribute("feature", 5);
    words.add(w1);
    words.add(w2);
    words.add(w3);
    words.add(w4);
    words.add(w5);

    ContextFrame frame = new ContextFrame(words, "feature", 1, 1);
    assertEquals(2, frame.getSize());
    frame.init();
    assertEquals(2, frame.getSize());
  }
}
