/*  ClassifierUtilsTest.java

    Copyright 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.classifier.MockClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 1/4/11 Time: 10:30 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassifierUtilsTest {

  private static final String TEST_DIR = System.getenv().get("AUTOBI_TEST_DIR");

  @Test
  public void testReadClassifierNoFile() {
    String filename = "THIS/IS/NOT/A/FILE";

    AuToBIClassifier c = ClassifierUtils.readAuToBIClassifier(filename);

    assertNull(c);
  }

  @Test
  public void ReadClassifier() {
    String filename = TEST_DIR + "/test.model";

    AuToBIClassifier c = ClassifierUtils.readAuToBIClassifier(filename);

    assertNotNull(c);
  }

  @Test
  public void testWriteClassifierExecutesWithoutError() {
    String filename = TEST_DIR + "/testwrite.model";

    AuToBIClassifier c = new MockClassifier();

    try {
      ClassifierUtils.writeAuToBIClassifier(filename, c);
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void ReadClassifierNotAClassifier() {
    String filename = TEST_DIR + "/sineWithNoise.wav";

    AuToBIClassifier c = ClassifierUtils.readAuToBIClassifier(filename);

    assertNull(c);
  }

  @Test
  public void testConvertWordToInstance() {
    Word w = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("featureA");
    fs.setClassAttribute("class_attribute");

    w.setAttribute("featureA", 3);
    w.setAttribute("class_attribute", "POSITIVE");
    fs.getDataPoints().add(w);

    fs.constructFeatures();

    try {
      Instance instance = ClassifierUtils.convertWordToInstance(w, fs);

      assertEquals(3, instance.value(0), 0.01);
      assertEquals(0, instance.classValue(), 0.01);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testGenerateWekaAttributesIsCorrectForStringFeatures() {
    Set<Feature> s = new HashSet<Feature>();

    Feature f1 = new Feature("string feature");
    f1.setString(true);
    s.add(f1);

    try {
      ArrayList<Attribute> fv = ClassifierUtils.generateWekaAttributes(s);

      assertEquals(1, fv.size());
    } catch (Exception e) {
      fail();
    }
  }


  @Test
  public void testGenerateWekaAttributesIsCorrectForNominalFeatures() {
    Set<Feature> s = new HashSet<Feature>();

    Feature f1 = new Feature("nominal feature");
    f1.setNominalValues(new String[]{"one", "two"});
    s.add(f1);

    try {
      ArrayList<Attribute> fv = ClassifierUtils.generateWekaAttributes(s);

      assertEquals(1, fv.size());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testConvertFeatureSetToWekaInstances() {
    Word w = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("featureA");
    fs.insertRequiredFeature("featureB");
    fs.setClassAttribute("class_attribute");

    w.setAttribute("featureA", 3);
    w.setAttribute("featureB", "three");
    w.setAttribute("class_attribute", "POSITIVE");

    fs.insertDataPoint(w);
    fs.constructFeatures();
    fs.getFeature("featureB").setString(true);

    try {
      Instances instances = ClassifierUtils.convertFeatureSetToWekaInstances(fs);

      assertEquals(3, instances.numAttributes());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testGenerateEvaluationResults() {
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "one");

    w1.setAttribute("true", "A");
    w1.setAttribute("hyp", "A");

    w2.setAttribute("true", "B");
    w2.setAttribute("hyp", "A");


    FeatureSet fs = new FeatureSet();

    fs.insertDataPoint(w1);
    fs.insertDataPoint(w2);

    try {
      EvaluationResults eval = ClassifierUtils.generateEvaluationResults("hyp", "true", fs);

      assertEquals(2, eval.getNumClasses());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testGenerateEvaluationSummary() {
    Word w1 = new Word(0, 1, "one");
    Word w2 = new Word(0, 1, "one");

    w1.setAttribute("true", "A");
    w1.setAttribute("hyp", "A");

    w2.setAttribute("true", "B");
    w2.setAttribute("hyp", "A");


    FeatureSet fs = new FeatureSet();

    fs.insertDataPoint(w1);
    fs.insertDataPoint(w2);

    try {
      String s = ClassifierUtils.evaluateClassification("hyp", "true", fs);

      assertNotNull(s);
      assertTrue(s.length() > 0);

    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testGeneratePredictions() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("one", 0.51);
        d.add("two", 0.49);
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

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictions(c, "hyp", "default", fs);

    assertTrue(w1.hasAttribute("hyp"));
    assertEquals("one", w1.getAttribute("hyp"));
  }

  @Test
  public void testGeneratePredictionsAssignsDefaultOnClassifierException() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        throw new AuToBIException("testing");
      }

      @Override
      public void train(FeatureSet feature_set) throws Exception {
      }

      @Override
      public AuToBIClassifier newInstance() {
        return null;
      }
    };

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictions(c, "hyp", "default", fs);

    assertTrue(w1.hasAttribute("hyp"));
    assertEquals("default", w1.getAttribute("hyp"));
  }


  @Test
  public void testGeneratePredictionsWithConfidence() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("one", 0.51);
        d.add("two", 0.49);
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

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictionsWithConfidenceScores(c, "hyp", "conf", "default", fs);

    assertTrue(w1.hasAttribute("hyp"));
    assertEquals("one", w1.getAttribute("hyp"));
    assertTrue(w1.hasAttribute("conf"));
    assertEquals(0.51, w1.getAttribute("conf"));
  }

  @Test
  public void testGeneratePredictionsWithConfidenceAssignsDefaultOnException() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        throw new AuToBIException("testing");
      }

      @Override
      public void train(FeatureSet feature_set) throws Exception {
      }

      @Override
      public AuToBIClassifier newInstance() {
        return null;
      }
    };

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictionsWithConfidenceScores(c, "hyp", "conf", "default", fs);

    assertTrue(w1.hasAttribute("hyp"));
    assertEquals("default", w1.getAttribute("hyp"));
    assertTrue(w1.hasAttribute("conf"));
    assertEquals(0.5, w1.getAttribute("conf"));
  }

  @Test
  public void testGeneratePredictionDistribution() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        Distribution d = new Distribution();
        d.add("one", 0.51);
        d.add("two", 0.49);
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

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictionDistribution(c, "dist", "default", fs);

    assertTrue(w1.hasAttribute("dist"));
    assertEquals(0.51, ((Distribution) w1.getAttribute("dist")).get("one"), 0.01);
  }

  @Test
  public void testGeneratePredictionDistributionAssignsDefaultOnClassifierException() {
    AuToBIClassifier c = new AuToBIClassifier() {
      @Override
      public Distribution distributionForInstance(Word testing_point) throws Exception {
        throw new AuToBIException("testing");
      }

      @Override
      public void train(FeatureSet feature_set) throws Exception {
      }

      @Override
      public AuToBIClassifier newInstance() {
        return null;
      }
    };

    Word w1 = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(w1);

    ClassifierUtils.generatePredictionDistribution(c, "dist", "default", fs);

    assertTrue(w1.hasAttribute("dist"));
    assertEquals("default", w1.getAttribute("dist"));
  }

  @Test
  public void testGenerateEvaluationResultsFailsGracefullyWithNoTrueFeature() {

    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "test");
    w.setAttribute("hyp_feature", "PREDICTED_VALUE");
    fs.insertDataPoint(w);

    try {
      EvaluationResults eval = ClassifierUtils.generateEvaluationResults("hyp_feature", "true_feature", fs);
      assertNotNull(eval);
    } catch (AuToBIException e) {
      fail();
    } catch (NullPointerException e) {
      fail();
    }
  }

  @Test
  public void testGenerateEvaluationResultsFailsGracefullyWithNoHypFeature() {

    FeatureSet fs = new FeatureSet();
    Word w = new Word(0, 1, "test");
    w.setAttribute("true_feature", "PREDICTED_VALUE");
    fs.insertDataPoint(w);

    try {
      EvaluationResults eval = ClassifierUtils.generateEvaluationResults("hyp_feature", "true_feature", fs);
      assertNotNull(eval);
    } catch (AuToBIException e) {
      fail();
    } catch (NullPointerException e) {
      fail();
    }
  }
}
