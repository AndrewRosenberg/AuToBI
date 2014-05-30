/*  TiltFeatureExtractorTest.java

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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for TiltFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.TiltFeatureExtractor
 */
@SuppressWarnings("unchecked")
public class TemporalContextNormalizedFeatureExtractorTest {

  private TemporalContextNormalizedFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new TemporalContextNormalizedFeatureExtractor("contour", 400, 400);
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    assertEquals(3, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("zMaxTimeContext[contour,400,400]"));
    assertTrue(fe.getExtractedFeatures().contains("zMinTimeContext[contour,400,400]"));
    assertTrue(fe.getExtractedFeatures().contains("zMeanTimeContext[contour,400,400]"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    assertEquals(4, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("contour"));
    assertTrue(fe.getRequiredFeatures().contains("min[contour]"));
    assertTrue(fe.getRequiredFeatures().contains("max[contour]"));
    assertTrue(fe.getRequiredFeatures().contains("mean[contour]"));
  }

  @Test
  public void testExtractFeatureExtractsFeaturesWithUnavailableContext() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{3.0, 4.0, 1.0}));
    w.setAttribute("min[contour]", 1.0);
    w.setAttribute("max[contour]", 4.0);
    w.setAttribute("mean[contour]", 8.0 / 3);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("zMaxTimeContext[contour,400,400]"));
      assertTrue(w.hasAttribute("zMeanTimeContext[contour,400,400]"));
      assertTrue(w.hasAttribute("zMinTimeContext[contour,400,400]"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectlyWithUnavailableContext() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{3.0, 4.0, 1.0}));
    w.setAttribute("min[contour]", 1.0);
    w.setAttribute("max[contour]", 4.0);
    w.setAttribute("mean[contour]", 8.0 / 3);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals(0.8729, (Double) w.getAttribute("zMaxTimeContext[contour,400,400]"), 0.0001);
      assertEquals(-1.091, (Double) w.getAttribute("zMinTimeContext[contour,400,400]"), 0.0001);
      assertEquals(0.0, (Double) w.getAttribute("zMeanTimeContext[contour,400,400]"), 0.0001);
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }
}
