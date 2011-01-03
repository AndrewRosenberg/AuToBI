/*  PitchAccentTypeFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.util.ToBIUtils;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.List;

/**
 * PitchAccentTypeFeatureExtractor extracts ground truth labels for the type of pitch accent (if any) on each region.
 */
public class PitchAccentTypeFeatureExtractor extends FeatureExtractor {
  private String feature;  // the destination feature

  /**
   * Cosntructs a new PitchAccentTypeFeatureExtractor.
   *
   * @param feature the destination feature to store the pitch accent type.
   */
  public PitchAccentTypeFeatureExtractor(String feature) {
    this.feature = feature;
    extracted_features.add(feature);
  }

  /**
   * Extracts the ground truth pitch accent typ annotation to the destination feature name for each region.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if there is a problem.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    ToBIUtils.setPitchAccentType((List<Word>) regions, feature);
  }
}