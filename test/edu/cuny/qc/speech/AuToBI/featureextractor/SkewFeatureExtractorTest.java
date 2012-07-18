/*  SkewFeatureExtractorTest.java

    Copyright 2012 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test class for SkewFeatureExtractor
 *
 * @see SkewFeatureExtractor
 */
public class SkewFeatureExtractorTest {
  private SkewFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    fe = new SkewFeatureExtractor();
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(2, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("skew_amp"));
    assertTrue(fe.getExtractedFeatures().contains("skew_dur"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(4, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("I__tilt_amp"));
    assertTrue(fe.getRequiredFeatures().contains("I__tilt_dur"));
    assertTrue(fe.getRequiredFeatures().contains("f0__tilt_amp"));
    assertTrue(fe.getRequiredFeatures().contains("f0__tilt_dur"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("I__tilt_amp", 1.0);
    w.setAttribute("I__tilt_dur", 2.0);
    w.setAttribute("f0__tilt_amp", 3.0);
    w.setAttribute("f0__tilt_dur", 4.0);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("skew_amp"));
      assertTrue(w.hasAttribute("skew_dur"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("I__tilt_amp", 1.0);
    w.setAttribute("I__tilt_dur", 2.0);
    w.setAttribute("f0__tilt_amp", 3.0);
    w.setAttribute("f0__tilt_dur", 5.0);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals(2.0, (Double) w.getAttribute("skew_amp"), 0.0001);
      assertEquals(3.0, (Double) w.getAttribute("skew_dur"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
