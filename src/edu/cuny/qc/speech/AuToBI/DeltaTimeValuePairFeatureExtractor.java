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
 * DetltaTimeValuePairFeatureExtractor extracts the first order differences of a list of
 * TimeValuePairs.
 * <p/>
 */
public class DeltaTimeValuePairFeatureExtractor extends FeatureExtractor {
  private String attribute_name;  // the attribute name to construct a delta contour from

  /**
   * Constructs a DeltaTimeValuePairFeatureExtractor.
   *
   * @param attribute_name the base attribute name
   */
  public DeltaTimeValuePairFeatureExtractor(String attribute_name) {
    setAttributeName(attribute_name);
  }

  /**
   * Set the attribute name.
   *
   * @param attribute_name the attribute name
   */
  public void setAttributeName(String attribute_name) {
    this.attribute_name = attribute_name;
    required_features.add(attribute_name);

    extracted_features.add("delta_" + attribute_name);
  }

  /**
   * Calculates delta time value pair features.
   * <p/>
   *
   * @param regions The regions to extract features from
   * @throws FeatureExtractorException When something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(attribute_name)) {
        List<TimeValuePair> delta_contour = generateDeltaValues((List<TimeValuePair>) r.getAttribute(attribute_name));
        r.setAttribute("delta_" + attribute_name, delta_contour);
      }
    }
  }

  /**
   * Generates delta time value pairs from raw data.
   * <p/>
   * The resulting points are the first order difference of subsequent values and they are placed in time between the
   * surrounding points..
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
