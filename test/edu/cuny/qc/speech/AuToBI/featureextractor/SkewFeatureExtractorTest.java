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
    fe = new SkewFeatureExtractor("f0", "I");
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    assertEquals(2, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("skewAmp[f0,I]"));
    assertTrue(fe.getExtractedFeatures().contains("skewDur[f0,I]"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    assertEquals(4, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("tiltAmp[I]"));
    assertTrue(fe.getRequiredFeatures().contains("tiltDur[I]"));
    assertTrue(fe.getRequiredFeatures().contains("tiltAmp[f0]"));
    assertTrue(fe.getRequiredFeatures().contains("tiltDur[f0]"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("tiltAmp[I]", 1.0);
    w.setAttribute("tiltDur[I]", 2.0);
    w.setAttribute("tiltAmp[f0]", 3.0);
    w.setAttribute("tiltDur[f0]", 4.0);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("skewAmp[f0,I]"));
      assertTrue(w.hasAttribute("skewDur[f0,I]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("tiltAmp[I]", 1.0);
    w.setAttribute("tiltDur[I]", 2.0);
    w.setAttribute("tiltAmp[f0]", 3.0);
    w.setAttribute("tiltDur[f0]", 5.0);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals(2.0, (Double) w.getAttribute("skewAmp[f0,I]"), 0.0001);
      assertEquals(3.0, (Double) w.getAttribute("skewDur[f0,I]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
