/* RatioFeatureExtractor.java

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
 * Created with IntelliJ IDEA. User: andrew Date: 7/24/12 Time: 11:13 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class RatioFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "ratio";
  public static final double MAX_VALUE = 1000;
  private String f1;
  private String f2;

  public RatioFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add(f1);
    this.required_features.add(f2);
    this.extracted_features.add(moniker + "[" + f1 + "," + f2 + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(f1) && r.hasAttribute(f2)) {
        double value = ((Double) r.getAttribute(f1)) / ((Double) r.getAttribute(f2));
        r.setAttribute(moniker + "[" + f1 + "," + f2 + "]", Math.max(Math.min(value, MAX_VALUE), -MAX_VALUE));
      }
    }
  }
}
