/*  SubregionResetFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.SubregionUtils;

import java.util.List;

/**
 * SubregionResetFeatureExtractor extracts reset features from subregions.
 * <p/>
 * The standard ResetContourFeatureExtractor extract reset features from the full regions.  This extends this
 * functionality to identify reset over narrower regions.
 * <p/>
 * While developed as a utility for subregions, this can operate on regions longer than the initial region.  For
 * example, if the initial region is 100ms, and a 200ms subregion is requested, the subregion boundary will fall earlier
 * than the initial region boundary
 */
@SuppressWarnings("unchecked")
public class SubregionResetFeatureExtractor extends FeatureExtractor {
  private String subregion_name;    // the name of the subregion
  private Double subregion_length;  // the length (in seconds) of the subregion

  /**
   * Constructs a new SubregionResetFeatureExtractor.
   * <p/>
   * Assigns a contour, subregion name and the name of the acoustic feature.
   * <p/>
   * Subregions are described as a number of seconds "2s" or milliseconds "50ms".  Other names will raise an exception
   *
   * @param subregion_name the subregion label
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   *          if something goes wrong with the subregion name
   */
  public SubregionResetFeatureExtractor(String subregion_name) throws FeatureExtractorException {
    extracted_features.add("van_" + subregion_name);
    extracted_features.add("trail_" + subregion_name);

    this.subregion_name = subregion_name;
    this.subregion_length = SubregionUtils.parseSubregionName(subregion_name);
  }

  /**
   * Extracts subregion reset regions for each region.
   *
   * @param regions the regions to process
   * @throws edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException
   *          if something goes wrong with the extraction.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      // construct subregions
      Region trail_region = new Region(r.getStart(), r.getStart() + subregion_length);
      Region van_region = new Region(r.getEnd() - subregion_length, r.getEnd());

      r.setAttribute("van_" + subregion_name, van_region);
      r.setAttribute("trail_" + subregion_name, trail_region);
    }
  }
}
