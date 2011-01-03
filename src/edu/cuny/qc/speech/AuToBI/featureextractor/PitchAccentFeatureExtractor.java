/*  PitchAccentFeatureExtractor.java

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
 * PitchAccentFeatureExtractor extracts the ground truth presence or absence of a pitch accent for a set of regions.
 */
public class PitchAccentFeatureExtractor extends FeatureExtractor {
  private String feature;  // the destination feature

  /**
   * Constructs a new PitchAccentFeatureExtractor.
   *
   * @param feature the destination feature
   */
  public PitchAccentFeatureExtractor(String feature) {
    this.feature = feature;
    extracted_features.add(feature);
  }

  /**
   * Stores an indicator of the presence or absence of a pitch accent on each region, based on ground truth annotation.
   *
   * Note: Currently the value of the feature if an accent is present is "ACCENTED", and absence is indicated by "DEACCENTED".
   * This should probably be modified; the term, "deaccented" can have more connotations than simply not bearing pitch
   * accent. It may be best to use "ACCENT" and "NOACCENT" as a default while allowing a user to specify alternate values. 
   *
   * @param regions The regions to extract features from.
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException if somethign goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    ToBIUtils.setPitchAccent((List<Word>) regions, feature);
  }
}
