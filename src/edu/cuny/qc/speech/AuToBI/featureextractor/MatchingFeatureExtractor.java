/*  MatchingFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.List;

/**
 * MatchingFeatureExtractor extracts a feature that determines if two other features match, or differ.
 * <p/>
 * Currently, if two variables match the resulting feature value is "CORRECT" and if they differ it is "INCORRECT". This
 * decision was made because this feature extractor is typicaly used to compare predicted values to hypothesized values.
 * A reasonable extension to this will be to allow a user to specify the values for matching and differing feature
 * values.
 */
@SuppressWarnings("unchecked")
public class MatchingFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "matching";

  private String feature1;     // One of the feature names
  private String feature2;     // The second feature name
  private String destination;  // Name for the destination variable.

  /**
   * Constructs a new MatchingFeatureExtractor to compare the values of two features, feature1, and feature2.
   * <p/>
   * The result "CORRECT" or "INCORRECT" is stored in a feature indicated by destination_feature.
   *
   * @param feature1            The first feature
   * @param feature2            The second feature
   * @param destination_feature The feature to store the result
   */
  @Deprecated
  public MatchingFeatureExtractor(String feature1, String feature2, String destination_feature) {
    super();
    this.feature1 = feature1;
    this.feature2 = feature2;
    this.destination = destination_feature;
    required_features.add(feature1);
    required_features.add(feature2);
    extracted_features.add(destination_feature);
  }

  public MatchingFeatureExtractor(String feature1, String feature2) {
    super();
    this.feature1 = feature1;
    this.feature2 = feature2;
    this.destination = "matching[" + feature1 + "," + feature2 + "]";
    required_features.add(feature1);
    required_features.add(feature2);
    extracted_features.add(destination);
  }

  /**
   * Extracts matching feature for each region.
   * <p/>
   * If the two features are equal the destination feature is "CORRECT" otherwise "INCORRECT"
   * <p/>
   * No destination feature is set if either feature is not present on a region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if any region does not have one of the matching features.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature1) && r.hasAttribute(feature2)) {
        if (r.getAttribute(feature1).equals(r.getAttribute(feature2))) {
          r.setAttribute(destination, "CORRECT");
        } else {
          r.setAttribute(destination, "INCORRECT");
        }
      }
    }
  }
}
