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
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * A feature extractor to calculate context normalized aggregations of Doubles or lists of TimeValuePairs.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.ContextDesc
 */
public class TemporalContextNormalizedFeatureExtractor extends FeatureExtractor {

  private static final Double EPSILON = 0.00001;  // values less than this are considered zero for normalization
  private String attribute_name;                  // the feature to normalize
  private int prev_context;                       // the normalization context
  private int foll_context;
  private final String context_label;  // A name for the context.

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

    this.prev_context = prev;
    this.foll_context = foll;

    this.context_label = prev + "ms_" + foll + "ms";

    String context_feature_prefix = this.attribute_name + "_" + context_label;
    extracted_features.add(context_feature_prefix + "__zMin");
    extracted_features.add(context_feature_prefix + "__zMax");
    extracted_features.add(context_feature_prefix + "__zMean");

    this.required_features.add(attribute_name);
    this.required_features.add(attribute_name + "__min");
    this.required_features.add(attribute_name + "__max");
    this.required_features.add(attribute_name + "__mean");
  }

  /**
   * Extracts features over a list of regions.
   *
   * @param regions the list of data points
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if there is a problem.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      extractContextNormAttributes(r, r.getStart() - prev_context / 1000., r.getEnd() + foll_context / 1000.);
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

    String context_feature_prefix = attribute_name + "_" + context_label;
    String stored_feature_prefix = attribute_name + "__";

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
          if (r.hasAttribute(stored_feature_prefix + "min")) {
            r.setAttribute(context_feature_prefix + "__zMin", (
                (Double) r.getAttribute(stored_feature_prefix + "min") - mean) / stdev);
          }
          if (r.hasAttribute(stored_feature_prefix + "max")) {
            r.setAttribute(context_feature_prefix + "__zMax", (
                (Double) r.getAttribute(stored_feature_prefix + "max") - mean) / stdev);
          }
          if (r.hasAttribute(stored_feature_prefix + "mean")) {
            r.setAttribute(context_feature_prefix + "__zMean", (
                (Double) r.getAttribute(stored_feature_prefix + "mean") - mean) / stdev);
          }
        }
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
    }
  }
}
