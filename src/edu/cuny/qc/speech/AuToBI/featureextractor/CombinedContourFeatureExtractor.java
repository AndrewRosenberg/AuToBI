/*  CombinedContourFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class CombinedContourFeatureExtractor extends FeatureExtractor {
  private String feature1;  // the first feature
  private String feature2;  // the second feature
  private double f2_coeff;  // the combination coefficient of the second feature

  /**
   * Constructs a new CombinedContourFeatureExtractor to merge two contours into a single numeric contour
   *
   * @param f1       the first contour feature
   * @param f2       the second contour feature
   * @param f2_coeff a coefficient to multiply the values of the second contour
   */
  public CombinedContourFeatureExtractor(String f1, String f2, double f2_coeff) {
    this.feature1 = f1;
    this.feature2 = f2;
    this.f2_coeff = f2_coeff;

    required_features.add(f1);
    required_features.add(f2);
    extracted_features.add(f1 + f2);
  }

  /**
   * Combines the two features
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if the normalization parameters cannot normalize the features
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature1) && r.hasAttribute(feature2)) {
        Contour c = combineContours((Contour) r.getAttribute(feature1), (Contour) r.getAttribute(feature2), f2_coeff);
        r.setAttribute(feature1 + feature2, c);
      }
    }
  }

  private Contour combineContours(Contour c1, Contour c2, double c2_coeff) {
    Contour c = new Contour(c1.getStart(), c1.getStep(), c1.size());
    for (Pair<Double, Double> p : c1) {
      int idx2 = c2.indexFromTime(p.first);
      if (!c2.isEmpty(idx2)) {
        c.set(p.first, p.second * c2.get(idx2) * c2_coeff);
      }
    }
    return c;
  }
}
