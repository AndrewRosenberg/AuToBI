/*  NormalizationParameterFeatureExtractor.java

    Copyright (c) 2011-2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * NormalizationParameterFeatureExtractor generates a set of normalization parameters based on pitch and intensity
 * across the full set of regions.
 * <p/>
 * This is used in situations where there is not previously generated speaker normalization parameters.
 * <p/>
 * The operation does assume, however, that the file contains speech by a single speaker.
 */
@SuppressWarnings("unchecked")
public class NormalizationParameterFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spkrNormParams";
  private String destination_feature;  // the destination feature name

  /**
   * Constructs a new NormalizationParameterFeatureExtractor.
   * <p/>
   * Currently this class requires "f0" and "I" attributes, and generates normalization parameters for pitch and
   * intensity.
   *
   * @param destination_feature the name of the feature to store the feature in.
   */
  @Deprecated
  public NormalizationParameterFeatureExtractor(String destination_feature) {
    this.destination_feature = destination_feature;

    this.extracted_features.add(destination_feature);
    // TODO: allow the normalized features to be specified through parameters
    this.required_features.add("f0");
    this.required_features.add("I");
  }

  public NormalizationParameterFeatureExtractor() {
    this.destination_feature = moniker;

    this.extracted_features.add(destination_feature);
    // TODO: allow the normalized features to be specified through parameters
    this.required_features.add("f0");
    this.required_features.add("I");
  }

  /**
   * Generates a SpeakerNormalizationParameter across all available pitch and intensity information and associates this
   * object with each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("f0")) {
        Contour pitch;
        try {
          pitch = ContourUtils.getSubContour((Contour) r.getAttribute("f0"), r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        snp.insertPitch(pitch);
      }
      if (r.hasAttribute("I")) {
        Contour intensity;
        try {
          intensity = ContourUtils.getSubContour((Contour) r.getAttribute("I"), r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        snp.insertIntensity(intensity);
      }
    }

    for (Region r : (List<Region>) regions) {
      r.setAttribute(destination_feature, snp);
    }
  }
}
