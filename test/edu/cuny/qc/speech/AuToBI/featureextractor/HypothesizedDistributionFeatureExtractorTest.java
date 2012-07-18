/*  HypothesizedDistributionFeatureExtractorTest.java

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
import edu.cuny.qc.speech.AuToBI.core.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for HypothesizedDistributionFeatureExtractor
 *
 * @see HypothesizedDistributionFeatureExtractor
 */
public class HypothesizedDistributionFeatureExtractorTest {

  @Test
  public void testConstructorSetsExtractedFeaturesCorrectly() {
    HypothesizedDistributionFeatureExtractor fe =
        new HypothesizedDistributionFeatureExtractor("feature", null, new FeatureSet());

    assertEquals(1, fe.getExtractedFeatures().size());
    assertTrue(fe.getExtractedFeatures().contains("feature"));
  }

  @Test
  public void testConstructorSetsRequiredFeaturesCorrectly() {

    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("required_one");
    fs.insertRequiredFeature("required_two");

    HypothesizedDistributionFeatureExtractor fe =
        new HypothesizedDistributionFeatureExtractor("feature", null, fs);

    assertEquals(2, fe.getRequiredFeatures().size());
    assertTrue(fe.getRequiredFeatures().contains("required_one"));
    assertTrue(fe.getRequiredFeatures().contains("required_two"));

  }

  @Test
  public void testConstructorExtractsFeaturesWithMockConstructor() {

    AuToBIClassifier c = new AuToBIClassifier() {

      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("POSITIVE", .75);
        d.add("NEGATIVE", .25);
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

    Word r = new Word(0.0, 1.0, "test_word");
    List<Region> regions = new ArrayList<Region>();
    regions.add(r);

    HypothesizedDistributionFeatureExtractor fe =
        new HypothesizedDistributionFeatureExtractor("feature", c, new FeatureSet());

    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("feature"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testConstructorExtractsFeaturesCorrectlyWithMockConstructor() {

    AuToBIClassifier c = new AuToBIClassifier() {

      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("POSITIVE", .75);
        d.add("NEGATIVE", .25);
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

    Word r = new Word(0.0, 1.0, "test_word");
    List<Region> regions = new ArrayList<Region>();
    regions.add(r);

    HypothesizedDistributionFeatureExtractor fe =
        new HypothesizedDistributionFeatureExtractor("feature", c, new FeatureSet());

    try {
      fe.extractFeatures(regions);
      Distribution d = (Distribution) r.getAttribute("feature");
      assertEquals(.75, d.get("POSITIVE"), 0.0001);
      assertEquals(.25, d.get("NEGATIVE"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

}
