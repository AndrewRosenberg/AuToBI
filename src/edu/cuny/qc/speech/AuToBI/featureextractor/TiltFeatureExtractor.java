/*  TiltFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * TiltFeatureExtractor is used to extract Paul Taylor's Tilt features over a given region.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.TiltParameters
 */
@SuppressWarnings("unchecked")
public class TiltFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "tilt,tiltAmp,tiltDur";

  private String contour_feature;  // the Contour feature to analyze

  /**
   * Constructs a new TiltFeatureExtractor to calculate Tilt features using the given feature
   *
   * @param contour_feature the Contour feature name
   */
  public TiltFeatureExtractor(String contour_feature) {
    this.contour_feature = contour_feature;
    required_features.add(contour_feature);

    extracted_features.add("tilt[" + contour_feature + "]");
    extracted_features.add("tiltAmp[" + contour_feature + "]");
    extracted_features.add("tiltDur[" + contour_feature + "]");
  }

  /**
   * Calculates Tilt parameters for the contour referenced by contour_feature over each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException should never happen.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (!r.hasAttribute(contour_feature)) {
        AuToBIUtils.warn("region, " + r.toString() + " doesn't have attribute: " + contour_feature);
        continue;
      }
      TiltParameters tilt;
      try {
        tilt =
            new TiltParameters(ContourUtils.getSubContour((Contour) r.getAttribute(contour_feature), r.getStart(),
                r.getEnd()));
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }

      r.setAttribute("tilt[" + contour_feature + "]", tilt.getTilt());
      r.setAttribute("tiltAmp[" + contour_feature + "]", tilt.getAmplitudeTilt());
      r.setAttribute("tiltDur[" + contour_feature + "]", tilt.getDurationTilt());
    }
  }
}
