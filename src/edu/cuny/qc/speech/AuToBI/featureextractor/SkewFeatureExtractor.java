/*  SkewFeatureExtractor.java

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
 * SkewFeatureExtractor calculates skew coefficients over each region.  Skew coefficients are calculated based on Paul
 * Taylor's Tilt parameterizations. They are used to calculate the offset between the tilt of pitch and intensity
 * contours.
 * <p/>
 * If the two contours are aligned the skew is zero.  If the pitch contour is left skewed, the skew values are negative,
 * and vice versa.
 *
 * @see TiltFeatureExtractor
 * @see edu.cuny.qc.speech.AuToBI.core.TiltParameters
 */
@SuppressWarnings("unchecked")
public class SkewFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "skewAmp,skewDur";

  private String f1;
  private String f2;

  /**
   * Constructs a new SkewFeatureExtractor.
   */
  public SkewFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    required_features.add("tiltAmp[" + f1 + "]");
    required_features.add("tiltDur[" + f1 + "]");
    required_features.add("tiltAmp[" + f2 + "]");
    required_features.add("tiltDur[" + f2 + "]");

    extracted_features.add("skewAmp[" + f1 + "," + f2 + "]");
    extracted_features.add("skewDur[" + f1 + "," + f2 + "]");
  }

  /**
   * Calculates skew features from each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException should never happen
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("tiltAmp[" + f1 + "]") && r.hasAttribute("tiltDur[" + f1 + "]") &&
          r.hasAttribute("tiltAmp[" + f2 + "]") && r.hasAttribute("tiltDur[" + f2 + "]")) {
        r.setAttribute("skewAmp[" + f1 + "," + f2 + "]",
            ((Double) r.getAttribute("tiltAmp[" + f1 + "]")) - ((Double) r.getAttribute("tiltAmp[" + f2 + "]")));
        r.setAttribute("skewDur[" + f1 + "," + f2 + "]",
            ((Double) r.getAttribute("tiltDur[" + f1 + "]")) - ((Double) r.getAttribute("tiltDur[" + f2 + "]")));
      }
    }
  }
}
