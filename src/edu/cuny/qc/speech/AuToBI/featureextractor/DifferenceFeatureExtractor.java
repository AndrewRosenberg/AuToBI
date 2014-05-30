/*  DifferenceFeatureExtractor.java

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
 * Calculates the difference of a previously extracted numeric feature on each region and the following region.
 * <p/>
 * Note: when using this feature extractor, it is assumed that the list of regions are properly ordered.
 */
public class DifferenceFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "diff";
  private String feature;

  /**
   * Constructs a DifferenceFeatureExtractor.
   * <p/>
   * Deprecated: There is no need for this functionality under the v1.4 feature registration.
   * <p/>
   * Currently will only process the first element in the difference_features list.
   *
   * @param difference_features the features to process
   */
  @Deprecated
  public DifferenceFeatureExtractor(List<String> difference_features) {
    super();
    this.feature = difference_features.get(0);

    extracted_features.add("diff[" + feature + "]");
    required_features.add(feature);
  }

  public DifferenceFeatureExtractor(String f) {
    super();
    this.feature = f;

    extracted_features.add("diff[" + f + "]");
    required_features.add(f);
  }

  /**
   * Extracts difference features.
   *
   * @param regions the regions to extract features on
   * @throws FeatureExtractorException if the feature hasn't been set, or if it is not a numeric value.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (int i = 0; i < regions.size() - 1; ++i) {
      Region r = (Region) regions.get(i);
      Region next_r = (Region) regions.get(i + 1);

      if (r.hasAttribute(feature) && next_r.hasAttribute(feature)) {

        if (!(r.getAttribute(feature) instanceof Number)) {
          throw new FeatureExtractorException(
              "Cannot calculate difference. Feature, " + feature + ", is not a Number on region, " + r);
        }
        if (!(next_r.getAttribute(feature) instanceof Number)) {
          throw new FeatureExtractorException(
              "Cannot calculate difference. Feature, " + feature + ", is not a Number on region, " + next_r);
        }

        Number value =
            ((Number) next_r.getAttribute(feature)).doubleValue() - ((Number) r.getAttribute(feature)).doubleValue();
        r.setAttribute("diff[" + feature + "]", value);
      }
    }
  }
}
