/*  SubregionFeatureExtractor.java

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

import java.util.List;


/**
 * SubregionFeatureExtractor constructs "subregions" aligned with the end of the supplied regions and preceding
 * the ending boundary by a specified amount of time.
 * <p/>
 * While it is intended that the subregions are completely contained within the initial region, there is no enforcement
 * of this policy.  Thus subregions can in fact be longer than the initial region.  E.g. if the initial region is 100ms
 * and a 200ms region is requested the "subregion" is longer than the initial region.
 */
public class SubregionFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "subregion";
  private String subregion_name;    // the name of the subregion
  private Double subregion_length;  // the length in seconds of the subregion

  /**
   * Constructs a new SubregionFeatureExtractor
   *
   * @param subregion_name a descriptor of the subregion
   * @throws FeatureExtractorException if the subregion_name cannot be parsed.
   */
  public SubregionFeatureExtractor(String subregion_name) throws FeatureExtractorException {
    this.subregion_name = subregion_name;
    this.subregion_length = SubregionUtils.parseSubregionName(subregion_name);

    this.extracted_features.add(moniker + "[" + subregion_name + "]");
  }

  /**
   * Constructs "subregion" features for each region.
   *
   * @param regions The regions to extract features from.
   */
  public void extractFeatures(List regions) {
    for (Region r : (List<Region>) regions) {
      Region subregion = new Region(r.getEnd() - subregion_length, r.getEnd());

      r.setAttribute(moniker + "[" + subregion_name + "]", subregion);
    }
  }
}
