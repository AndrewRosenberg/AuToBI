/* SpeakerNormalizationParameterFeatureExtractor.java

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

import java.util.HashMap;
import java.util.List;

/**
 * Generates speaker normalization parameters based on a speaker id feature.
 */
public class SpeakerNormalizationParameterFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "spkrNormParameterFeatures";
  private String speaker_id_feature;
  private String destination_feature;

  public SpeakerNormalizationParameterFeatureExtractor(String speaker_id_feature, String destination_feature) {
    this.speaker_id_feature = speaker_id_feature;
    this.destination_feature = destination_feature;

    required_features.add("f0");
    required_features.add("I");
    extracted_features.add(destination_feature);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<String, SpeakerNormalizationParameter> params = new HashMap<String, SpeakerNormalizationParameter>();

    for (Region r : (List<Region>) regions) {
      SpeakerNormalizationParameter norm_params;
      if (!params.containsKey(r.getAttribute(speaker_id_feature))) {
        params.put((String) r.getAttribute(speaker_id_feature),
            new SpeakerNormalizationParameter(r.getAttribute(speaker_id_feature).toString()));
      }
      norm_params = params.get(r.getAttribute(speaker_id_feature));
      if (r.hasAttribute("f0")) {
        try {
          norm_params.insertPitch(ContourUtils.getSubContour((Contour) r.getAttribute("f0"), r.getStart(), r.getEnd()));
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
      if (r.hasAttribute("I")) {
        try {
          norm_params
              .insertIntensity(ContourUtils.getSubContour((Contour) r.getAttribute("I"), r.getStart(), r.getEnd()));
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
    }

    for (Region r : (List<Region>) regions) {
      r.setAttribute(destination_feature, params.get(r.getAttribute(speaker_id_feature)));
    }
  }
}
