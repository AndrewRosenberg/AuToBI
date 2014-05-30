/*  DeltaContourFeatureExtractorTest.java

    Copyright 2012-2014 Andrew Rosenberg

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for DifferenceFeatureExtractor
 *
 * @see DifferenceFeatureExtractor
 */
public class DifferenceFeatureExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      regions.add(new Word(0, 1, "test"));

      DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");
      fe.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesFailsWithNonNumericFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("attr", "string attribute");
      regions.add(w);

      Word w1 = new Word(0, 1, "test");
      w1.setAttribute("attr", "string attribute");
      regions.add(w1);

      DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");
      fe.extractFeatures(regions);
      fail();
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testSetAttributeNameCorrectlySetsExtractedFeatures() {
    DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");

    List<String> extracted_features = fe.getExtractedFeatures();

    assertTrue(extracted_features.contains("diff[attr]"));
  }

  @Test
  public void testSetAttributeNameCorrectlySetsRequiredFeatures() {
    DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");

    Set<String> required_features = fe.getRequiredFeatures();

    assertTrue(required_features.contains("attr"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("attr", 0.6);
      regions.add(w);

      Word w1 = new Word(1, 2, "test");
      w1.setAttribute("attr", 0.9);
      regions.add(w1);

      DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");
      fe.extractFeatures(regions);

      assertTrue(w.hasAttribute("diff[attr]"));
      assertFalse(w1.hasAttribute("diff[attr]"));
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
      w.setAttribute("attr", 0.6);
      regions.add(w);

      Word w1 = new Word(1, 2, "test");
      w1.setAttribute("attr", 0.9);
      regions.add(w1);

      DifferenceFeatureExtractor fe = new DifferenceFeatureExtractor("attr");
      fe.extractFeatures(regions);

      Double diff = (Double) w.getAttribute("diff[attr]");

      assertEquals(0.3, diff, 0.0001);

    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
