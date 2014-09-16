/*  ContourFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * ContourExtractor extracts a set of standard aggregations from a Contour.
 * <p/>
 * These include, mean, min, max, standard deviation, the z-score of the maximum, and the relative location of the
 * min and max.
 * <p/>
 * (The name of this class could stand to be changed.  It was named when there were relatively few features
 * extracted over a contour.  Now there are many.)
 */
public class ContourFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "mean,max,min,stdev,zMax,maxLocation,minLocation";

  protected String attribute_name;  // the name of the feature name to analyze

  /**
   * Constructs a new ContourFeatureExtractor with associated values and attribute name.
   * <p/>
   * The attribute name often matches the the stored SpeakerNormalizationParameter identifiers, e.g. "f0"
   *
   * @param attribute_name the attribute_name
   */
  public ContourFeatureExtractor(String attribute_name) {
    setAttributeName(attribute_name);
  }

  /**
   * Constructs a new empty ContourFeatureExtractor.
   */
  public ContourFeatureExtractor() {
  }

  /**
   * Sets the attribute name and registers extracted features to match the new name.
   *
   * @param attribute_name the new attribute name
   */
  public void setAttributeName(String attribute_name) {
    this.attribute_name = attribute_name;

    extracted_features = new ArrayList<String>();
    extracted_features.add("max[" + this.attribute_name + "]");
    extracted_features.add("min[" + this.attribute_name + "]");
    extracted_features.add("mean[" + this.attribute_name + "]");
    extracted_features.add("stdev[" + this.attribute_name + "]");
    extracted_features.add("zMax[" + this.attribute_name + "]");
    extracted_features.add("maxLocation[" + this.attribute_name + "]");
    extracted_features.add("maxRelLocation[" + this.attribute_name + "]");

    required_features.add(this.attribute_name);
  }

  /**
   * Retrieves the attribute name.
   *
   * @return the attribute name
   */
  public String getAttributeName() {
    return attribute_name;
  }

  /**
   * Extracts acoustic aggregations of the associated contour for each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region region : (List<Region>) regions) {
      try {
        extractFeatures(region);
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }

  /**
   * Extract features from a single region.
   *
   * @param region the region
   */
  private void extractFeatures(Region region) throws AuToBIException {
    if (!region.hasAttribute(attribute_name)) {
      AuToBIUtils.warn("region doesn't have attribute: " + attribute_name);
      return;
    }
    Contour contour =
        ContourUtils.getSubContour((Contour) region.getAttribute(attribute_name), region.getStart(), region.getEnd());
    double max_location = region.getStart();
    Aggregation agg = new Aggregation();
    for (Pair<Double, Double> tvp : contour) {
      if (tvp.second > agg.getMax()) {
        max_location = tvp.first;
      }
      agg.insert(tvp.second);
    }

    double mean = agg.getMean();
    double stdev = agg.getStdev();
    double duration = region.getDuration();

    if (agg.getSize() > 0) {
      region.setAttribute("max[" + attribute_name + "]", agg.getMax());
      region.setAttribute("min[" + attribute_name + "]", agg.getMin());
      region.setAttribute("mean[" + attribute_name + "]", mean);
      if (Double.isNaN(stdev)) {
        region.setAttribute("stdev[" + attribute_name + "]", 0.0);
      } else {
        region.setAttribute("stdev[" + attribute_name + "]", stdev);
      }
      if (stdev == 0.0) {
        region.setAttribute("zMax[" + attribute_name + "]", 0.0);
      } else {
        region.setAttribute("zMax[" + attribute_name + "]", (agg.getMax() - mean) / stdev);
      }


      max_location -= region.getStart();
      max_location = Math.min(Math.max(max_location, 0.0), duration);
      region.setAttribute("maxLocation[" + attribute_name + "]", max_location);
      region.setAttribute("maxRelLocation[" + attribute_name + "]", max_location / duration);
    } else {
      region.setAttribute("max[" + attribute_name + "]", 0.0);
      region.setAttribute("min[" + attribute_name + "]", 0.0);
      region.setAttribute("mean[" + attribute_name + "]", 0.0);
      region.setAttribute("stdev[" + attribute_name + "]", 0.0);
      region.setAttribute("zMax[" + attribute_name + "]", 0.0);

      region.setAttribute("maxLocation[" + attribute_name + "]", 0.0);
      region.setAttribute("maxRelLocation[" + attribute_name + "]", 0.0);
    }
  }
}
