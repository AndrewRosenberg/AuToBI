/*  RangeNormalizedContourFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.HashMap;
import java.util.List;

/**
 * NormalizedContourFeatureExtractor normalizes a contour represented by a list of time value pairs and aligns the
 * resulting normalized contour to the supplied regions.
 * <p/>
 * The normalization is performed using range normalization ((x-min) / (max-min)) based on a supplied
 * SpeakerNormalizationParameter object.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter
 */
@SuppressWarnings("unchecked")
public class RangeNormalizedContourFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "rnormC";
  private String feature_name;  // the feature to analyze
  private String norm_feature;  // the parameters to run the normalization

  /**
   * Constructs a new NormalizedContourFeatureExtractor to analyze the supplied feature_name using the supplied
   * parameters
   *
   * @param feature_name          the feature to modify
   * @param normalization_feature the zscore normalization parameters to apply
   */
  public RangeNormalizedContourFeatureExtractor(String feature_name, String normalization_feature) {
    this.feature_name = feature_name;
    this.norm_feature = normalization_feature;

    required_features.add(feature_name);
    required_features.add(normalization_feature);
    extracted_features.add("rnormC[" + feature_name + "]");
  }

  /**
   * Constructs a new NormalizedContourFeatureExtractor to analyze the supplied feature_name using the supplied
   * parameters
   *
   * @param feature_name the feature to modify
   */
  public RangeNormalizedContourFeatureExtractor(String feature_name) {
    this.feature_name = feature_name;
    this.norm_feature = "spkrNormParams";

    required_features.add(feature_name);
    required_features.add(this.norm_feature);
    extracted_features.add("rnormC[" + feature_name + "]");
  }

  /**
   * Performs z-score normalization on the specified contour, storing the result in a new list of TimeValuePairs
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if the normalization parameters
   *                                                                              cannot normalize the features
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<Contour, Contour> cache = new HashMap<Contour, Contour>();

    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature_name) && r.hasAttribute(norm_feature)) {
        Contour c = (Contour) r.getAttribute(feature_name);
        if (cache.containsKey(c)) {
          r.setAttribute("rnormC[" + feature_name + "]", cache.get(c));
        } else {
          SpeakerNormalizationParameter norm_params = (SpeakerNormalizationParameter) r.getAttribute(norm_feature);
          if (norm_params.canNormalize(feature_name)) {
            Contour norm_contour =
                ContourUtils.rangeNormalizeContour(c, norm_params, feature_name);
            r.setAttribute("rnormC[" + feature_name + "]", norm_contour);
            cache.put(c, norm_contour);
          } else {
            throw new FeatureExtractorException(
                "Supplied normalization parameters cannot normalize values: " + feature_name);
          }
        }
      }
    }
  }
}
