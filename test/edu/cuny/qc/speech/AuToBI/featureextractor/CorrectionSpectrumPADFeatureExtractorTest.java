/*  CorrectionSpectrumPADFeatureExtractorTest.java

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

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
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
 * Test class for TiltFeatureExtractor
 *
 * @see edu.cuny.qc.speech.AuToBI.featureextractor.TiltFeatureExtractor
 */
@SuppressWarnings("unchecked")
public class CorrectionSpectrumPADFeatureExtractorTest {
  private CorrectionSpectrumPADFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("attr");
    fs.setClassAttribute("test_class");
    AuToBIClassifier classifier = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("CORRECT");
        d.add("CORRECT");
        d.add("INCORRECT");
        d.normalize();
        return d;
      }

      @Override
      public void train(FeatureSet feature_set) throws Exception {

      }

      @Override
      public AuToBIClassifier newInstance() {
        return null;
      }
    };
    fe = new CorrectionSpectrumPADFeatureExtractor(0, 1, classifier, fs);
  }

  @Test
  public void testSetsExtractedFeaturesCorrectly() {
    assertEquals(2, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("nominal_bark_0_1__correction_prediction"));
    assertTrue(fe.getExtractedFeatures().contains("bark_0_1__correction_prediction_confidence"));
  }

  @Test
  public void testSetsRequiredFeaturesCorrectly() {
    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("attr"));
    assertTrue(fe.getRequiredFeatures().contains("test_class"));
  }

  @Test
  public void testExtractFeatureExtractsFeatures() {
    Word w = new Word(0, 1, "test");
    w.setAttribute("attr", 1);
    w.setAttribute("test_class", "ACCENTED");
    regions.add(w);

    Word w2 = new Word(0, 1, "test");
    w2.setAttribute("attr", 2);
    w2.setAttribute("test_class", "DEACCENTED");
    regions.add(w2);

    Word w3 = new Word(0, 1, "test");
    w3.setAttribute("attr", 2);
    w3.setAttribute("test_class", "DEACCENTED");
    regions.add(w3);

    Word w4 = new Word(0, 1, "test");
    w4.setAttribute("attr", 1);
    w4.setAttribute("test_class", "ACCENTED");
    regions.add(w4);

    try {
      fe.extractFeatures(regions);
      for (Region word : regions) {
        assertTrue(word.hasAttribute("nominal_bark_0_1__correction_prediction"));
        assertTrue(word.hasAttribute("bark_0_1__correction_prediction_confidence"));
      }
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testExtractFeatureExtractsFeaturesCorrectly() {
    // Add some mocked out words to train a cross-validated classifier.
    // This test is meaningless with too few data points.
    for (int i = 0; i < 10; i++) {
      Word w = new Word(0, 1, "test - accented");
      w.setAttribute("attr", 1);
      w.setAttribute("test_class", "ACCENTED");
      regions.add(w);
    }
    for (int i = 0; i < 10; i++) {
      Word w2 = new Word(0, 1, "test - deaccented");
      w2.setAttribute("attr", 2);
      w2.setAttribute("test_class", "DEACCENTED");
      regions.add(w2);
    }

    try {
      fe.extractFeatures(regions);
      for (Region word : regions) {
        // In this test case the classifier should be able to correctly predict each test label
        assertEquals("CORRECT", word.getAttribute("nominal_bark_0_1__correction_prediction"));
        assertEquals(0.6666, (Double) word.getAttribute("bark_0_1__correction_prediction_confidence"), 0.0001);
      }
    } catch (FeatureExtractorException e) {
      fail(e.getMessage());
    }
  }
}
