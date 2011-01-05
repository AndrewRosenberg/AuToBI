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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * DetltaContourPairFeatureExtractor extracts the first order differences of a Contour.
 *
 * @see Contour
 */
public class DeltaContourFeatureExtractor extends FeatureExtractor {
  private String attribute_name;  // the attribute name to construct a delta contour from

  /**
   * Constructs a DeltaContourFeatureExtractor.
   *
   * @param attribute_name the base attribute name
   */
  public DeltaContourFeatureExtractor(String attribute_name) {
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
   * Calculates delta Contour features.
   * <p/>
   *
   * @param regions The regions to extract features from
   * @throws FeatureExtractorException When something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(attribute_name)) {
        Contour delta_contour = ContourUtils.generateDeltaContour((Contour) r.getAttribute(attribute_name));
        r.setAttribute("delta_" + attribute_name, delta_contour);
      }
    }
  }

}
