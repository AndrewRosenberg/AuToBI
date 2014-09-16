/*  HypothesizedDistributionFeatureExtractor.java

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

package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.classifier.AuToBIClassifier;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.List;

/**
 * HypothesizedDistributionFeatureExtractor is used to drive the AuToBI hypothesis generation through the
 * FeatureExtractor
 * interface.
 * <p/>
 * This feature extractor generates a distribution over classes for each data point.
 */
public class HypothesizedDistributionFeatureExtractor extends FeatureExtractor {
  private String dist_feature; // The feature to store the distribution
  private AuToBIClassifier classifier; // The classifier to generate distribution.
  private FeatureSet fs; // The feature set describing the required features.

  /**
   * Constructs a new HypothesizedDistributionFeatureExtractor.
   *
   * @param dist_feature the feature to store the generated distribution in
   * @param classifier   the classifier to use to generate the distribution
   * @param fs           a feature set describing the required features.
   */
  public HypothesizedDistributionFeatureExtractor(String dist_feature, AuToBIClassifier classifier, FeatureSet fs) {
    this.dist_feature = dist_feature;
    this.classifier = classifier;

    extracted_features.add(dist_feature);
    required_features.addAll(fs.getRequiredFeatures());
  }

  /**
   * Generates an appropriate distribution for each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      if (r instanceof Word) {
        try {
          Distribution dist = classifier.distributionForInstance((Word) r);
          r.setAttribute(dist_feature, dist);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
