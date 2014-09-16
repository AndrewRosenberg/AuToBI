/*  WekaClassifier.java

    Copyright 2009-2014 Andrew Rosenberg

  This file is part of the AuToBI prosodic analysis package.

  AuToBI is free software: you can redistribute it and/or modify
  it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Feature;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Set;

/**
 * WekaClassifier is an AuToBI wrapper around a weka classifier.
 */
public class WekaClassifier extends AuToBIClassifier {

  private static final long serialVersionUID = 1633315748323749801L;

  protected Classifier weka_classifier; // the weka classifier
  // Stored features are necessary for classifying a single data point.
  protected Set<Feature> features;
  protected String class_attribute;

  /**
   * Constructs a new WekaClassifier given a weka Classfiier object.
   *
   * @param classifier the weka Classifier.
   */
  public WekaClassifier(Classifier classifier) {
    this.weka_classifier = classifier;
    this.features = null;
    this.class_attribute = "";
  }

  /**
   * Evaluates the weka classifier on a single point.
   *
   * @param testing_point The point to evaluate
   * @return a distribution of the hypotheses.
   * @throws Exception
   */
  public Distribution distributionForInstance(Word testing_point) throws Exception {
    Instance test_instance = ClassifierUtils.convertWordToInstance(testing_point, features, class_attribute);

    double[] distribution = weka_classifier.distributionForInstance(test_instance);

    Distribution d = new Distribution();
    for (int i = 0; i < test_instance.classAttribute().numValues(); ++i) {
      d.put(test_instance.classAttribute().value(i), distribution[i]);
    }
    return d;
  }

  /**
   * Trains the weka classifier based on training data supplied by a FeatureSet.
   *
   * @param feature_set The training data
   * @throws Exception if weka has a training problem.
   */
  public void train(FeatureSet feature_set) throws Exception {
    if (feature_set.getFeatures() == null || feature_set.getFeatures().size() == 0) {
      feature_set.constructFeatures();
    }
    setFeatures(feature_set.getFeatures());
    class_attribute = feature_set.getClassAttribute();

    Instances weka_instances = ClassifierUtils.convertFeatureSetToWekaInstances(feature_set);
    weka_classifier.buildClassifier(weka_instances);
  }

  /**
   * Constructs a copy of the object.
   *
   * @return a copy of the object.
   */
  public AuToBIClassifier newInstance() {
    try {
      return new WekaClassifier(AbstractClassifier.makeCopy(weka_classifier));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Sets the features used by the classifier
   *
   * @param features the features.
   */
  public void setFeatures(Set<Feature> features) {
    this.features = features;
  }

  /**
   * Constructs a string representation of the classifier.
   *
   * @return the string description of the classifier.
   */
  public String toString() {
    return weka_classifier.toString();
  }
}
