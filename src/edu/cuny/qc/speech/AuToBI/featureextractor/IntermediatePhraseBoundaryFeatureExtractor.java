/*  IntermediatePhraseBoundaryFeatureSet.java

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

import edu.cuny.qc.speech.AuToBI.util.ToBIUtils;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.List;

/**
 * IntermediatePhraseBoundaryFeatureExtractor is a FeatureExtractor that is responsible for extracting ground truth
 * labels for whether or not a word boundary is an intermediate phrase boundary.
 */
public class IntermediatePhraseBoundaryFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "nominal_IntermediatePhraseBoundary";
  private String feature;  // the stored feature name

  /**
   * Constructs a new IntermediatePhraseBoundaryFeatureExtractor.
   * <p/>
   * The ground truth label will be stored in the feature name passed via the parameter, feature.
   *
   * @param feature the stored parameter name
   */
  @Deprecated
  public IntermediatePhraseBoundaryFeatureExtractor(String feature) {
    this.feature = feature;
    extracted_features.add(feature);
  }

  public IntermediatePhraseBoundaryFeatureExtractor() {
    this.feature = moniker;
    extracted_features.add(feature);
  }

  /**
   * Extracts the ground truth intermediate phrase boundary labels.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException If something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    ToBIUtils.setIntermediatePhraseBoundary((List<Word>) regions, feature);
  }
}