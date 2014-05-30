/*  PitchFeatureExtractor.java

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

import edu.cuny.qc.speech.AuToBI.*;
import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * IntensityFeatureExtractor extracts intensity information from a given WavData object.
 * <p/>
 * * v1.4 IntensityFeatureExtractor has changed to attach full contours to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
public class IntensityFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "I";

  private String feature_name;  // the name of the feature to hold intensity information

  @Deprecated
  public IntensityFeatureExtractor(String feature_name) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  public IntensityFeatureExtractor() {
    this.feature_name = moniker;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // Identify all regions which are associated with each wav data.
    HashMap<WavData, List<Region>> wave_region_map = new HashMap<WavData, List<Region>>();
    for (Region r : (List<Region>) regions) {
      WavData wav = (WavData) r.getAttribute("wav");
      if (wav != null) {
        if (!wave_region_map.containsKey(wav)) {
          wave_region_map.put(wav, new ArrayList<Region>());
        }
        wave_region_map.get(wav).add(r);
      }
    }

    for (WavData wav : wave_region_map.keySet()) {
      IntensityExtractor extractor = new IntensityExtractor(wav);
      Contour contour = extractor.soundToIntensity();
      // Assign pointer to the full contour to all data points.
      for (Region r : wave_region_map.get(wav)) {
        r.setAttribute(feature_name, contour);
      }
    }
  }
}