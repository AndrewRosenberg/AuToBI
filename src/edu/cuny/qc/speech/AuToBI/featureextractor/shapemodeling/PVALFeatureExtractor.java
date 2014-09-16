/* PVALFeatureExtractior.java

  Copyright 2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.GParam;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Calculates Peak/Valley Amplitude and location features.
 * <p/>
 * These features are based on the isotonic regression output of peak and valley curves and likelihood.
 * <p/>
 * The amplitude PVAmp[feature] is the mean of the high or low component of a univariate, two-component GMM. If
 * the contour was classified as a peak the high component is used, if a valley, the low component is used.
 */
@SuppressWarnings("unchecked")
public class PVALFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "PVAmp,PVLocation";
  private String feature;   // name of the feature

  public PVALFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature);
    this.required_features.add("lowGP[" + feature + "]");
    this.required_features.add("highGP[" + feature + "]");
    this.required_features.add("peakCurve[" + feature + "]");
    this.required_features.add("peakLL[" + feature + "]");
    this.required_features.add("valleyCurve[" + feature + "]");
    this.required_features.add("valleyLL[" + feature + "]");

    this.extracted_features.add("PVAmp[" + feature + "]");
    this.extracted_features.add("PVLocation[" + feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("peakLL[" + feature + "]") && r.hasAttribute("valleyLL[" + feature + "]")) {
        double p_peak = (Double) r.getAttribute("peakLL[" + feature + "]");
        double p_valley = (Double) r.getAttribute("valleyLL[" + feature + "]");
        CurveShape curve;
        if (p_peak >= p_valley) {
          curve = (CurveShape) r.getAttribute("peakCurve[" + feature + "]");
        } else {
          curve = (CurveShape) r.getAttribute("valleyCurve[" + feature + "]");
        }

        Contour c;
        try {
          c = ContourUtils.getSubContour((Contour) r.getAttribute(feature), r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        r.setAttribute("PVLocation[" + feature + "]", 1 - (r.getEnd() - c.timeFromIndex(curve.peak)) / r.getDuration());
        GParam gp;
        if (p_peak >= p_valley) {
          gp = (GParam) r.getAttribute("lowGP[" + feature + "]");
        } else {
          gp = (GParam) r.getAttribute("highGP[" + feature + "]");
        }
        r.setAttribute("PVAmp[" + feature + "]", Math.abs(c.get(curve.peak) - gp.mean));
      }
    }
  }
}
