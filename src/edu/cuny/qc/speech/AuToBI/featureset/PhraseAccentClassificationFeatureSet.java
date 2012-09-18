/*  PhraseAccentClassificationFeatureSet.java

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
package edu.cuny.qc.speech.AuToBI.featureset;

import edu.cuny.qc.speech.AuToBI.core.FeatureSet;

import java.util.ArrayList;
import java.util.List;

/**
 * PhraseAccentClassificationFeatureSet is responsible for describing the features necessary to perform phrase accent
 * classification.
 */
public class PhraseAccentClassificationFeatureSet extends FeatureSet {

  /**
   * Constructs a new PhraseAccentClassificationFeatureSet.
   */
  public PhraseAccentClassificationFeatureSet() {
    super();

    insertRequiredFeature("duration__duration");

    for (String acoustic : new String[]{"f0", "I"}) {
      for (String norm : new String[]{"", "norm_"}) {
        for (String slope : new String[]{"", "delta_"}) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            insertRequiredFeature(slope + norm + acoustic + "_200ms" + "__" + agg);
          }
        }
      }
    }

    /**
     * AT&T Specific features
     */
    insertRequiredFeature("log_f0__voicingRatio");

    insertRequiredFeature("duration__duration");

    // Aggregations, center of gravity, area
    List<String> subregions = new ArrayList<String>();
    subregions.add("_200ms");

    for (String acoustic : new String[]{"norm_log_f0", "rnorm_I", "norm_log_f0rnorm_I"}) {
      for (String slope : new String[]{"", "delta_"}) {
        for (String subregion : subregions) {
          for (String agg : new String[]{"max", "mean", "min", "stdev", "zMax", "cog", "area", "tilt_amp", "tilt_dur",
              "highLowDiff", "PVAmp", "PVLocation", "risingCurveLikelihood", "fallingCurveLikelihood",
              "peakCurveLikelihood", "valleyCurveLikelihood"}) {
            insertRequiredFeature(slope + acoustic + subregion + "__" + agg);
          }
        }
      }
    }

    // skew
    insertRequiredFeature("norm_log_f0rnorm_I__skew_amp");
    insertRequiredFeature("norm_log_f0rnorm_I__skew_dur");

    // location and area difference features
    insertRequiredFeature("norm_log_f0__area_minus_rnorm_I__area");
    insertRequiredFeature("norm_log_f0__area_ratio_rnorm_I__area");
    insertRequiredFeature("norm_log_f0__PVLocation_minus_rnorm_I__PVLocation");
    insertRequiredFeature("norm_log_f0__PVLocation_ratio_rnorm_I__PVLocation");

    // rmse and error features
    insertRequiredFeature("norm_log_f0_rnorm_I__rmse");
    insertRequiredFeature("norm_log_f0_rnorm_I__meanError");

    // twoway shape likelihood features
    insertRequiredFeature("norm_log_f0rnorm_I__rrCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__rfCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__rpCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__rvCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__frCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__ffCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__fpCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__fvCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__prCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__pfCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__ppCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__pvCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__vrCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__vfCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__vpCurveLikelihood");
    insertRequiredFeature("norm_log_f0rnorm_I__vvCurveLikelihood");

    class_attribute = "nominal_PhraseAccentType";
  }
}
