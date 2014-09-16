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

import edu.cuny.qc.speech.AuToBI.*;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * IntensityFeatureExtractor extracts intensity information from a given WavData object.
 * <p/>
 * * v1.4 IntensityFeatureExtractor has changed to attach full contours to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
public class IntensityFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "I";

  private String feature_name;  // the name of the feature to hold intensity information

  @Deprecated
  public IntensityFeatureExtractor(String feature_name) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  public IntensityFeatureExtractor() {
    this.feature_name = moniker;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
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
      IntensityExtractor extractor = new IntensityExtractor(wav);
      Contour contour = extractor.soundToIntensity();
      // Assign pointer to the full contour to all data points.
      for (Region r : wave_region_map.get(wav)) {
        r.setAttribute(feature_name, contour);
      }
    }
  }
}