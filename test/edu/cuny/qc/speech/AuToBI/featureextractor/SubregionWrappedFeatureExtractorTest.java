/*  SubregionWrappedFeatureExtractorTest.java

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
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SubregionWrappedFeatureExtractor
 *
 * @see SubregionWrappedFeatureExtractor
 */
@SuppressWarnings("unchecked")
public class SubregionWrappedFeatureExtractorTest {

  private SubregionWrappedFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() throws Exception {
    regions = new ArrayList<Region>();
    FeatureExtractor sub_fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("attribute", "value");
        }
      }
    };
    sub_fe.getExtractedFeatures().add("attribute");
    fe = new SubregionWrappedFeatureExtractor(sub_fe, "subregion");
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("attribute_subregion"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("subregion"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectlyFromWrappedFeatureExtractor() {
    FeatureExtractor sub_fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("attribute", "value");
        }
      }
    };
    sub_fe.getRequiredFeatures().add("sub_req");

    fe = new SubregionWrappedFeatureExtractor(sub_fe, "subregion");
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("subregion"));
    assertTrue(fe.getRequiredFeatures().contains("sub_req"));
  }

  @Test
  public void testExtractFeatureExtractsFeatures() {
    Word w = new Word(0, 1, "test_word");
    Region r = new Region(0.5, 0.75, "test_subregion");
    w.setAttribute("subregion", r);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("attribute_subregion"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test_word");
    Region r = new Region(0.5, 0.75, "test_subregion");
    w.setAttribute("subregion", r);
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals("value", w.getAttribute("attribute_subregion"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureThrowsAnExceptionWhenSubregionIsUnavailable() {
    Word w = new Word(0, 1, "test_word");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      fail();
    } catch (FeatureExtractorException e) {
    }
  }

  @Test
  public void testExtractFeatureCopiesRequiredFeatureToSubregion() {
    FeatureExtractor sub_fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("attribute", "value");
        }
      }
    };
    sub_fe.getRequiredFeatures().add("sub_req");
    fe = new SubregionWrappedFeatureExtractor(sub_fe, "subregion");

    Word w = new Word(0, 1, "test_word");
    Region r = new Region(0.5, 0.75, "test_subregion");
    w.setAttribute("subregion", r);
    w.setAttribute("sub_req", "copied feature");
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("sub_req"));
      assertEquals("copied feature", r.getAttribute("sub_req"));
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testExtractFeatureCopiesRequiredContourFeatureToSubregion() {
    FeatureExtractor sub_fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("attribute", "value");
        }
      }
    };
    sub_fe.getRequiredFeatures().add("sub_req");
    fe = new SubregionWrappedFeatureExtractor(sub_fe, "subregion");

    Word w = new Word(0, 1, "test_word");
    Region r = new Region(0.5, 0.75, "test_subregion");
    w.setAttribute("subregion", r);
    w.setAttribute("sub_req", new Contour(0.4, 0.1, new double[]{1, 2, 3, 4, 5, 6}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("sub_req"));
      Contour c = (Contour) r.getAttribute("sub_req");
      assertEquals(6, c.size());
      assertEquals(0.4, c.getStart());
    } catch (FeatureExtractorException e) {
      e.printStackTrace();
    }
  }
}
