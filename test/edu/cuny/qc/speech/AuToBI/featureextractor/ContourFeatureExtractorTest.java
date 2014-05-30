/*  ContourFeatureExtractorTest.java

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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for ContourFeatureExtractor
 *
 * @see ContourFeatureExtractor
 */
public class ContourFeatureExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      regions.add(new Word(0, 1, "test"));

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("test_attribute");
      cfe.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesWorksWithZeroLengthContour() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("test_attribute", new Contour(0.0, 1.0, new double[]{}));
      regions.add(w);

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("test_attribute");
      cfe.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testSetAndGetAttributeName() {
    ContourFeatureExtractor cfe = new ContourFeatureExtractor();

    cfe.setAttributeName("test_attribute");
    assertEquals("test_attribute", cfe.getAttributeName());
  }

  @Test
  public void testSetAttributeNameCorrectlySetsExtractedFeatures() {
    ContourFeatureExtractor cfe = new ContourFeatureExtractor();

    cfe.setAttributeName("attr");

    List<String> features = cfe.getExtractedFeatures();

    assertTrue(features.contains("min[attr]"));
    assertTrue(features.contains("max[attr]"));
    assertTrue(features.contains("mean[attr]"));
    assertTrue(features.contains("stdev[attr]"));
    assertTrue(features.contains("zMax[attr]"));
    assertTrue(features.contains("maxLocation[attr]"));
    assertTrue(features.contains("maxRelLocation[attr]"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      regions.add(w);

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("attr");
      cfe.extractFeatures(regions);

      assertTrue(w.hasAttribute("min[attr]"));
      assertTrue(w.hasAttribute("max[attr]"));
      assertTrue(w.hasAttribute("mean[attr]"));
      assertTrue(w.hasAttribute("stdev[attr]"));
      assertTrue(w.hasAttribute("zMax[attr]"));
      assertTrue(w.hasAttribute("maxLocation[attr]"));
      assertTrue(w.hasAttribute("maxRelLocation[attr]"));
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 2, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      regions.add(w);

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("attr");
      cfe.extractFeatures(regions);

      assertEquals(0.1, (Double) w.getAttribute("min[attr]"), 0.001);
      assertEquals(0.4, (Double) w.getAttribute("max[attr]"), 0.001);
      assertEquals(0.2166, (Double) w.getAttribute("mean[attr]"), 0.001);
      assertEquals(0.1169, (Double) w.getAttribute("stdev[attr]"), 0.001);
      assertEquals(1.5682, (Double) w.getAttribute("zMax[attr]"), 0.001);
      assertEquals(0.04, (Double) w.getAttribute("maxLocation[attr]"), 0.001);
      assertEquals(0.02, (Double) w.getAttribute("maxRelLocation[attr]"), 0.001);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeaturesWithZeroStdev() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 2, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1}));
      regions.add(w);

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("attr");
      cfe.extractFeatures(regions);

      assertEquals(0.1, (Double) w.getAttribute("min[attr]"), 0.001);
      assertEquals(0.1, (Double) w.getAttribute("max[attr]"), 0.001);
      assertEquals(0.1, (Double) w.getAttribute("mean[attr]"), 0.001);
      assertEquals(0.0, (Double) w.getAttribute("stdev[attr]"), 0.001);
      assertEquals(0.0, (Double) w.getAttribute("zMax[attr]"), 0.001);
      assertEquals(0.0, (Double) w.getAttribute("maxLocation[attr]"), 0.001);
      assertEquals(0.0, (Double) w.getAttribute("maxRelLocation[attr]"), 0.001);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesCorrectlyExtractsFeaturesFromSubFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 0.035, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      regions.add(w);

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("attr");
      cfe.extractFeatures(regions);

      assertEquals(0.1, (Double) w.getAttribute("min[attr]"), 0.001);
      assertEquals(0.3, (Double) w.getAttribute("max[attr]"), 0.001);
      assertEquals(0.2, (Double) w.getAttribute("mean[attr]"), 0.001);
      assertEquals(0.08164965809277258, (Double) w.getAttribute("stdev[attr]"), 0.001);
      assertEquals(1.2247448713915892, (Double) w.getAttribute("zMax[attr]"), 0.001);
      assertEquals(0.02, (Double) w.getAttribute("maxLocation[attr]"), 0.001);
      assertEquals(0.5714, (Double) w.getAttribute("maxRelLocation[attr]"), 0.001);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
