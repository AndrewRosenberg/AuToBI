/*  CorrectionSpectrumPADFeatureExtractor.java

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
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;


import java.util.List;

/**
 * A feature extraction to generate hypothesized correction values for spectrum based pitch accent detection.
 * <p/>
 * The Corrected Classifier is a two tiered ensemble technique where first predictions are generated, then they are
 * corrected using a second feature set.  This feature extraction routine generates hypotheses for the second --
 * correction -- tier.
 * <p/>
 * This is currently deprecated because it does not conform with the AuToBI 1.4 feature naming conventions
 */
@SuppressWarnings("unchecked")
@Deprecated
public class CorrectionSpectrumPADFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "correction_prediction,correction_prediction_confidence";

  private int low;                      // the low bark
  private int high;                     // the high bark
  private AuToBIClassifier classifier;  // The correction classifier
  private FeatureSet fs;                // A FeatureSet to describe the required features for the classifier

  /**
   * Constructs a CorrectionSpectrumPADFeatureExtractor
   * <p/>
   * Each correction is associated with a frequency region defined in bark by low and high.
   *
   * @param low        the low bark value
   * @param high       the high bark value
   * @param classifier the correction classifier
   * @param fs         a feature set to describe the necessary features for the classifier
   */
  public CorrectionSpectrumPADFeatureExtractor(int low, int high, AuToBIClassifier classifier, FeatureSet fs) {
    this.low = low;
    this.high = high;
    this.classifier = classifier;
    this.fs = fs;

    extracted_features.add("nominal_bark_" + low + "_" + high + "__correction_prediction");
    extracted_features.add("bark_" + low + "_" + high + "__correction_prediction_confidence");

    required_features.addAll(fs.getRequiredFeatures());
    required_features.add(fs.getClassAttribute());
  }


  /**
   * Extracts correction features over each region.
   *
   * @param regions the regions to generate features for
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {

    // Construct a feature set.
    FeatureSet feature_set = fs.newInstance();

    feature_set.setDataPoints(regions);
    feature_set.constructFeatures();

    for (Word w : (List<Word>) regions) {
      try {
        Distribution result = classifier.distributionForInstance(w);

        w.setAttribute("nominal_bark_" + low + "_" + high + "__correction_prediction", result.getKeyWithMaximumValue());
        w.setAttribute("bark_" + low + "_" + high + "__correction_prediction_confidence",
            result.get(result.getKeyWithMaximumValue()));
      } catch (Exception e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}