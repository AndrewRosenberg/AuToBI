/*  ResetContourFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.core.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test class for ResetContourFeatureExtractor
 *
 * @see ResetContourFeatureExtractor
 */
public class ResetContourFeatureExtractorTest {
  private ResetContourFeatureExtractor fe;
  private List<Region> regions;
  private String contour_feature;
  private String subregion_feature;


  @Before
  public void setUp() throws Exception {
    contour_feature = "contour";
    subregion_feature = "subregion";
    regions = new ArrayList<Region>();
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectlyWithNullSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature);
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("reset[" + contour_feature + "]"));
  }

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectlyWithSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature, subregion_feature);
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("reset[" + contour_feature + "," + subregion_feature + "]"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectlyWithNoSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature);
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains(contour_feature));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectlyWithSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature, subregion_feature);
    assertEquals(3, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains(contour_feature));
    assertTrue(fe.getRequiredFeatures().contains("van_" + subregion_feature));
    assertTrue(fe.getRequiredFeatures().contains("trail_" + subregion_feature));
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesWithNoSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature);

    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{0, 1, 2, 3, 4}));
    Word w2 = new Word(1, 2, "test2");
    w2.setAttribute("contour", new Contour(1, 0.1, new double[]{10, 11, 12, 13, 14}));
    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("reset[contour]"));
      assertFalse(w2.hasAttribute("reset[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectlyWithNoSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature);

    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{0, 1, 2, 3, 4}));
    Word w2 = new Word(1, 2, "test2");
    w2.setAttribute("contour", new Contour(1, 0.1, new double[]{10, 11, 12, 13, 14}));
    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertEquals(10.0, (Double) w.getAttribute("reset[contour]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesWithSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature, subregion_feature);

    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    w.setAttribute("van_" + subregion_feature, new Region(0.6, 1));
    w.setAttribute("trail_" + subregion_feature, new Region(0, 0.6));
    Word w2 = new Word(1, 2, "test2");
    w2.setAttribute("contour", new Contour(1, 0.1, new double[]{10, 11, 12, 13, 14}));
    w2.setAttribute("trail_" + subregion_feature, new Region(1, 1.4));
    w2.setAttribute("van_" + subregion_feature, new Region(1.4, 2.0));
    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("reset[contour,subregion]"));
      assertFalse(w2.hasAttribute("reset[contour,subregion]"));
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectlyWithSubregion() {
    fe = new ResetContourFeatureExtractor(contour_feature, subregion_feature);

    Word w = new Word(0, 1, "test");
    w.setAttribute("contour", new Contour(0, 0.1, new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    w.setAttribute("van_" + subregion_feature, new Region(0.6, 1));
    w.setAttribute("trail_" + subregion_feature, new Region(0, 0.6));
    Word w2 = new Word(1, 2, "test2");
    w2.setAttribute("contour", new Contour(1, 0.1, new double[]{10, 11, 12, 13, 14}));
    w2.setAttribute("trail_" + subregion_feature, new Region(1, 1.4));
    w2.setAttribute("van_" + subregion_feature, new Region(1.4, 2.0));
    regions.add(w);
    regions.add(w2);

    try {
      fe.extractFeatures(regions);
      assertEquals(3.5, (Double) w.getAttribute("reset[contour,subregion]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }
}
