/* SurroundingContextFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/17/12 Time: 2:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class SurroundingContextFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "prev,next";

  private String feature;  // feature to copy

  public SurroundingContextFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add(feature);
    this.extracted_features.add("prev[" + feature + "]");
    this.extracted_features.add("next[" + feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (int i = 0; i < regions.size(); ++i) {
      Region r = (Region) regions.get(i);
      if (i != 0) {
        if (((Region) regions.get(i - 1)).hasAttribute(feature)) {
          r.setAttribute("prev[" + feature + "]", ((Region) regions.get(i - 1)).getAttribute(feature));
        }
      }
      if (i != regions.size() - 1) {
        if (((Region) regions.get(i + 1)).hasAttribute(feature)) {
          r.setAttribute("next[" + feature + "]", ((Region) regions.get(i + 1)).getAttribute(feature));
        }
      }
    }
  }
}
