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
import edu.cuny.qc.speech.AuToBI.core.*;
import org.junit.Test;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 1/4/11 Time: 10:30 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassifierUtilsTest {

  private final String TEST_DIR = "/Users/andrew/code/AuToBI/release/test_data";

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
  public void ReadClassifierNotAClassifier() {
    String filename = TEST_DIR + "/sineWithNoise.wav";

    AuToBIClassifier c = ClassifierUtils.readAuToBIClassifier(filename);

    assertNull(c);
  }

  @Test
  public void testConvertWordToInstance() {
    Word w = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getRequiredFeatures().add("featureA");
    fs.setClassAttribute("class_attribute");

    w.setAttribute("featureA", 3);
    w.setAttribute("class_attribute", "POSITIVE");

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
      FastVector fv = ClassifierUtils.generateWekaAttributes(s);

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
      FastVector fv = ClassifierUtils.generateWekaAttributes(s);

      assertEquals(1, fv.size());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testConvertFeatureSetToWekaInstances() {
    Word w = new Word(0, 1, "one");
    FeatureSet fs = new FeatureSet();
    fs.getRequiredFeatures().add("featureA");
    fs.getRequiredFeatures().add("featureB");
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

}
