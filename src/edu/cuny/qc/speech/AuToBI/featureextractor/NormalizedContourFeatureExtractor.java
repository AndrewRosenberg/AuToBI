/*  NormalizedTimeValuePairFeatureExtractor.java

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
 * NormalizedContourFeatureExtractor normalizes a contour represented by a list of time value pairs and aligns the
 * resulting normalized contour to the supplied regions.
 * <p/>
 * The normalization is performed using z-score normalization ((x-mean) / stdev) based on a supplied
 * SpeakerNormalizationParameter object.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter
 */
public class NormalizedContourFeatureExtractor extends FeatureExtractor {
  private String feature_name;  // the feature to analyze
  private String norm_feature;  // the parameters to run the normalization

  /**
   * Constructs a new NormalizedContourFeatureExtractor to analyze the supplied feature_name using the supplied
   * parameters
   *
   * @param feature_name          the feature to modify
   * @param normalization_feature the zscore normalization parameters to apply
   */
  public NormalizedContourFeatureExtractor(String feature_name, String normalization_feature) {
    this.feature_name = feature_name;
    this.norm_feature = normalization_feature;

    required_features.add(feature_name);
    required_features.add(normalization_feature);
    extracted_features.add("norm_" + feature_name);
  }

  /**
   * Performs z-score normalization on the specified contour, storing the result in a new list of TimeValuePairs
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if the normalization parameters cannot normalize the features
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature_name) && r.hasAttribute(norm_feature)) {
        SpeakerNormalizationParameter norm_params = (SpeakerNormalizationParameter) r.getAttribute(norm_feature);
        if (norm_params.canNormalize(feature_name)) {
          Contour norm_contour =
              ContourUtils.zScoreNormalizeContour((Contour) r.getAttribute(feature_name), norm_params, feature_name);
          r.setAttribute("norm_" + feature_name, norm_contour);
        } else {
          throw new FeatureExtractorException(
              "Supplied normalization parameters cannot normalize values: " + feature_name);
        }
      }
    }


  }
}
