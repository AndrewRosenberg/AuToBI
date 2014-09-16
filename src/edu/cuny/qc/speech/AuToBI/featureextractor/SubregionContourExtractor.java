/*  SubregionContourExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Constructs a subcontour object based on an acoustic contour and a defined subregion.
 */
@SuppressWarnings("unchecked")
public class SubregionContourExtractor extends FeatureExtractor {
  public static final String moniker = "subregionC";

  private String contour_feature; // the acoustic contour feature
  private String subregion_feature; // the subregion feature

  /**
   * Constructs a new SubregionContourExtractor.
   * <p/>
   * Extracts a new subcontour feature called "feature_name"_"subregion_feature" which is a subcontour of the feature
   * name extracted from the region defined by subregion.
   *
   * @param contour_feature   the acoustic feature name
   * @param subregion_feature the subregion
   */
  public SubregionContourExtractor(String contour_feature, String subregion_feature) {
    this.contour_feature = contour_feature;
    this.subregion_feature = subregion_feature;
    required_features.add(contour_feature);
    required_features.add(subregion_feature);

    extracted_features.add(moniker + "[" + contour_feature + "," + subregion_feature + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      Contour c = (Contour) r.getAttribute(contour_feature);
      if (c != null && r.hasAttribute(subregion_feature)) {
        Region subregion = (Region) r.getAttribute(subregion_feature);
        try {
          Contour subcontour = ContourUtils.getSubContour(c, subregion.getStart(), subregion.getEnd());
          r.setAttribute(moniker + "[" + contour_feature + "," + subregion_feature + "]", subcontour);
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
    }
  }
}
