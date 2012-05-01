/*  ContextNormalizedFeatureExtractor.java

    Copyright (c) 2009-2010 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.List;
import java.util.ArrayList;

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

    String context_feature_prefix = this.attribute_name + "_" + context.getLabel();
    extracted_features.add(context_feature_prefix + "__zMin");
    extracted_features.add(context_feature_prefix + "__zMax");
    extracted_features.add(context_feature_prefix + "__zMean");
    extracted_features.add(context_feature_prefix + "__zNorm");
    extracted_features.add(context_feature_prefix + "__rNorm");
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
  public void extractFeatures(List regions, List context_regions) throws FeatureExtractorException {
    ContextFrame window = new ContextFrame(context_regions, attribute_name, context.getBack(), context.getForward());
    for (Object o : regions) {
      Region r = (Region) o;
      extractContextNormAttributes(r, window);
      window.increment();
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

    String context_feature_prefix = attribute_name + "_" + context.getLabel();
    String stored_feature_prefix = attribute_name + "__";

    if (r.getAttribute(attribute_name) instanceof Double) {
      Double value = (Double) r.getAttribute(attribute_name);
      // Z Score
      if (Math.abs(stdev) > EPSILON) {
        r.setAttribute(context_feature_prefix + "__zNorm", (value - mean) / stdev);
      }
      // Range Normalization
      if ((max - min) > EPSILON) {
        r.setAttribute(context_feature_prefix + "__rNorm", (value - min) / (max - min));
      }
    } else if (r.getAttribute(attribute_name) instanceof Contour) {
      // Calculate Z Score normalization
      if (Math.abs(stdev) > EPSILON) {
        if (r.hasAttribute(stored_feature_prefix + "min"))
          r.setAttribute(context_feature_prefix + "__zMin", (
              (Double) r.getAttribute(stored_feature_prefix + "min") - mean) / stdev);
        if (r.hasAttribute(stored_feature_prefix + "max"))
          r.setAttribute(context_feature_prefix + "__zMax", (
              (Double) r.getAttribute(stored_feature_prefix + "max") - mean) / stdev);
        if (r.hasAttribute(stored_feature_prefix + "mean"))
          r.setAttribute(context_feature_prefix + "__zMean", (
              (Double) r.getAttribute(stored_feature_prefix + "mean") - mean) / stdev);
      }
    }
  }
}
