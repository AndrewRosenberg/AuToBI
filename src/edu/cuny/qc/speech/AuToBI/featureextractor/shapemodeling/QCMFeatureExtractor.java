/*  QCMFeatureExtractor.java

    Copyright (c) 2009-2014 Andrew Rosenberg

  This file is part of the AuToBI prosodic analysis package.

  AuToBI is free software: you can redistribute it and/or modify
  it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * QCMFeatureExtractor extracts QuantizedContourModel log likelihood features.
 * <p/>
 * This feature extractor is currently deprecated by not following the AuToBI v1.4 feature naming conventions.
 * <p/>
 * NOTE: The challenge here is that the QCM model itself is a retrieved resource that isn't 'created' at runtime by the
 * by the feature extraction routine.  This suggests a resource registry that can be indexed by a string to attach a
 * trained
 * model.  Here resources would be registered via a config file or default paths pointing to a resource path (within
 * the jar)  This is a strong candidate for inclusion in 1.4.1.
 */
@SuppressWarnings("unchecked")
@Deprecated
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
   * Calculates the log likelihood of the contour stored in acoustic_feature and stores this value in feature_name
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(acoustic_feature)) {
        Contour super_c = (Contour) r.getAttribute(acoustic_feature);
        Contour c;
        try {
          c = ContourUtils.getSubContour(super_c, r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
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
