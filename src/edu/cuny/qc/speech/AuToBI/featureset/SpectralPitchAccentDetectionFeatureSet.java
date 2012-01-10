/*  SpectralPitchAccentDetectionFeatureSet.java

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
package edu.cuny.qc.speech.AuToBI.featureset;

import edu.cuny.qc.speech.AuToBI.core.FeatureSet;

/**
 * SpectralPitchAccentDetectionFeatureSet describes a feature set necessary for the corrected weighted majority voting
 * classification used by SpectralPitchAccentDetector.
 *
 * @see edu.cuny.qc.speech.AuToBI.SpectralPitchAccentDetector
 */
public class SpectralPitchAccentDetectionFeatureSet extends FeatureSet {

  public SpectralPitchAccentDetectionFeatureSet() {
    super();

    for (int low = 0; low <= 19; ++low) {
      for (int high = low + 1; high <= 20; ++high) {
        insertRequiredFeature("nominal_bark_" + low + "_" + high + "__prediction");
        insertRequiredFeature("bark_" + low + "_" + high + "__prediction_confidence");
        insertRequiredFeature("nominal_bark_" + low + "_" + high + "__correction_prediction");
        insertRequiredFeature("bark_" + low + "_" + high + "__correction_prediction_confidence");
      }
    }
    this.class_attribute = "nominal_PitchAccent";
  }
}
