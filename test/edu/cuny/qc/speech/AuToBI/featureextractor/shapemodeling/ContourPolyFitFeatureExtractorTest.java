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

    ContourPolynomialFitter fitter = new ContourPolynomialFitter(2);
    fe = new ContourPolyFitFeatureExtractor(fitter, "polyfit", "f0");
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    Assert.assertEquals(4, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("polyfit_0"));
    assertTrue(fe.getExtractedFeatures().contains("polyfit_1"));
    assertTrue(fe.getExtractedFeatures().contains("polyfit_2"));
    assertTrue(fe.getExtractedFeatures().contains("polyfit_mse"));
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
      assertTrue(w.hasAttribute("polyfit_0"));
      assertTrue(w.hasAttribute("polyfit_1"));
      assertTrue(w.hasAttribute("polyfit_2"));
      assertTrue(w.hasAttribute("polyfit_mse"));
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
      assertEquals(0.1321, (Double) w.getAttribute("polyfit_0"), 0.0001);
      assertEquals(4.675, (Double) w.getAttribute("polyfit_1"), 0.0001);
      assertEquals(-9.4643, (Double) w.getAttribute("polyfit_2"), 0.0001);
      assertEquals(0.81835, (Double) w.getAttribute("polyfit_mse"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
