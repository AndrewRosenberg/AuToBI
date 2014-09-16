/*  ClassWeightedWekaClassifier.java

    Copyright 2012-2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.util.ClassifierUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * ClassWeightedWekaClassifier is an AuToBI wrapper around a weka classifier.
 * <p/>
 * Each training instance is importance weighted by the value of its class attribute.
 * <p/>
 * The importance weight function is defined by a WeightFunction object.
 */
public class ClassWeightedWekaClassifier extends WekaClassifier {

  private static final long serialVersionUID = 20120303L;

  /**
   * Constructs a new ClassWeightedWekaClassifier given a weka Classfiier object.
   *
   * @param classifier the weka Classifier.
   */
  public ClassWeightedWekaClassifier(Classifier classifier) {
    super(classifier);
  }

  /**
   * Trains the weka classifier based on training data supplied by a FeatureSet.
   *
   * @param feature_set The training data
   * @throws Exception if weka has a training problem.
   */
  public void train(FeatureSet feature_set) throws Exception {
    setFeatures(feature_set.getFeatures());
    class_attribute = feature_set.getClassAttribute();

    ClassBasedWeightFunctionTrainer trainer =
        new ClassBasedWeightFunctionTrainer(class_attribute, ClassBasedWeightFunctionTrainer.WeightType.LINEAR);
    ClassBasedWeightFunction fn = trainer.trainWeightFunction(feature_set.getDataPoints());

    Instances weka_instances = ClassifierUtils.convertFeatureSetToWeightedWekaInstances(feature_set, fn);
    weka_classifier.buildClassifier(weka_instances);
  }
}
