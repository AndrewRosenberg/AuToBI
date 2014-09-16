/*  HypothesizedEventFeatureExtractor.java

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
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;

import java.util.List;

/**
 * HypothesizedEventFeatureExtractor is used to drive the AuToBI hypothesis generation through the FeatureExtractor
 * interface.    This allows the feature reference counting structure to be leveraged across multiple classification
 * tasks.
 */
public class HypothesizedEventFeatureExtractor extends FeatureExtractor {
  private String hyp_feature; // The feature to store the hypothesis
  private AuToBIClassifier classifier; // The classifier to generate hypotheses.
  private FeatureSet fs; // The feature set describing the required features.

  /**
   * Constructs a new HypothesizedEventFeatureExtractor.
   *
   * @param hyp_feature the feature to store the generated hypothesis in
   * @param classifier  the classifier to use to generate the hypothesis
   * @param fs          a feature set describing the required features.
   */
  public HypothesizedEventFeatureExtractor(String hyp_feature, AuToBIClassifier classifier, FeatureSet fs) {
    this.hyp_feature = hyp_feature;
    this.classifier = classifier;

    extracted_features.add(hyp_feature);
    required_features.addAll(fs.getRequiredFeatures());
  }

  /**
   * Generates an appropriate hypothesized event for each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      if (r instanceof Word) {
        try {
          String hyp = classifier.classify((Word) r);
          r.setAttribute(hyp_feature, hyp);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
