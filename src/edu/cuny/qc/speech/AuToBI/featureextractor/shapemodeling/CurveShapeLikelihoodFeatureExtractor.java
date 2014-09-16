/* CurveShapeLikelihoodFeatureExtractor.java

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
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class CurveShapeLikelihoodFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "risingLL,fallingLL,peakLL,valleyLL";
  private String feature;

  public CurveShapeLikelihoodFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add("risingCurve[" + feature + "]");
    this.required_features.add("fallingCurve[" + feature + "]");
    this.required_features.add("peakCurve[" + feature + "]");
    this.required_features.add("valleyCurve[" + feature + "]");

    this.extracted_features.add("risingLL[" + feature + "]");
    this.extracted_features.add("fallingLL[" + feature + "]");
    this.extracted_features.add("peakLL[" + feature + "]");
    this.extracted_features.add("valleyLL[" + feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("risingCurve[" + feature + "]") && r.hasAttribute("fallingCurve[" + feature + "]") &&
          r.hasAttribute("peakCurve[" + feature + "]") && r.hasAttribute("valleyCurve[" + feature + "]")) {
        CurveShape rising =
            (CurveShape) r.getAttribute("risingCurve[" + feature + "]");
        CurveShape falling =
            (CurveShape) r.getAttribute("fallingCurve[" + feature + "]");
        CurveShape peak =
            (CurveShape) r.getAttribute("peakCurve[" + feature + "]");
        CurveShape valley =
            (CurveShape) r.getAttribute("valleyCurve[" + feature + "]");

        double maxrmse = Math.max(rising.rmse, Math.max(falling.rmse, Math.max(peak.rmse, valley.rmse)));
        Distribution d = new Distribution();
        d.add("rising", maxrmse - rising.rmse);
        d.add("falling", maxrmse - falling.rmse);
        d.add("peak", maxrmse - peak.rmse);
        d.add("valley", maxrmse - valley.rmse);
        try {
          d.normalize();
        } catch (AuToBIException e) {
          // This exception is thrown when the distribution is even -- therefore all rmse values are equal to the max.
          // Since this happens so often, we'll set this to be even.
          d.put("rising", 0.25);
          d.put("falling", 0.25);
          d.put("peak", 0.25);
          d.put("valley", 0.25);
        }

        r.setAttribute("risingLL[" + feature + "]", d.get("rising"));
        r.setAttribute("fallingLL[" + feature + "]", d.get("falling"));
        r.setAttribute("peakLL[" + feature + "]", d.get("peak"));
        r.setAttribute("valleyLL[" + feature + "]", d.get("valley"));
      }
    }
  }
}
