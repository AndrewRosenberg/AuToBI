/* HighLowDifferenceFeatureExtractor.java

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
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class HighLowDifferenceFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "highLowDiff";
  private String feature; // the name of the feature name

  public HighLowDifferenceFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add("highGP[" + feature + "]");
    this.required_features.add("lowGP[" + feature + "]");

    this.extracted_features.add("highLowDiff[" + feature + "]");
  }


  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("lowGP[" + feature + "]") && r.hasAttribute("highGP[" + feature + "]")) {
        GParam lowgp = (GParam) r.getAttribute("lowGP[" + feature + "]");
        GParam highgp = (GParam) r.getAttribute("highGP[" + feature + "]");
        r.setAttribute("highLowDiff[" + feature + "]", highgp.mean - lowgp.mean);
      }
    }
  }
}
