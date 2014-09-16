/*  DeltaTimeValuePairFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.HashMap;
import java.util.List;

/**
 * DeltaContourPairFeatureExtractor extracts the first order differences of a Contour.
 *
 * @see Contour
 */
public class DeltaContourFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "delta";
  private String attribute_name;  // the attribute name to construct a delta contour from

  /**
   * Constructs a DeltaContourFeatureExtractor.
   *
   * @param attribute_name the base attribute name
   */
  public DeltaContourFeatureExtractor(String attribute_name) {
    setAttributeName(attribute_name);
  }

  /**
   * Set the attribute name.
   *
   * @param attribute_name the attribute name
   */
  public void setAttributeName(String attribute_name) {
    this.attribute_name = attribute_name;
    required_features.add(attribute_name);

    extracted_features.add("delta[" + attribute_name + "]");
  }

  /**
   * Calculates delta Contour features.
   * <p/>
   *
   * @param regions The regions to extract features from
   * @throws FeatureExtractorException When something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<Contour, Contour> cache = new HashMap<Contour, Contour>();

    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(attribute_name)) {
        Contour c = (Contour) r.getAttribute(attribute_name);
        if (cache.containsKey(c)) {
          r.setAttribute("delta[" + attribute_name + "]", cache.get(c));
        } else {
          Contour delta_contour = ContourUtils.generateDeltaContour(c);
          r.setAttribute("delta[" + attribute_name + "]", delta_contour);
          cache.put(c, delta_contour);
        }
      }
    }
  }

}
