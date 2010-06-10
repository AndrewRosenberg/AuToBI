/*  DeltaTimeValuePairFeatureExtractor.java

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
 * DetltaTimeValuePairFeatureExtractor extracts aggregate qualities of the first order differences of a list of
 * TimeValuePairs.
 * <p/>
 * This operates by first calculating the deltas of a contour, then sending this new contour to a standard
 * TimeValuePairFeatureExtractor.
 *
 * @see TimeValuePairFeatureExtractor
 */
public class DeltaTimeValuePairFeatureExtractor extends TimeValuePairFeatureExtractor {
  private List<TimeValuePair> delta_values;  // a list of first order differences

  private TimeValuePairFeatureExtractor tvpfe;  // a base feature extractor

  /**
   * Constructs a DeltaTimeValuePairFeatureExtractor.
   *
   * @param values         the initial values
   * @param attribute_name the base attribute name
   */
  public DeltaTimeValuePairFeatureExtractor(List<TimeValuePair> values, String attribute_name) {
    this.values = values;
    this.delta_values = null;
    this.attribute_name = attribute_name + "_delta";
    this.tvpfe = new TimeValuePairFeatureExtractor(null, this.attribute_name);
    this.setAttributeName(this.attribute_name);
  }

  /**
   * Set the attribute name.
   *
   * @param attribute_name the attribute name
   */
  public void setAttributeName(String attribute_name) {
    super.setAttributeName(attribute_name);
    if (tvpfe != null)
      tvpfe.setAttributeName(attribute_name);
  }

  /**
   * Extracts delta time value pair features.
   * <p/>
   * Note: we defer calculating the delta time values until feature extraction.
   * This limits the construction overhead of the feature extractor in situations where the delta featues may not be used.
   *
   * @param regions The regions to extract features from
   * @throws FeatureExtractorException When something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    if (this.delta_values == null) {
      this.delta_values = generateDeltaValues(values);
    }
    tvpfe.setValues(delta_values);
    tvpfe.extractFeatures(regions);
  }

  /**
   * Generates delta time value pairs from raw data.
   * <p/>
   * The resulting points are the first order difference of subsequent values and they are placed in time between
   * the surrounding points..
   *
   * @param values The initial values
   * @return The delta values
   */
  private List<TimeValuePair> generateDeltaValues(List<TimeValuePair> values) {
    List<TimeValuePair> d_values = new ArrayList<TimeValuePair>();
    for (int i = 0; i < values.size() - 1; ++i) {
      double time = (values.get(i + 1).getTime() + values.get(i).getTime()) / 2;
      double value = (values.get(i + 1).getValue() - values.get(i).getValue()) /
                     (values.get(i + 1).getTime() - values.get(i).getTime());
      d_values.add(new TimeValuePair(time, value));
    }
    return d_values;
  }
}
