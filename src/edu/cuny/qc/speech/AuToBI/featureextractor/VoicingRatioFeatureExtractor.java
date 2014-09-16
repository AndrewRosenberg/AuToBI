/* VoicingRatioFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Voiced to unvoiced ratio
 */
@SuppressWarnings("unchecked")
public class VoicingRatioFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "voicingRatio";

  private String pitch_feature;  // the feature containing the pitch feature to determine voicing

  public VoicingRatioFeatureExtractor(String pitch_feature) {
    this.pitch_feature = pitch_feature;

    this.required_features.add(pitch_feature);
    this.extracted_features.add("voicingRatio[" + pitch_feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {

      Contour pitch;
      try {
        pitch = ContourUtils.getSubContour((Contour) r.getAttribute(pitch_feature), r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage() + ":" + r.getFile());
      }
      if (pitch != null) {
        r.setAttribute("voicingRatio[" + pitch_feature + "]", pitch.contentSize() * 1.0 / pitch.size());
      } else {
        r.setAttribute("voicingRatio[" + pitch_feature + "]", 0.0);
      }
    }
  }
}
