/*  TemporalContextNormalizedFeatureExtractor.java

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

/**
 * A feature extractor to calculate context normalized aggregations of Doubles or lists of TimeValuePairs.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.ContextDesc
 */
public class TemporalContextNormalizedFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "zMinTimeContext,zMaxTimeContext,zMeanTimeContext";

  private static final Double EPSILON = 0.00001;  // values less than this are considered zero for normalization
  private String attribute_name;                  // the feature to normalize
  private int prev;                       // the normalization context in milliseconds (ms)
  private int foll;

  /**
   * Constructs a ContextNormalizedFeatureExtractor from String arguments.
   * @param attribute_name the attribute to analyze
   * @param prev the ms previous to include (as a string)
   * @param foll the ms following to include (as a string)
   */
  public TemporalContextNormalizedFeatureExtractor(String attribute_name, String prev, String foll) {
    this(attribute_name, Integer.parseInt(prev), Integer.parseInt(foll));
  }

  /**
   * Constructs a ContextNormalizedFeatureExtractor
   *
   * @param attribute_name the attribute to analyze
   * @param prev           the ms previous to include
   * @param foll           the ms following to include
   */
  public TemporalContextNormalizedFeatureExtractor(String attribute_name, int prev, int foll) {
    super();
    this.attribute_name = attribute_name;

    this.prev = prev;
    this.foll = foll;

    extracted_features.add("zMinTimeContext[" + attribute_name + "," + prev + "," + foll + "]");
    extracted_features.add("zMaxTimeContext[" + attribute_name + "," + prev + "," + foll + "]");
    extracted_features.add("zMeanTimeContext[" + attribute_name + "," + prev + "," + foll + "]");

    this.required_features.add(attribute_name);
    this.required_features.add("min[" + attribute_name + "]");
    this.required_features.add("max[" + attribute_name + "]");
    this.required_features.add("mean[" + attribute_name + "]");
  }

  /**
   * Extracts features over a list of regions.
   *
   * @param regions the list of data points
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if there is a problem.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      extractContextNormAttributes(r, r.getStart() - prev / 1000., r.getEnd() + foll / 1000.);
    }
  }

  /**
   * Extract context normalized attributes for a single data point.
   *
   * @param r     The data point
   * @param start starting time
   * @param end   ending time
   */
  private void extractContextNormAttributes(Region r, Double start, Double end) throws FeatureExtractorException {
    Object attr = r.getAttribute(attribute_name);

    if (attr instanceof Contour) {
      Contour c = (Contour) attr;
      try {
        Contour sub_c = ContourUtils.getSubContour(c, start, end);
        if (sub_c == null) {
          return;
        }
        Aggregation agg = new Aggregation();
        for (Pair<Double, Double> p : sub_c) {
          agg.insert(p.second);
        }
        Double mean = agg.getMean();
        Double stdev = agg.getStdev();

        // Calculate Z Score normalization
        if (Math.abs(stdev) > EPSILON) {
          if (r.hasAttribute("min[" + attribute_name + "]")) {
            r.setAttribute("zMinTimeContext[" + attribute_name + "," + prev + "," + foll + "]", (
                (Double) r.getAttribute("min[" + attribute_name + "]") - mean) / stdev);
          }
          if (r.hasAttribute("max[" + attribute_name + "]")) {
            r.setAttribute("zMaxTimeContext[" + attribute_name + "," + prev + "," + foll + "]", (
                (Double) r.getAttribute("max[" + attribute_name + "]") - mean) / stdev);
          }
          if (r.hasAttribute("mean[" + attribute_name + "]")) {
            r.setAttribute("zMeanTimeContext[" + attribute_name + "," + prev + "," + foll + "]", (
                (Double) r.getAttribute("mean[" + attribute_name + "]") - mean) / stdev);
          }
        }
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}
