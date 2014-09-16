/*  PitchFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.IntensityExtractor;
import edu.cuny.qc.speech.AuToBI.RAPTPitchExtractor;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PitchFeatureExtractor extracts pitch information from a given WavData object.
 * <p/>
 * v1.4 PitchFeatureExtractor has changed to attach full pitch contours to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
@SuppressWarnings("ALL")
public class PitchFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "f0";

  private String feature_name;  // the name of the feature to hold pitch information
  private double threshold;     // the intensity threshold to determine silence.

  @Deprecated
  public PitchFeatureExtractor(String feature_name) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = Double.NaN;
  }

  public PitchFeatureExtractor() {
    this.feature_name = moniker;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = Double.NaN;
  }

  @Deprecated
  public PitchFeatureExtractor(String feature_name, double threshold) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = threshold;
  }

  public PitchFeatureExtractor(double threshold) {
    this.feature_name = moniker + "[" + ((Double) threshold).toString() + "]";

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = threshold;
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    try {
      // Identify all regions which are associated with each wav data.
      HashMap<WavData, List<Region>> wave_region_map = new HashMap<WavData, List<Region>>();
      for (Region r : (List<Region>) regions) {
        WavData wav = (WavData) r.getAttribute("wav");
        if (wav != null) {
          if (!wave_region_map.containsKey(wav)) {
            wave_region_map.put(wav, new ArrayList<Region>());
          }
          wave_region_map.get(wav).add(r);
        }
      }

      for (WavData wav : wave_region_map.keySet()) {
//        PitchExtractor extractor = new PitchExtractor(wav);
        RAPTPitchExtractor extractor = new RAPTPitchExtractor();
        Contour pitch_contour = extractor.getPitch(wav);
        if (!Double.isNaN(threshold)) {
          // Interpolate over non-silent regions
          IntensityExtractor int_extractor = new IntensityExtractor(wav);
          Contour intensity = int_extractor.soundToIntensity();
          pitch_contour = ContourUtils.interpolate(pitch_contour, intensity, threshold);
        }

        // Assign pointer to the full contour to all data points.
        // Specific aggregate feature extractors will access the points that are needed.
        for (Region r : wave_region_map.get(wav)) {
          r.setAttribute(feature_name, pitch_contour);
//          Contour c = ContourUtils.getSubContour(pitch_contour, r.getStart(), r.getEnd());
        }
      }
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
  }
}
