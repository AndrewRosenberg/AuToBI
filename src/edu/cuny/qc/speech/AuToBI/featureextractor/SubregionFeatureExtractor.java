/*  SubregionFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.SubregionUtils;

import java.util.List;


/**
 * SubregionFeatureExtractor constructs "subregions" aligned with the end of the supplied regions and preceding
 * the ending boundary by a specified amount of time.
 * <p/>
 * While it is intended that the subregions are completely contained within the initial region, there is no enforcement
 * of this policy.  Thus subregions can in fact be longer than the initial region.  E.g. if the initial region is 100ms
 * and a 200ms region is requested the "subregion" is longer than the initial region.
 */
public class SubregionFeatureExtractor extends FeatureExtractor {
  private String subregion_name;    // the name of the subregion
  private Double subregion_length;  // the length in seconds of the subregion

  /**
   * Constructs a new SubregionFeatureExtractor
   *
   * @param subregion_name a descriptor of the subregion
   * @throws FeatureExtractorException if the subregion_name cannot be parsed.
   */
  public SubregionFeatureExtractor(String subregion_name) throws FeatureExtractorException {
    this.subregion_name = subregion_name;
    this.subregion_length = SubregionUtils.parseSubregionName(subregion_name);

    this.extracted_features.add(subregion_name);
  }

  /**
   * Constructs "subregion" features for each region.
   *
   * @param regions The regions to extract features from.
   */
  public void extractFeatures(List regions) {
    for (Region r : (List<Region>) regions) {
      Region subregion = new Region(r.getEnd() - subregion_length, r.getEnd());

      r.setAttribute(subregion_name, subregion);
    }
  }
}
