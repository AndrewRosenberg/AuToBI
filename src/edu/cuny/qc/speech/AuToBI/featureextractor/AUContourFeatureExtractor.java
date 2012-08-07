/*  AUContourFeatureExtractor.java

    Copyright (c) 2012 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Pair;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Extracts the area under a contour.
 */
@SuppressWarnings("unchecked")
public class AUContourFeatureExtractor extends FeatureExtractor {
  private String feature;  // The contour feature to calculate the area under

  public AUContourFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature);
    this.extracted_features.add(feature + "__area");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature)) {
        Contour c = (Contour) r.getAttribute(feature);
        double sum = 0.0;
        for (Pair<Double, Double> x : c) {
          sum += x.second;
        }
        r.setAttribute(feature + "__area", sum);
      }
    }
  }
}
