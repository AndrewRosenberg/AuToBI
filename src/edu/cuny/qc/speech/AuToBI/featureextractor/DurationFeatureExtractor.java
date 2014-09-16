/*  DurationFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.List;

/**
 * DurationFeatureExtractor extracts features related to the length of the regions.
 * <p/>
 * The extracted features include the duration in seconds, the duration normalized by the duration of surrounding words,
 * and the length and presence of any following silences.
 * <p/>
 * TODO: This behavior should almost certainly be refactored into a broader set of feature extractors
 */
public class DurationFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "duration,prevPause,follPause,nominal_followsSilence,nominal_precedesSilence";

  // Gaps between regions greater than this length are considered silent.
  private static final Double SILENCE_THRESHOLD = 0.01;

  /**
   * Constructs a DurationFeatureExtractor.
   */
  public DurationFeatureExtractor() {
    super();
    extracted_features.add("duration");
    extracted_features.add("prevPause");
    extracted_features.add("follPause");
    extracted_features.add("nominal_followsSilence");
    extracted_features.add("nominal_precedesSilence");
  }

  /**
   * Extracts duration features from the data points.
   *
   * @param data_points the data points
   * @throws FeatureExtractorException if the regions overlap
   */
  public void extractFeatures(List data_points) throws FeatureExtractorException {

    Double previous_pause;
    Double following_pause;
    Double max_pauselen = 120.0;
    Double epsilon = 0.001;
    for (int i = 0; i < data_points.size(); ++i) {
      if (i < data_points.size() - 1) {
        following_pause = Math.min(max_pauselen, ((Region) data_points.get(i + 1)).getStart() -
            ((Region) data_points.get(i)).getEnd());
      } else {
        following_pause = max_pauselen;
      }
      if (i > 0) {
        previous_pause = Math.min(max_pauselen, ((Region) data_points.get(i)).getStart() -
            ((Region) data_points.get(i - 1)).getEnd());
      } else {
        previous_pause = max_pauselen;
      }
      if (previous_pause < 0.0) {
        if (previous_pause > -epsilon) {
          previous_pause = 0.0;
        } else {
          AuToBIUtils.warn(
              "Invalid previous pause value on word: " + data_points.get(i).toString());
        }
      }

      if (following_pause < 0.0) {
        if (following_pause > -epsilon) {
          following_pause = 0.0;
        } else {
          AuToBIUtils.warn(
              "Invalid following pause value on word: " + data_points.get(i).toString());
        }
      }

      Region region = (Region) data_points.get(i);

      region.setAttribute("duration", (region.getEnd() - region.getStart()));
      region.setAttribute("prevPause", previous_pause);
      region.setAttribute("follPause", following_pause);
      if (following_pause > SILENCE_THRESHOLD) {
        region.setAttribute("nominal_precedesSilence", "TRUE");
      } else {
        region.setAttribute("nominal_precedesSilence", "FALSE");
      }
      if (previous_pause > SILENCE_THRESHOLD) {
        region.setAttribute("nominal_followsSilence", "TRUE");
      } else {
        region.setAttribute("nominal_followsSilence", "FALSE");
      }
    }
  }
}
