/*  ContourPolyFitFeatureExtractor.java

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
 * ContourPolyFitFeatureExtractor extracts features related to fitting a polynomial to a contour.
 * <p/>
 * The features extracted include each polynomial coefficient and the mean squared error of the fit.
 */
@SuppressWarnings("unchecked")
public class ContourPolyFitFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "fit,fitMSE";
  private ContourPolynomialFitter fitter;  // the fitter responsible for calculating coefficients
  private String acoustic_feature; // the name of the acoustic contour feature

  public ContourPolyFitFeatureExtractor(ContourPolynomialFitter fitter, String acoustic_feature) {
    this.fitter = fitter;
    this.acoustic_feature = acoustic_feature;

    this.required_features.add(acoustic_feature);
    for (int i = 0; i <= fitter.getOrder(); ++i) {
      this.extracted_features.add("fit[" + acoustic_feature + "," + fitter.getOrder() + "," + i + "]");
    }
    this.extracted_features.add("fitMSE[" + acoustic_feature + "," + fitter.getOrder() + "]");
  }

  public ContourPolyFitFeatureExtractor(String order, String acoustic_feature) {
    this(new ContourPolynomialFitter(Integer.parseInt(order)), acoustic_feature);
  }

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
        double[] w = fitter.fitContour(c);
        for (int i = 0; i < w.length; ++i) {
          r.setAttribute("fit[" + acoustic_feature + "," + fitter.getOrder() + "," + i + "]", w[i]);
        }

        r.setAttribute("fitMSE[" + acoustic_feature + "," + fitter.getOrder() + "]", fitter.getMSE(c, w));
      }
    }
  }
}
