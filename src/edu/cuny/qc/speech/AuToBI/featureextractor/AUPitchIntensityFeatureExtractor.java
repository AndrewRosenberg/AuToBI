/*  AUMultipleContourFeatureExtractor.java

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
 * Extracts the area under multiple contours with weights for each.
 */
@SuppressWarnings("unchecked")
public class AUPitchIntensityFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "area2";

  private String extracted_f;  // the name of the extracted feature.
  private String pitch_f;      // the name of the pitch feature
  private String i_f;          // the name of the intensity feature
  private double i_coeff;      // the intensity scaling coefficient

  public AUPitchIntensityFeatureExtractor(String pitch_f, String i_f, String i_coeff) {
    this(pitch_f, i_f, Double.parseDouble(i_coeff));
    // Respect the user specified string
    this.extracted_f = "area2[" + pitch_f + "," + i_f + "," + i_coeff + "]";
  }

  public AUPitchIntensityFeatureExtractor(String pitch_f, String i_f, double i_coeff) {
    this.pitch_f = pitch_f;
    this.i_f = i_f;
    this.i_coeff = i_coeff;

    this.extracted_f = "area2[" + pitch_f + "," + i_f + "," + i_coeff + "]";
    this.extracted_features.add(extracted_f);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      Contour super_pitch_c = (Contour) r.getAttribute(pitch_f);
      Contour pitch_c;
      try {
        pitch_c = ContourUtils.getSubContour(super_pitch_c, r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      Contour super_i_c = (Contour) r.getAttribute(i_f);
      Contour i_c;
      try {
        i_c = ContourUtils.getSubContour(super_i_c, r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      double sum = 0.0;
      for (Pair<Double, Double> x : pitch_c) {
        double time = x.first;
        if (!Double.isNaN(x.second) && !i_c.isEmpty(i_c.indexFromTime(time))) {
          sum += i_c.get(time) * i_coeff * x.second;
        }
      }

      r.setAttribute(extracted_f, sum);
    }
  }
}
