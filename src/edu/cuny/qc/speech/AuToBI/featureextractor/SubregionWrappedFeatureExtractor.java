/*  SubregionWrappedFeatureExtractor.java

    Copyright 2009-2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.SubregionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SubregionWrappedFeatureExtractor applies any wrapped FeatureExtractor over a predefined subregion.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.FeatureExtractor
 */
@SuppressWarnings("unchecked")
@Deprecated
public class SubregionWrappedFeatureExtractor extends FeatureExtractor {

  private FeatureExtractor fe;  // the Wrapped feature extractor
  private String subregion_attribute;
  // a descriptor of the feature containing the subregion object to be analysed

  /**
   * Constructs a new SubregionTimeValuePairFeatureExtractor.
   *
   * @param subregion_attribute the name of the attribute containing the subregion
   */
  public SubregionWrappedFeatureExtractor(FeatureExtractor fe, String subregion_attribute) {
    this.subregion_attribute = subregion_attribute;
    this.fe = fe;

    extracted_features = new ArrayList<String>();
    for (String feature : fe.getExtractedFeatures()) {
      extracted_features.add(feature + "_" + subregion_attribute);
    }
    this.required_features.addAll(fe.getRequiredFeatures());
    this.required_features.add(subregion_attribute);
  }

  /**
   * Extracts time value pair features from subregions of each region.
   * <p/>
   * A subregion object must be assigned to each region prior to processing.
   *
   * @param regions the regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if somethign goes wrong -- no
   *                                                                              subregion feature is assigned or a
   *                                                                              problem with the tvpfe.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Construct a list of subregions.
    List<Region> subregions = new ArrayList<Region>();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(subregion_attribute)) {
        subregions.add((Region) r.getAttribute(subregion_attribute));
      } else {
        throw new FeatureExtractorException(
            "Region, " + r + ", does not have a valid subregion feature, " + subregion_attribute);
      }
    }

    // if Feature Extractor needs an attribute, copy it to the subregions.
    for (String f : fe.getRequiredFeatures()) {
      SubregionUtils.assignFeatureToSubregions(regions, subregion_attribute, f);
    }

    fe.extractFeatures(subregions);

    // Copy extracted features from subregions to regions
    for (int i = 0; i < regions.size(); ++i) {
      Region r = (Region) regions.get(i);
      Region subregion = subregions.get(i);
      for (String feature : fe.getExtractedFeatures()) {
        r.setAttribute(feature + "_" + subregion_attribute, subregion.getAttribute(feature));
      }
    }
  }
}