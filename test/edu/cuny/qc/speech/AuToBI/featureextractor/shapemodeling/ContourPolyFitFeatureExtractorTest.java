/*  QCMFeatureExtractorTest.java

    Copyright 2009-2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


/**
 * A Test class for Quantized contour models.
 *
 * @see QuantizedContourModel
 */
public class ContourPolyFitFeatureExtractorTest {
  private ContourPolyFitFeatureExtractor fe;
  private List<Region> regions;


  @Before
  public void setUp() {
    regions = new ArrayList<Region>();

    fe = new ContourPolyFitFeatureExtractor("2", "f0");
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    Assert.assertEquals(4, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("fit[f0,2,0]"));
    assertTrue(fe.getExtractedFeatures().contains("fit[f0,2,1]"));
    assertTrue(fe.getExtractedFeatures().contains("fit[f0,2,2]"));
    assertTrue(fe.getExtractedFeatures().contains("fitMSE[f0,2]"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    Assert.assertEquals(1, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("f0"));
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("f0", new Contour(0.0, 0.001, new double[]{0.3, 0.3, 0.3, 1.4, 0.2, 0.1}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertTrue(w.hasAttribute("fit[f0,2,0]"));
      assertTrue(w.hasAttribute("fit[f0,2,1]"));
      assertTrue(w.hasAttribute("fit[f0,2,2]"));
      assertTrue(w.hasAttribute("fitMSE[f0,2]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesExtractsFeaturesCorrectly() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("f0", new Contour(0.0, 0.1, new double[]{0.3, 0.3, 0.3, 1.4, 0.2, 0.1}));
    regions.add(w);

    try {
      fe.extractFeatures(regions);
      assertEquals(0.1321, (Double) w.getAttribute("fit[f0,2,0]"), 0.0001);
      assertEquals(4.675, (Double) w.getAttribute("fit[f0,2,1]"), 0.0001);
      assertEquals(-9.4643, (Double) w.getAttribute("fit[f0,2,2]"), 0.0001);
      assertEquals(0.81835, (Double) w.getAttribute("fitMSE[f0,2]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
