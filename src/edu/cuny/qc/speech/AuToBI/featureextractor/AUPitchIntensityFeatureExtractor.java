/*  AUMultipleContourFeatureExtractor.java

    Copyright (c) 2012 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Extracts the area under multiple contours with weights for each.
 */
@SuppressWarnings("unchecked")
public class AUPitchIntensityFeatureExtractor extends FeatureExtractor {

  private String extracted_f;  // the name of the extracted feature.
  private String pitch_f;      // the name of the pitch feature
  private String i_f;          // the name of the intensity feature
  private double i_coeff;      // the intensity scaling coefficient

  public AUPitchIntensityFeatureExtractor(String pitch_f, String i_f, double i_coeff) {
    this.pitch_f = pitch_f;
    this.i_f = i_f;
    this.i_coeff = i_coeff;

    this.extracted_f = pitch_f + i_f + "__area";
    this.extracted_features.add(extracted_f);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      Contour super_pitch_c = (Contour) r.getAttribute(pitch_f);
      Contour pitch_c;
      try {
        pitch_c = ContourUtils.getSubContour(super_pitch_c, r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      Contour super_i_c = (Contour) r.getAttribute(i_f);
      Contour i_c;
      try {
        i_c = ContourUtils.getSubContour(super_i_c, r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      double sum = 0.0;
      for (Pair<Double, Double> x : pitch_c) {
        double time = x.first;
        if (!Double.isNaN(x.second) && !i_c.isEmpty(i_c.indexFromTime(time))) {
          sum += i_c.get(time) * i_coeff * x.second;
        }
      }

      r.setAttribute(extracted_f, sum);
    }
  }
}
