/*  SkewFeatureExtractor.java

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
 * SkewFeatureExtractor calculates skew coefficients over each region.  Skew coefficients are calculated based on Paul
 * Taylor's Tilt parameterizations. They are used to calculate the offset between the tilt of pitch and intensity
 * contours.
 * <p/>
 * If the two contours are aligned the skew is zero.  If the pitch contour is left skewed, the skew values are negative,
 * and vice versa.
 *
 * @see TiltFeatureExtractor
 * @see edu.cuny.qc.speech.AuToBI.core.TiltParameters
 */
@SuppressWarnings("unchecked")
public class SkewFeatureExtractor extends FeatureExtractor {

  private String f1;
  private String f2;

  /**
   * Constructs a new SkewFeatureExtractor.
   */
  public SkewFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    required_features.add(f1 + "__tilt_amp");
    required_features.add(f1 + "__tilt_dur");
    required_features.add(f2 + "__tilt_amp");
    required_features.add(f2 + "__tilt_dur");

    extracted_features.add(f1 + f2 + "__skew_amp");
    extracted_features.add(f1 + f2 + "__skew_dur");
  }

  /**
   * Calculates skew features from each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   *          should never happen
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(f1 + "__tilt_amp") && r.hasAttribute(f1 + "__tilt_dur") && r.hasAttribute(f2 + "__tilt_amp") &&
          r.hasAttribute(f2 + "__tilt_dur")) {
        r.setAttribute(f1 + f2 + "__skew_amp",
            ((Double) r.getAttribute(f1 + "__tilt_amp")) - ((Double) r.getAttribute(f2 + "__tilt_amp")));
        r.setAttribute(f1 + f2 + "__skew_dur",
            ((Double) r.getAttribute(f1 + "__tilt_dur")) - ((Double) r.getAttribute(f2 + "__tilt_dur")));
      }
    }
  }
}
