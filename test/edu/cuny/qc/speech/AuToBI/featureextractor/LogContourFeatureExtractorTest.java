/*  LogContourFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test class for LogContourFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.IntonationalPhraseBoundaryFeatureExtractor
 */
public class LogContourFeatureExtractorTest {

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    LogContourFeatureExtractor fe = new LogContourFeatureExtractor("I");

    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("I"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {
    LogContourFeatureExtractor fe = new LogContourFeatureExtractor("I");

    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("log[I]"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    LogContourFeatureExtractor fe = new LogContourFeatureExtractor("I");

    List<Region> regions = new ArrayList<Region>();
    Word w = new Word(0, 1, "testing");
    w.setAttribute("I", new Contour(0, 0.01, new double[]{0.1, 0.5}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("log[I]"));
    } catch (FeatureExtractorException e) {
      fail();
    }

  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    LogContourFeatureExtractor fe = new LogContourFeatureExtractor("I");

    List<Region> regions = new ArrayList<Region>();
    Word w = new Word(0, 1, "testing");
    w.setAttribute("I", new Contour(0, 0.01, new double[]{0.1, 0.5}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      Contour c = (Contour) w.getAttribute("log[I]");
      assertEquals(2, c.size());
      assertEquals(Math.log(0.1), c.get(0));
      assertEquals(Math.log(0.5), c.get(1));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeaturesAssignsTheSameObjectToSubsequentRegions() {
    LogContourFeatureExtractor fe = new LogContourFeatureExtractor("I");

    Contour c = new Contour(0, 0.01, new double[]{0.1, 0.5});
    List<Region> regions = new ArrayList<Region>();
    Word w = new Word(0, .5, "testing");
    Word w2 = new Word(0.5, 1, "testing");
    w.setAttribute("I", c);
    w2.setAttribute("I", c);
    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.getAttribute("log[I]") == w2.getAttribute("log[I]"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }
}
