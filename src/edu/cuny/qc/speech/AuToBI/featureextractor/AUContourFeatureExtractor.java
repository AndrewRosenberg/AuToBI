/*  AUContourFeatureExtractor.java

    Copyright (c) 2012-2014 Andrew Rosenberg

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
 * Extracts the area under a contour.
 */
@SuppressWarnings("unchecked")
public class AUContourFeatureExtractor extends FeatureExtractor {
  private String feature;  // The contour feature to calculate the area under

  public static final String moniker = "area";

  public AUContourFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature);
    this.extracted_features.add("area[" + feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature)) {
        Contour super_c = (Contour) r.getAttribute(feature);
        Contour c;
        try {
          c = ContourUtils.getSubContour(super_c, r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        double sum = 0.0;
        for (Pair<Double, Double> x : c) {
          sum += x.second;
        }
        r.setAttribute("area[" + feature + "]", sum);
      }
    }
  }
}
