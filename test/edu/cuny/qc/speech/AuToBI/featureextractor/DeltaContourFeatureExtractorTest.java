/*  DeltaContourFeatureExtractorTest.java

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
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for DeltaContourFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.DeltaContourFeatureExtractor
 */
public class DeltaContourFeatureExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      regions.add(new Word(0, 1, "test"));

      DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("test_attribute");
      fe.extractFeatures(regions);
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

      DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("test_attribute");
      fe.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testSetAttributeNameCorrectlySetsExtractedFeatures() {
    DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("attr");

    fe.setAttributeName("attr");

    List<String> features = fe.getExtractedFeatures();

    assertTrue(features.contains("delta_attr"));
  }

  @Test
  public void testSetAttributeNameCorrectlySetsRequiredFeatures() {
    DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("attr");

    fe.setAttributeName("attr");

    Set<String> features = fe.getRequiredFeatures();

    assertTrue(features.contains("attr"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      regions.add(w);

      DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("attr");
      fe.extractFeatures(regions);

      assertTrue(w.hasAttribute("delta_attr"));
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
      Word w = new Word(0, 1, "test");
      w.setAttribute("attr", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      regions.add(w);

      DeltaContourFeatureExtractor fe = new DeltaContourFeatureExtractor("attr");
      fe.extractFeatures(regions);

      Contour delta_c = (Contour) w.getAttribute("delta_attr");

      assertEquals(5, delta_c.size());
      assertEquals(0.005, delta_c.getStart(), 0.0001);
      assertEquals(0.01, delta_c.getStep(), 0.0001);
      assertEquals(0.1 / 0.01, delta_c.get(0), 0.0001);
      assertEquals(0.1 / 0.01, delta_c.get(1), 0.0001);
      assertEquals(-0.1 / 0.01, delta_c.get(2), 0.0001);
      assertEquals(0.2 / 0.01, delta_c.get(3), 0.0001);
      assertEquals(-0.3 / 0.01, delta_c.get(4), 0.0001);

    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
