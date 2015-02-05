/*  ResetTimeValuePairFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * ResetContourFeatureExtractor identifes the change of a contour across region boundaries.
 * <p/>
 * This feature extractor assumes the presence of previously extracted subregions identifying the region of analysis to
 * calculate the amount of change across the boundary.
 * <p/>
 * The reset feature calculates the difference between the mean value of a current region and the mean value of another.
 * This can be calculated over the full region, or a previously identified subregion that is stored as an
 * "van_subregion" (right-most), "trail_subregion" (left-most) attribute on subsequent regions.
 *
 * @see SubregionResetFeatureExtractor
 */
@SuppressWarnings("unchecked")
public class ResetContourFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "reset";
  private String feature_name;       // the prefix of the stored feature name
  private String subregion_name;     // the name of the subregion

  /**
   * Constructs a new ResetContourFeatureExtractor.
   * <p/>
   * The reset feature is stored in "<feature_name>_<subregion_name>_reset" or "<feature_name>_reset" if no subregion is
   * specified.
   *
   * @param feature_name   the prefix of the stored feature name
   * @param subregion_name the name of the subregion feature
   */
  public ResetContourFeatureExtractor(String feature_name, String subregion_name) {
    super();
    this.feature_name = feature_name;
    this.subregion_name = subregion_name;

    if (subregion_name != null && subregion_name.length() > 0) {
      this.extracted_features.add(moniker + "[" + feature_name + "," + subregion_name + "]");
    } else {
      this.extracted_features.add(moniker + "[" + feature_name + "]");
    }

    this.required_features.add(feature_name);

    if (subregion_name != null && !subregion_name.equals("")) {
      this.required_features.add("van[" + subregion_name + "]");
      this.required_features.add("trail[" + subregion_name + "]");
    }
  }

  /**
   * Default ResetContourFeatureExtractor with no subregion.
   * <p/>
   * The reset feature is stored in "reset[feature_name]"
   *
   * @param feature_name The prefix of the stored feature name
   */
  public ResetContourFeatureExtractor(String feature_name) {
    this(feature_name, null);
  }

  /**
   * Extracts reset feature over the contour for each region.
   * <p/>
   * If the subregion name is null or an empty string, the full region will be used as the domain to calculate reset
   * over.
   *
   * @param regions The regions to analyze.
   * @throws FeatureExtractorException if any region doesn't have a valid subregion.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    List<Region> van_subregions;
    List<Region> trail_subregions;

    String destination_feature = moniker + "[" + feature_name + "]";
    if (subregion_name != null && !subregion_name.equals("")) {
      destination_feature = moniker + "[" + feature_name + "," + subregion_name + "]";

      van_subregions = new ArrayList<Region>();
      trail_subregions = new ArrayList<Region>();
      for (Region r : (List<Region>) regions) {
        String van_subregion_name = "van[" + subregion_name + "]";
        String trail_subregion_name = "trail[" + subregion_name + "]";
        if (r.hasAttribute(van_subregion_name)) {
          van_subregions.add((Region) r.getAttribute(van_subregion_name));
        } else {
          throw new FeatureExtractorException("Region, " + r + ", has no subregion: " + van_subregion_name);
        }

        if (r.hasAttribute(trail_subregion_name)) {
          trail_subregions.add((Region) r.getAttribute(trail_subregion_name));
        } else {
          throw new FeatureExtractorException("Region, " + r + ", has no subregion: " + trail_subregion_name);
        }
      }

      try {
        ContourUtils.assignValuesToSubregions(van_subregions, regions, feature_name);
        ContourUtils.assignValuesToSubregions(trail_subregions, regions, feature_name);
      } catch (AuToBIException e) {
        e.printStackTrace();
        return;
      }
    } else {
      van_subregions = regions;
      trail_subregions = regions;
    }

    for (int i = 0; i < regions.size() - 1; ++i) {
      Region van = van_subregions.get(i);
      Region trail = trail_subregions.get(i + 1);

      Aggregation van_agg = new Aggregation();
      Aggregation trail_agg = new Aggregation();

      for (Pair<Double, Double> tvp : (Contour) van.getAttribute(feature_name)) {
        van_agg.insert(tvp.second);
      }
      for (Pair<Double, Double> tvp : (Contour) trail.getAttribute(feature_name)) {
        trail_agg.insert(tvp.second);
      }

      ((Region) regions.get(i))
          .setAttribute(destination_feature, trail_agg.getMean() - van_agg.getMean());
    }
  }
}
