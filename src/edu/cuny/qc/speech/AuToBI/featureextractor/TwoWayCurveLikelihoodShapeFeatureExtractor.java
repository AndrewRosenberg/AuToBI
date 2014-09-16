/* TwoWayCurveLikelihoodShapeFeatureExtractor.java

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
 * Created with IntelliJ IDEA. User: andrew Date: 7/24/12 Time: 11:19 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class TwoWayCurveLikelihoodShapeFeatureExtractor extends FeatureExtractor {
  public static final String moniker =
      "rrLL,rfLL,rpLL,rvLL,frLL,ffLL,fpLL,fvLL,prLL,pfLL,ppLL,vvLL,vrLL,vfLL,vpLL,vvLL";

  private String f1;
  private String f2;

  public TwoWayCurveLikelihoodShapeFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add("risingLL[" + f1 + "]");
    this.required_features.add("fallingLL[" + f1 + "]");
    this.required_features.add("peakLL[" + f1 + "]");
    this.required_features.add("valleyLL[" + f1 + "]");

    this.required_features.add("risingLL[" + f2 + "]");
    this.required_features.add("fallingLL[" + f2 + "]");
    this.required_features.add("peakLL[" + f2 + "]");
    this.required_features.add("valleyLL[" + f2 + "]");

    this.extracted_features.add("rrLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("rfLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("rpLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("rvLL[" + f1 + "," + f2 + "]");

    this.extracted_features.add("frLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("ffLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("fpLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("fvLL[" + f1 + "," + f2 + "]");

    this.extracted_features.add("prLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("pfLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("ppLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("pvLL[" + f1 + "," + f2 + "]");

    this.extracted_features.add("vrLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("vfLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("vpLL[" + f1 + "," + f2 + "]");
    this.extracted_features.add("vvLL[" + f1 + "," + f2 + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      for (String shape1 : new String[]{"rising", "falling", "peak", "valley"}) {
        for (String shape2 : new String[]{"rising", "falling", "peak", "valley"}) {
          if (r.hasAttribute(shape1 + "LL[" + f1 + "]") &&
              r.hasAttribute(shape2 + "LL[" + f2 + "]")) {
            double value = ((Double) r.getAttribute(shape1 + "LL[" + f1 + "]")) *
                ((Double) r.getAttribute(shape2 + "LL[" + f2 + "]"));
            String name = String.format("%s%sLL[%s,%s]", shape1.charAt(0), shape2.charAt(0), f1, f2);
            r.setAttribute(name, value);
          }
        }
      }
    }
  }
}
