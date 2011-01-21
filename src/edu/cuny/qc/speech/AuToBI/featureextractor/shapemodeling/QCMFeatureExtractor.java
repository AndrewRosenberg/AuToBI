/*  QCMFeatureExtractor.java

    Copyright (c) 2009-2011 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;

import java.util.List;

/**
 * QCMFeatureExtractor extracts QuantizedContourModel log likelihood features.
 */
public class QCMFeatureExtractor extends FeatureExtractor {
  private QuantizedContourModel qcm; // the model
  private String acoustic_feature; // the acoustic contour feature
  private String feature_name; // the destination feature

  /**
   * Constructs a new QCMFeatureExtractor, using a user-specified model, and extracting the log likelihood of the
   * contour stored in acoustic_feature in feature_name.
   *
   * @param model            the QuantizedContourModel
   * @param feature_name     the destination feature for the log likelihood
   * @param acoustic_feature the feature containing the acoustic contour to evaluate.
   */
  public QCMFeatureExtractor(QuantizedContourModel model, String feature_name,
                             String acoustic_feature) {
    this.qcm = model;
    this.feature_name = feature_name;
    this.acoustic_feature = acoustic_feature;

    this.required_features.add(acoustic_feature);
    this.extracted_features.add(feature_name);
  }

  /**
   * Calculates the log likelihood of the contour stored in acoustic_feature and stores this
   * value in feature_name
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(acoustic_feature)) {
        Contour c = (Contour) r.getAttribute(acoustic_feature);
        try {
          double ll = qcm.evaluateContour(c);
          r.setAttribute(feature_name, ll);
        } catch (ContourQuantizerException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
    }
  }
}
