/*  TiltFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.TiltParameters;
import edu.cuny.qc.speech.AuToBI.core.TimeValuePair;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;

import java.util.List;

/**
 * TiltFeatureExtractor is used to extract Paul Taylor's Tilt features over a given region.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.TiltParameters
 */
public class TiltFeatureExtractor extends FeatureExtractor {
  private String tvp_feature;  // the TimeValuePair feature to analyze

  /**
   * Constructs a new TiltFeatureExtractor to calculate Tilt features using the given feature
   * 
   * @param tvp_feature the TimeValuePair feature name
   */
  public TiltFeatureExtractor(String tvp_feature) {
    this.tvp_feature = tvp_feature;
    required_features.add(tvp_feature);

    extracted_features.add(tvp_feature + "__tilt");
    extracted_features.add(tvp_feature + "__tilt_amp");
    extracted_features.add(tvp_feature + "__tilt_dur");
  }

  /**
   * Calculates Tilt parameters for the contour referenced by tvp_feature over each region.
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException should never happen.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r: (List<Region>) regions) {
      TiltParameters tilt = new TiltParameters((List<TimeValuePair>) r.getAttribute(tvp_feature));

      r.setAttribute(tvp_feature + "__tilt", tilt.getTilt());
      r.setAttribute(tvp_feature + "__tilt_amp", tilt.getAmplitudeTilt());
      r.setAttribute(tvp_feature + "__tilt_dur", tilt.getDurationTilt());
    }
  }
}
