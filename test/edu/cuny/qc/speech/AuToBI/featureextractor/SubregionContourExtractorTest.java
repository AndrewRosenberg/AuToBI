/*  SubregionContourExtractorTest.java

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
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SubregionContourFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.SubregionContourExtractor
 */
public class SubregionContourExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      regions.add(new Word(0, 1, "test"));

      SubregionContourExtractor sce = new SubregionContourExtractor("test_contour", "test_attribute");
      sce.extractFeatures(regions);
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
      w.setAttribute("test_contour", new Contour(0.0, 1.0, new double[]{}));
      w.setAttribute("test_region", new Region(0.4, 0.6, "subregion"));
      regions.add(w);

      SubregionContourExtractor sce = new SubregionContourExtractor("test_contour", "test_region");
      sce.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesWorksWithNullRegion() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("test_contour", new Contour(0.0, 1.0, new double[]{}));
      w.setAttribute("test_region", null);
      regions.add(w);

      SubregionContourExtractor sce = new SubregionContourExtractor("test_contour", "test_region");
      sce.extractFeatures(regions);

      assertFalse(w.hasAttribute("subregionC[test_contour,test_region]"));
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testSetAttributeNameCorrectlySetsExtractedFeatures() {
    SubregionContourExtractor sce = new SubregionContourExtractor("test_contour", "test_region");
    List<String> features = sce.getExtractedFeatures();

    assertTrue(features.contains("subregionC[test_contour,test_region]"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    try {
      List<Region> regions = new ArrayList<Region>();
      Word w = new Word(0, 1, "test");
      w.setAttribute("contour", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      w.setAttribute("region", new Region(0.015, 0.035));
      regions.add(w);

      SubregionContourExtractor sce = new SubregionContourExtractor("contour", "region");
      sce.extractFeatures(regions);

      assertTrue(w.hasAttribute("subregionC[contour,region]"));
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
      w.setAttribute("contour", new Contour(0.0, 0.01, new double[]{0.1, 0.2, 0.3, 0.2, 0.4, 0.1}));
      w.setAttribute("region", new Region(0.015, 0.035));
      regions.add(w);

      SubregionContourExtractor sce = new SubregionContourExtractor("contour", "region");
      sce.extractFeatures(regions);

      assertEquals(2, ((Contour) w.getAttribute("subregionC[contour,region]")).size());
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
