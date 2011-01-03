/*  IntonationalPhraseBoundaryFeatureSet.java

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
 * IntonationalPhraseBoundaryFeatureExtractor is a FeatureExtractor that is responsible for extracting ground truth
 * labels for whether or not a word boundary is an intonational phrase boundary.
 *
 * @see edu.cuny.qc.speech.AuToBI.core.FeatureExtractor
 */
public class IntonationalPhraseBoundaryFeatureExtractor extends FeatureExtractor {
  private String feature;  // the stored feature name


  /**
   * Constructs a new IntonationalPhraseBoundaryFeatureExtractor.
   *
   * The ground truth label will be stored in the feature name passed via the parameter, feature.
   *
   * @param feature the stored parameter name
   */
  public IntonationalPhraseBoundaryFeatureExtractor(String feature) {
    this.feature = feature;
    extracted_features.add(feature);
  }

  /**
   * Extracts the ground truth intonational phrase boundary labels.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException If something goes wrong.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    ToBIUtils.setIntonationalPhraseBoundary((List<Word>) regions, feature);
  }
}