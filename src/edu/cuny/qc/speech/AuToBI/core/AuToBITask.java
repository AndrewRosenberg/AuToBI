/*  AuToBITask.java

    Copyright (c) 2009-2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;

/**
 * AuToBITask is a wrapper object to hold information about AuToBI classification tasks.
 * <p/>
 * This information includes, the feature name that will store the prediction, the gold standard label, a trained
 * classifier, and an empty associated featureset describing the features required to execute this task.
 */
public class AuToBITask {

  private String trueFeature;           // The feature name to store the gold standard label
  private String hypFeature;            // The feature name to store the hypothesized label
  private String confFeature;           // A feature to store the confidence score in
  private String distFeature;           // A feature to store a distribution of confidence scores in
  private AuToBIClassifier classifier;  // A classifier associated with this task
  private String defaultValue;          // A default value for the classification
  private FeatureSet featureSet;
  // An empty feature set describing the feature structure for this classification task


  /**
   * Get the feature name holding the gold standard label
   *
   * @return the gold standard feature name
   */
  public String getTrueFeature() {
    return trueFeature;
  }

  /**
   * Sets the feature name holding the gold standard label
   *
   * @param true_feature the feature name
   */
  public void setTrueFeature(String true_feature) {
    trueFeature = true_feature;
  }


  /**
   * Gets the feature name holding the hypothesized feature.
   *
   * @return the hypothesized feature name
   */
  public String getHypFeature() {
    return hypFeature;
  }

  /**
   * Sets the feature name holding the hypothesized feature.
   *
   * @param hyp_feature the feature name
   */
  public void setHypFeature(String hyp_feature) {
    hypFeature = hyp_feature;
  }

  /**
   * Gets the feature name which holds the confidence score for the prediction.
   *
   * @return the confidence feature name
   */
  public String getConfFeature() {
    return confFeature;
  }

  /**
   * Sets the feature name which holds the confidence score for the prediction.
   *
   * @param conf_feature the feature name
   */
  public void setConfFeature(String conf_feature) {
    confFeature = conf_feature;
  }

  /**
   * Gets the feature name which holds the confidence score distribution for the prediction.
   *
   * @return the distribution feature name
   */
  public String getDistFeature() {
    return distFeature;
  }

  /**
   * Sets the feature name which holds the confidence score distribution for the prediction.
   *
   * @param dist_feature the feature name
   */
  public void setDistFeature(String dist_feature) {
    distFeature = dist_feature;
  }

  /**
   * Gets the classifier associated with this task.
   *
   * @return an appropriate classifier.
   */
  public AuToBIClassifier getClassifier() {
    return classifier;
  }

  /**
   * Sets the classifier associated with this task.
   *
   * @param classifier the classifier
   */
  public void setClassifier(AuToBIClassifier classifier) {
    this.classifier = classifier;
  }

  /**
   * Gets the default value for this classification task, to assign if somethign goes wrong.
   *
   * @return the default value
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Gets an empty feature set to describe the class feature and required features for this task
   *
   * @return an empty feature set.
   */
  public FeatureSet getFeatureSet() {
    return featureSet;
  }

  /**
   * Sets an empty feature set to describe the class feature and required features for this task
   *
   * @param featureSet an empty feature set.
   */
  public void setFeatureSet(FeatureSet featureSet) {
    this.featureSet = featureSet;
  }
}
