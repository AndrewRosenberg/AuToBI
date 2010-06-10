/*  NormalizedFeatureExtractor.java

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
package edu.cuny.qc.speech.AuToBI;

import java.util.List;
import java.util.ArrayList;

/**
 * NormalizedFeatureExtractor extracts noramlized time value pair features.
 * <p/>
 * This is accomplished by first normalizing the values, and then using a TimeValuePairFeatureExtractor as normal.
 *
 * @see TimeValuePairFeatureExtractor
 */
public class NormalizedFeatureExtractor extends TimeValuePairFeatureExtractor {

  // The name of the feature to normalize.  This must match the label in the speaker normalization parameter
  // that is responsible for the normalization parameters.
  private String base_feature_name;
  private SpeakerNormalizationParameter norm_params;  // Normalization parameters
  private List<TimeValuePair> normalized_values;      // Normalized time value pairs
  private TimeValuePairFeatureExtractor tvpfe;        // A TimeValuePairFeatureExtractor to perform the extraction

  /**
   * Constructs a new NormalizedFeatureExtractor.
   *
   * @param tvpfe             an associated TimeValuePairFeatureExtractor to extract features.
   * @param norm_params       the normalization parameters to normalize the contour.
   * @param base_feature_name The name of the controu.
   */
  public NormalizedFeatureExtractor(TimeValuePairFeatureExtractor tvpfe,
                                    SpeakerNormalizationParameter norm_params, String base_feature_name) {
    this.base_feature_name = base_feature_name;
    this.values = tvpfe.getValues();
    this.norm_params = norm_params;
    this.normalized_values = null;
    this.attribute_name = tvpfe.getAttributeName();  // The base feature name e.g. "f0"
    this.tvpfe = tvpfe;
    this.tvpfe.setAttributeName(this.tvpfe.getAttributeName() + "_norm");
    this.extracted_features.addAll(this.tvpfe.extracted_features);
  }

  /**
   * Normalizes the values associated with the connected TimeValuePairFeatureExtractor, and then extracts
   * time value pair features.
   *
   * @param regions The regions to extract features from
   * @throws FeatureExtractorException if something goes wrong with the feature extraction
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    if (norm_params != null && normalized_values == null && norm_params.canNormalize(base_feature_name)) {
      normalized_values = zScoreNormalizeValues(values, norm_params);
    }

    tvpfe.setValues(normalized_values);
    tvpfe.extractFeatures(regions);
  }

  /**
   * Performs z-score, (x-mean) / stdev, normalization of the supplied values, using the parameters in norm_params.
   *
   * @param values      The values to normalize
   * @param norm_params the normalization parameters
   * @return a normalized list of time value pairs
   */
  private List<TimeValuePair> zScoreNormalizeValues(List<TimeValuePair> values,
                                                    SpeakerNormalizationParameter norm_params) {
    List<TimeValuePair> normalized_values = new ArrayList<TimeValuePair>();

    for (TimeValuePair tvp : values) {
      normalized_values.add(new TimeValuePair(tvp.getTime(), norm_params.normalize(base_feature_name, tvp.getValue())));
    }
    return normalized_values;
  }
}
