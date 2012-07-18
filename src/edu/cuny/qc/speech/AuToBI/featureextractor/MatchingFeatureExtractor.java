/*  MatchingFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.List;

/**
 * MatchingFeatureExtractor extracts a feature that determines if two other features match, or differ.
 * <p/>
 * Currently, if two variables match the resulting feature value is "CORRECT" and if they differ it is "INCORRECT". This
 * decision was made because this feature extractor is typicaly used to compare predicted values to hypothesized values.
 * A reasonable extension to this will be to allow a user to specify the values for matching and differing feature
 * values.
 */
@SuppressWarnings("unchecked")
public class MatchingFeatureExtractor extends FeatureExtractor {
  private String feature1;     // One of the feature names
  private String feature2;     // The second feature name
  private String destination;  // Name for the destination variable.

  /**
   * Constructs a new MatchingFeatureExtractor to compare the values of two features, feature1, and feature2.
   * <p/>
   * The result "CORRECT" or "INCORRECT" is stored in a feature indicated by destination_feature.
   *
   * @param feature1            The first feature
   * @param feature2            The second feature
   * @param destination_feature The feature to store the result
   */
  public MatchingFeatureExtractor(String feature1, String feature2, String destination_feature) {
    super();
    this.feature1 = feature1;
    this.feature2 = feature2;
    this.destination = destination_feature;
    required_features.add(feature1);
    required_features.add(feature2);
    extracted_features.add(destination_feature);
  }

  /**
   * Extracts matching feature for each region.
   * <p/>
   * If the two features are equal the destination feature is "CORRECT" otherwise "INCORRECT"
   * <p/>
   * No destination feature is set if either feature is not present on a region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if any region does not have one of the matching features.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature1) && r.hasAttribute(feature2)) {
        if (r.getAttribute(feature1).equals(r.getAttribute(feature2))) {
          r.setAttribute(destination, "CORRECT");
        } else {
          r.setAttribute(destination, "INCORRECT");
        }
      }
    }
  }
}
