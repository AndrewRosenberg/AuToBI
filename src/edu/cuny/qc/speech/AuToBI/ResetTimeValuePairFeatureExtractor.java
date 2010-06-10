/*  ResetTimeValuePairFeatureExtractor.java

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
 * ResetTimeValuePairFeatureExtractor identifes the change of a contour across region boundaries.
 * <p/>
 * This feature extractor assumes the presence of previously extracted subregions identifying the region of analysis
 * to calculate the amount of change across the boundary.
 *
 * @see SubregionResetFeatureExtractor
 */
public class ResetTimeValuePairFeatureExtractor extends TimeValuePairFeatureExtractor {
  private List<TimeValuePair> values;  // the contour to analyze
  private String feature_prefix;       // the prefix of the stored feature name
  private String subregion_name;       // the name of the subregion

  /**
   * Constructs a new ResetTimeValuePairFeatureExtractor.
   * <p/>
   * The reset feature is stored in "<feature_prefix>_<subregion_name>_reset" or "<feature_prefix>_reset" if no
   * subregion is specified.
   *
   * @param values         the contour to analyze.
   * @param feature_prefix the prefix of the stored feature name
   * @param subregion_name the name of the subregion feature
   */
  public ResetTimeValuePairFeatureExtractor(List<TimeValuePair> values, String feature_prefix, String subregion_name) {
    super();
    this.values = values;
    this.feature_prefix = feature_prefix;
    this.subregion_name = subregion_name;

    if (subregion_name != null && subregion_name.length() > 0)
      this.extracted_features.add(feature_prefix + "_" + subregion_name + "_reset");
    else
      this.extracted_features.add(feature_prefix + "_reset");
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
    TimeValuePairUtils.assignValuesToOrderedRegions(regions, values, feature_prefix);

    List<Region> van_subregions;
    List<Region> trail_subregions;
    if (subregion_name != null || subregion_name != "") {
      van_subregions = new ArrayList<Region>();
      trail_subregions = new ArrayList<Region>();
      for (Region r : (List<Region>) regions) {
        String van_subregion_name = "van_" + subregion_name;
        String trail_subregion_name = "trail_" + subregion_name;
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
        TimeValuePairUtils.assignValuesToRegions(van_subregions, values, feature_prefix);
        TimeValuePairUtils.assignValuesToRegions(trail_subregions, values, feature_prefix);
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

      for (TimeValuePair tvp : (List<TimeValuePair>) van.getAttribute(feature_prefix)) {
        van_agg.insert(tvp.getValue());
      }
      for (TimeValuePair tvp : (List<TimeValuePair>) trail.getAttribute(feature_prefix)) {
        trail_agg.insert(tvp.getValue());
      }

      ((Region) regions.get(regions.size() - 1))
          .setAttribute(feature_prefix + "_reset", trail_agg.getMean() - van_agg.getMean());
    }

    ((Region) regions.get(regions.size() - 1)).setAttribute(feature_prefix + "_reset", "?");

  }
}
