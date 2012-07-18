/*  SubregionContourExtractor.java

    Copyright 2009-2011 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Constructs a subcontour object based on an acoustic contour and a defined subregion.
 */
@SuppressWarnings("unchecked")
public class SubregionContourExtractor extends FeatureExtractor {

  private String contour_feature; // the acoustic contour feature
  private String subregion_feature; // the subregion feature

  /**
   * Constructs a new SubregionContourExtractor.
   * <p/>
   * Extracts a new subcontour feature called "feature_name"_"subregion_feature" which is a subcontour of the feature
   * name extracted from the region defined by subregion.
   *
   * @param contour_feature   the acoustic feature name
   * @param subregion_feature the subregion
   */
  public SubregionContourExtractor(String contour_feature, String subregion_feature) {
    this.contour_feature = contour_feature;
    this.subregion_feature = subregion_feature;
    required_features.add(contour_feature);
    required_features.add(subregion_feature);

    extracted_features.add(contour_feature + "_" + subregion_feature);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    for (Region r : (List<Region>) regions) {
      Contour c = (Contour) r.getAttribute(contour_feature);
      if (c != null && r.hasAttribute(subregion_feature)) {
        Region subregion = (Region) r.getAttribute(subregion_feature);
        try {
          Contour subcontour = ContourUtils.getSubContour(c, subregion.getStart(), subregion.getEnd());
          r.setAttribute(contour_feature + "_" + subregion_feature, subcontour);
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
      }
    }
  }
}
