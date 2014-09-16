/*  SpectrumPADFeatureExtractor.java

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
 * SpectrumPADFeatureExtractor is a FeatureExtractor which generates hypothesized pitch accent detection (PAD)
 * prediction hypotheses given a particular spectral region.
 * <p/>
 * This feature extractor requires the association of an externally trained pitch accent detection classifier and a
 * descriptor of the spectral region -- here typically in terms of a low and high bark boundary.
 * <p/>
 * This is deprecated because it's functionality does not conform to the AuToBI v1.4 feature extraction conventions
 */
@SuppressWarnings("unchecked")
@Deprecated
public class SpectrumPADFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spectrumPAD";

  private final String ACCENTED_VALUE = "ACCENTED";  // a label for ACCENTED words
  private int low;                                   // the bottom of the spectral region (in bark)
  private int high;                                  // the top of the spectral region (in bark)
  private AuToBIClassifier classifier;               // the classifier responsible for generating predictions
  private FeatureSet fs;                             // a featureSet to describe the features requied by the classifier

  /**
   * Constructs a new SpectrumPADFeatureExtractor given a spectral region, externally trained classifier, and AuToBI
   * object
   *
   * @param low        the low boundary of the spectral region -- in bark units
   * @param high       the high boundary of the spectral region
   * @param classifier the classifier responsible for generating hypotheses
   * @param fs         the AuToBI object to guide feature extraction
   */
  public SpectrumPADFeatureExtractor(int low, int high, AuToBIClassifier classifier, FeatureSet fs) {
    this.low = low;
    this.high = high;
    this.classifier = classifier;
    this.fs = fs;

    extracted_features.add("nominal_bark_" + low + "_" + high + "__prediction");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence");
    extracted_features.add("bark_" + low + "_" + high + "__prediction_confidence_accented");

    required_features.addAll(fs.getRequiredFeatures());
    required_features.add(fs.getClassAttribute());
  }

  /**
   * Generates hypothesed pitch accent detection predictions for each region and stores these with the regions.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Construct a feature set.
    FeatureSet feature_set = fs.newInstance();

    // Extract spectrum features.
    feature_set.setDataPoints((List<Word>) regions);
    feature_set.constructFeatures();

    for (Word w : (List<Word>) regions) {
      try {
        Distribution result = classifier.distributionForInstance(w);

        w.setAttribute("nominal_bark_" + low + "_" + high + "__prediction", result.getKeyWithMaximumValue());
        w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence",
            result.get(result.getKeyWithMaximumValue()));
        w.setAttribute("bark_" + low + "_" + high + "__prediction_confidence_accented", result.get(ACCENTED_VALUE));
      } catch (Exception e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}
