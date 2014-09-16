/*  PitchAccentFeatureExtractor.java

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
 * PitchAccentFeatureExtractor extracts the ground truth presence or absence of a pitch accent for a set of regions.
 */
public class PitchAccentFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "nominal_PitchAccent";
  private String feature;  // the destination feature

  /**
   * Constructs a new PitchAccentFeatureExtractor.
   *
   * @param feature the destination feature
   */
  @Deprecated
  public PitchAccentFeatureExtractor(String feature) {
    this.feature = feature;
    extracted_features.add(feature);
  }

  public PitchAccentFeatureExtractor() {
    this.feature = moniker;
    extracted_features.add(feature);
  }

  /**
   * Stores an indicator of the presence or absence of a pitch accent on each region, based on ground truth annotation.
   * <p/>
   * Note: Currently the value of the feature if an accent is present is "ACCENTED",
   * and absence is indicated by "DEACCENTED".
   * This should probably be modified; the term, "deaccented" can have more connotations than simply not bearing pitch
   * accent. It may be best to use "ACCENT" and "NOACCENT" as a default while allowing a user to specify alternate
   * values.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if somethign goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    ToBIUtils.setPitchAccent((List<Word>) regions, feature);
  }
}
