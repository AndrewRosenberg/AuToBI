/*  AuToBIClassifier.java

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.io.Serializable;

/**
 * An abstract serializable class to store, call and train classifiers.
 */
public abstract class AuToBIClassifier implements Serializable {

  private static final long serialVersionUID = 20090507L;

  /**
   * Return the normalized posterior distribution from the classifier.
   *
   * @param testing_point The point to evaluate
   * @return a normalized posterior distribution
   * @throws Exception If something fails.
   */
  public abstract Distribution distributionForInstance(Word testing_point) throws Exception;

  /**
   * Train the classifier on the given FeatureSet
   *
   * @param feature_set The training data
   * @throws Exception If something fails
   */
  public abstract void train(FeatureSet feature_set) throws Exception;

  /**
   * Construct and return an untrained copy of the classifier.
   *
   * @return the new classifier
   */
  public abstract AuToBIClassifier newInstance();

  /**
   * Classifies a given test point.
   *
   * @param testing_point the test point
   * @return the hypothesized value
   * @throws Exception if something goes wrong.
   */
  public String classify(Word testing_point) throws Exception {
    Distribution dist = distributionForInstance(testing_point);
    if (dist == null) {
      return null;
    }
    return dist.getKeyWithMaximumValue();
  }
}
