/*  ContextNormalizedFeatureExtractor.java

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
 * <p/>
 * This operation requires a ContextDesc object to describe the context.
 * <p/>
 * Extracts 2 or 3 features depending on if the attribute is a Doubles or a list of TimeValuePairs.
 * <p/>
 * If the attribute is a Double, zscore and range normalization of the value based on the context.
 * <p/>
 * If the attribute is a list of TimeValuePairs, zscore normalization of the min, max and mean within the current word
 * based on the context are calculated.
 * <p/>
 * TODO: This FeatureExtractor should be divided into two feature extractors, one for contour features and another for
 * Doubles.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.ContextDesc
 */
public class ContextNormalizedFeatureExtractor extends FeatureExtractor {
  public static final String moniker =
      "zMinWordContext,zMaxWordContext,zMeanWordContext,zNormWordContext,rNormWordContext";

  private static final Double EPSILON = 0.00001;  // values less than this are considered zero for normalization
  private String attribute_name;                 // the feature to normalize
  private ContextDesc context;                   // the normalization context

  /**
   * Constructs a ContextNormalizedFeatureExtractor
   *
   * @param attribute_name the attribute to analyze
   * @param context        the normalization context
   */
  public ContextNormalizedFeatureExtractor(String attribute_name, ContextDesc context) {
    super();
    this.attribute_name = attribute_name;

    this.context = context;

    extracted_features.add("zMinWordContext[" + attribute_name + "," + context.getLabel() + "]");
    extracted_features.add("zMaxWordContext[" + attribute_name + "," + context.getLabel() + "]");
    extracted_features.add("zMeanWordContext[" + attribute_name + "," + context.getLabel() + "]");
    extracted_features.add("zNormWordContext[" + attribute_name + "," + context.getLabel() + "]");
    extracted_features.add("rNormWordContext[" + attribute_name + "," + context.getLabel() + "]");
  }

  /**
   * Constructs a ContextNormalizedFeatureExtractor
   *
   * @param attribute_name the attribute to analyze
   * @param context_desc   the normalization context descriptor
   */
  public ContextNormalizedFeatureExtractor(String attribute_name, String context_desc) throws AuToBIException {
    super();
    this.attribute_name = attribute_name;

    this.context = ContextDesc.parseContextDescriptor(context_desc);

    extracted_features.add("zMinWordContext[" + attribute_name + "," + context_desc + "]");
    extracted_features.add("zMaxWordContext[" + attribute_name + "," + context_desc + "]");
    extracted_features.add("zMeanWordContext[" + attribute_name + "," + context_desc + "]");
    extracted_features.add("zNormWordContext[" + attribute_name + "," + context_desc + "]");
    extracted_features.add("rNormWordContext[" + attribute_name + "," + context_desc + "]");
  }

  /**
   * Extracts features over a list of regions.
   *
   * @param regions the list of data points
   * @throws FeatureExtractorException if there is a problem.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Don't differentiate context regions and feature regions
    extractFeatures(regions, regions);
  }

  /**
   * Extracts features from a list of regions with a distinct list of regions to describe the context.
   * <p/>
   * This version of the function decouples the extraction of the value to be normalized from the normalization context.
   * This is helpful when performing context normalization of subregions.
   *
   * @param regions         The regions to extract features for normalization
   * @param context_regions The regions to calculate normalization parameters from
   * @throws FeatureExtractorException if something goes wrong
   */
  public void extractFeatures(List regions, List<Region> context_regions) throws FeatureExtractorException {
    if (regions.size() != context_regions.size()) {
      throw new FeatureExtractorException("Regions and Context Regions must be the same size");
    }
    if (regions.isEmpty()) return;
    if (((Region) regions.get(0)).getAttribute(attribute_name) instanceof Contour) {
      for (int i = 0; i < regions.size(); ++i) {
        Region r = (Region) regions.get(i);

        int prev_idx = Math.max(0, i - context.getBack());
        int next_idx = Math.min(regions.size() - 1, i + context.getBack());

        extractContextNormAttributes(r, context_regions.get(prev_idx).getStart(),
            context_regions.get(next_idx).getEnd());
      }
    } else if (((Region) regions.get(0)).getAttribute(attribute_name) instanceof Double) {
      ContextFrame window = new ContextFrame(context_regions, attribute_name, context.getBack(), context.getForward());
      for (Object o : regions) {
        Region r = (Region) o;

        extractContextNormAttributes(r, window);
        window.increment();
      }
    }
  }

  /**
   * Extract context normalized attributes for a single region and context defined by time.
   *
   * @param r     The data point
   * @param start Start time
   * @param end   End time
   */
  private void extractContextNormAttributes(Region r, Double start, Double end) throws FeatureExtractorException {

    if (!r.hasAttribute(attribute_name)) {
      return;
    }
    // Generate context normalizing statistics
    Contour c = (Contour) r.getAttribute(attribute_name);
    Contour context_c;
    try {
      context_c = ContourUtils.getSubContour(c, start, end);
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }

    Aggregation agg = new Aggregation();
    for (Pair<Double, Double> tvp : context_c) {
      agg.insert(tvp.second);
    }
    Double max = agg.getMax();
    Double min = agg.getMin();
    Double mean = agg.getMean();
    Double stdev = agg.getStdev();

    // Calculate normalized features
    String context_feature_stem = attribute_name + "," + context.getLabel();

    if (r.getAttribute(attribute_name) instanceof Double) {
      Double value = (Double) r.getAttribute(attribute_name);
      // Z Score
      if (Math.abs(stdev) > EPSILON) {
        r.setAttribute("zNormWordContext[" + context_feature_stem + "]", (value - mean) / stdev);
      }
      // Range Normalization
      if ((max - min) > EPSILON) {
        r.setAttribute("rNormWordContext[" + context_feature_stem + "]", (value - min) / (max - min));
      }
    } else if (r.getAttribute(attribute_name) instanceof Contour) {
      // Calculate Z Score normalization
      if (Math.abs(stdev) > EPSILON) {
        if (r.hasAttribute("min[" + attribute_name + "]")) {
          r.setAttribute("zMinWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("min[" + attribute_name + "]") - mean) / stdev);
        }
        if (r.hasAttribute("max[" + attribute_name + "]")) {
          r.setAttribute("zMaxWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("max[" + attribute_name + "]") - mean) / stdev);
        }
        if (r.hasAttribute("mean[" + attribute_name + "]")) {
          r.setAttribute("zMeanWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("mean[" + attribute_name + "]") - mean) / stdev);
        }
      }
    }
  }

  /**
   * Extract context normalized attributes for a single data point.
   *
   * @param r      The data point
   * @param window The current context window
   */
  private void extractContextNormAttributes(Region r, ContextFrame window) {
    Double max = window.getMax();
    Double min = window.getMin();
    Double mean = window.getMean();
    Double stdev = window.getStdev();

    String context_feature_stem = attribute_name + "," + context.getLabel();

    if (r.getAttribute(attribute_name) instanceof Double) {
      Double value = (Double) r.getAttribute(attribute_name);
      // Z Score
      if (Math.abs(stdev) > EPSILON) {
        r.setAttribute("zNormWordContext[" + context_feature_stem + "]", (value - mean) / stdev);
      }
      // Range Normalization
      if ((max - min) > EPSILON) {
        r.setAttribute("rNormWordContext[" + context_feature_stem + "]", (value - min) / (max - min));
      }
    } else if (r.getAttribute(attribute_name) instanceof Contour) {
      // Calculate Z Score normalization
      if (Math.abs(stdev) > EPSILON) {
        if (r.hasAttribute("min[" + attribute_name + "]")) {
          r.setAttribute("zMinWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("min[" + attribute_name + "]") - mean) / stdev);
        }
        if (r.hasAttribute("max[" + attribute_name + "]")) {
          r.setAttribute("zMaxWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("max[" + attribute_name + "]") - mean) / stdev);
        }
        if (r.hasAttribute("mean[" + attribute_name + "]")) {
          r.setAttribute("zMeanWordContext[" + context_feature_stem + "]", (
              (Double) r.getAttribute("mean[" + attribute_name + "]") - mean) / stdev);
        }
      }
    }
  }
}
