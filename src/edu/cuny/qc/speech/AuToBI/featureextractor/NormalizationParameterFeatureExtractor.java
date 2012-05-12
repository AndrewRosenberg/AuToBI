/*  NormalizationParameterFeatureExtractor.java

    Copyright (c) 2011 Andrew Rosenberg

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

/**
 * NormalizationParameterFeatureExtractor generates a set of normalization parameters based on pitch and intensity
 * across the full set of regions.
 * <p/>
 * This is used in situations where there is not previously generated speaker normalization parameters.
 * <p/>
 * The operation does assume, however, that the file contains speech by a single speaker.
 */
@SuppressWarnings("unchecked")
public class NormalizationParameterFeatureExtractor extends FeatureExtractor {
  private String destination_feature;  // the destination feature name

  /**
   * Constructs a new NormalizationParameterFeatureExtractor.
   * <p/>
   * Currently this class requires "f0" and "I" attributes, and generates normalization parameters for pitch and
   * intensity.
   *
   * @param destination_feature the name of the feature to store the feature in.
   */
  public NormalizationParameterFeatureExtractor(String destination_feature) {
    this.destination_feature = destination_feature;

    this.extracted_features.add(destination_feature);
    // TODO: allow the normalized features to be specified through parameters
    this.required_features.add("f0");
    this.required_features.add("I");
  }

  /**
   * Generates a SpeakerNormalizationParameter across all available pitch and intensity information and associates this
   * object with each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    SpeakerNormalizationParameter snp = new SpeakerNormalizationParameter();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("f0")) {
        Contour pitch = (Contour) r.getAttribute("f0");
        snp.insertPitch(pitch);
      }
      if (r.hasAttribute("I")) {
        Contour intensity = (Contour) r.getAttribute("I");
        snp.insertIntensity(intensity);
      }
    }

    for (Region r : (List<Region>) regions) {
      r.setAttribute(destination_feature, snp);
    }
  }
}
