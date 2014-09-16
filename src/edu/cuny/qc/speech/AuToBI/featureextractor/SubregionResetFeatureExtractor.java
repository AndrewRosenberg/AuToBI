/*  SubregionResetFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.SubregionUtils;

import java.util.List;

/**
 * SubregionResetFeatureExtractor extracts reset features from subregions.
 * <p/>
 * The standard ResetContourFeatureExtractor extract reset features from the full regions.  This extends this
 * functionality to identify reset over narrower regions.
 * <p/>
 * While developed as a utility for subregions, this can operate on regions longer than the initial region.  For
 * example, if the initial region is 100ms, and a 200ms subregion is requested, the subregion boundary will fall earlier
 * than the initial region boundary
 */
@SuppressWarnings("unchecked")
public class SubregionResetFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "van,trail";
  private String subregion_name;    // the name of the subregion
  private Double subregion_length;  // the length (in seconds) of the subregion

  /**
   * Constructs a new SubregionResetFeatureExtractor.
   * <p/>
   * Assigns a contour, subregion name and the name of the acoustic feature.
   * <p/>
   * Subregions are described as a number of seconds "2s" or milliseconds "50ms".  Other names will raise an exception
   *
   * @param subregion_name the subregion label
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if something goes wrong with the
   *                                                                              subregion name
   */
  public SubregionResetFeatureExtractor(String subregion_name) throws FeatureExtractorException {
    extracted_features.add("van[" + subregion_name + "]");
    extracted_features.add("trail[" + subregion_name + "]");

    this.subregion_name = subregion_name;
    this.subregion_length = SubregionUtils.parseSubregionName(subregion_name);
  }

  /**
   * Extracts subregion reset regions for each region.
   *
   * @param regions the regions to process
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if something goes wrong with the
   *                                                                              extraction.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      // construct subregions
      Region trail_region = new Region(r.getStart(), r.getStart() + subregion_length);
      Region van_region = new Region(r.getEnd() - subregion_length, r.getEnd());

      r.setAttribute("van[" + subregion_name + "]", van_region);
      r.setAttribute("trail[" + subregion_name + "]", trail_region);
    }
  }
}
