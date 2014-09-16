/*  ContourCenterOfGravityFeatureExtractor.java

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
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * ContourCenterOfGravityFeatureExtractor identifies the "Center of Gravity" in time of a contour.
 * <p/>
 * This has typically been used on f0 contours and called "Tonal Center of Gravity" or ToCG.
 * <p/>
 * Here we allow any contour -- intensity, spectral tilt, etc -- to be processed using the same notion.
 */
@SuppressWarnings("unchecked")
public class ContourCenterOfGravityFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "cog";
  private String attribute_name;       // the contour attribute name

  public ContourCenterOfGravityFeatureExtractor(String attribute_name) {
    this.attribute_name = attribute_name;
    extracted_features.add("cog[" + attribute_name + "]");
    required_features.add(attribute_name);
  }

  /**
   * Extracts center of gravity features for each region from the contour.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(attribute_name)) {
        Contour super_c = (Contour) r.getAttribute(attribute_name);
        Contour c;
        try {
          c = ContourUtils.getSubContour(super_c, r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        double num = 0.0;
        double denom = 0.0;
        for (Pair<Double, Double> tvp : c) {
          num += tvp.first * tvp.second;
          denom += tvp.second;
        }
        if (denom != 0.0) {
          r.setAttribute("cog[" + attribute_name + "]", ((num / denom) - r.getStart()) / r.getDuration());
        }
      }
    }
  }
}
