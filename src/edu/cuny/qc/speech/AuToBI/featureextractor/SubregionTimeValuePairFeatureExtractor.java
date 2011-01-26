/*  SubregionTimeValuePairFeatureExtractor.java

    Copyright 2009-2010 Andrew Rosenberg

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
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * SubregionTimeValuePairFeatureExtractor extarcts standard time value pair features from subregions rather than from
 * the entire supplied regions.
 *
 * This has been deprecated in favor of identifying the subcontour using SubregionContourExtractor and then
 * extracting features as usual with a ContourFeatureExtractor
 *
 * @see ContourFeatureExtractor
 */
@Deprecated
public class SubregionTimeValuePairFeatureExtractor extends FeatureExtractor {

  private ContourFeatureExtractor tvpfe;
  // a FeatureExtractor responsible for the extraction of subregion features
  private String attribute_name;  // the name of the acoustic attribute to analyze.
  private String subregion_attribute;
  // a descriptor of the feature containing the subregion object to be analysed

  /**
   * Constructs a new SubregionTimeValuePairFeatureExtractor.
   *
   * @param feature_name        the contour to analyze.
   * @param subregion_attribute the name of the attribute containing the subregion
   */
  public SubregionTimeValuePairFeatureExtractor(String feature_name, String subregion_attribute) {
    this.subregion_attribute = subregion_attribute;
    this.tvpfe = new ContourFeatureExtractor(feature_name);
    setAttributeName(feature_name);

    this.required_features.add(subregion_attribute);
  }

  /**
   * Sets the base attribute name, e.g., "f0".
   * <p/>
   * This is initially assigned from the associated ContourFeatureExtractor.
   * <p/>
   * It is necessary for this value to match the identifier stored in SpeakerNormalizationParameter.
   *
   * @param attribute_name the new attribute name
   * @see edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter
   */
  public void setAttributeName(String attribute_name) {
    this.attribute_name = attribute_name;
    tvpfe.setAttributeName(attribute_name);

    extracted_features = new ArrayList<String>();
    for (String feature : tvpfe.getExtractedFeatures()) {
      extracted_features.add(feature + "_" + subregion_attribute);
    }
    this.required_features.add(attribute_name);
  }

  /**
   * Extracts time value pair features from subregions of each region.
   * <p/>
   * A subregion object must be assigned to each region prior to processing.
   *
   * @param regions the regions to extract features from.
   * @throws FeatureExtractorException if somethign goes wrong -- no subregion feature is assigned or a problem with the
   *                                   tvpfe.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Construct a list of subregions.
    List<Region> subregions = new ArrayList<Region>();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(subregion_attribute)) {
        subregions.add((Region) r.getAttribute(subregion_attribute));
      } else {
        throw new FeatureExtractorException(
            "Region, " + r + ", does not have a valid subregion feature, " + subregion_attribute);
      }
    }

    try {
      ContourUtils.assignValuesToSubregions(subregions, regions, attribute_name);
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }

    tvpfe.extractFeatures(subregions, regions);

    // Copy extracted features from subregions to regions
    for (int i = 0; i < regions.size(); ++i) {
      Region r = (Region) regions.get(i);
      Region subregion = subregions.get(i);
      for (String feature : tvpfe.getExtractedFeatures()) {
        r.setAttribute(feature + "_" + subregion_attribute, subregion.getAttribute(feature));
      }
    }
  }
}
