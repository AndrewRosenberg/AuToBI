/* ContourDifferenceFeatureExtractor.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Calculates the RMSE difference between two contours.
 * <p/>
 * TODO: rename this class
 */
@SuppressWarnings("unchecked")
public class ContourDifferenceFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "rmse,meanError";
  private String f1;
  private String f2;

  public ContourDifferenceFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add(f1);
    this.required_features.add(f2);
    this.extracted_features.add("rmse[" + f1 + "," + f2 + "]");
    this.extracted_features.add("meanError[" + f1 + "," + f2 + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(f1) && r.hasAttribute(f2)) {
        Contour c1, c2;
        try {
          c1 = ContourUtils.getSubContour((Contour) r.getAttribute(f1), r.getStart(), r.getEnd());
          c2 = ContourUtils.getSubContour((Contour) r.getAttribute(f2), r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        r.setAttribute("rmse[" + f1 + "," + f2 + "]", contourRMSE(c1, c2));
        r.setAttribute("meanError[" + f1 + "," + f2 + "]", contourError(c1, c2));
      }
    }
  }

  private Double contourError(Contour c1, Contour c2) {
    double error = 0.0;
    for (Pair<Double, Double> p : c1) {
      error += (p.second - c2.get(p.first));
    }
    error /= c1.contentSize();
    return error;
  }

  private Double contourRMSE(Contour c1, Contour c2) {
    double rmse = 0.0;
    for (Pair<Double, Double> p : c1) {
      rmse += (p.second - c2.get(p.first)) * (p.second - c2.get(p.first));
    }
    rmse /= c1.contentSize();
    rmse = Math.sqrt(rmse);
    return rmse;
  }
}
