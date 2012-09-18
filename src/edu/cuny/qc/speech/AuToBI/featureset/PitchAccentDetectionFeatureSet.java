/*  PitchAccentDetectionFeatureSet.java

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

import edu.cuny.qc.speech.AuToBI.core.ContextDesc;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;

import java.util.List;
import java.util.ArrayList;

/**
 * PitchAccentDetectionFeatureSet describes the features required for detecting pitch accents.
 */
public class PitchAccentDetectionFeatureSet extends FeatureSet {

  /**
   * Constructs a new PitchAccentDetectionFeatureSet.
   */
  public PitchAccentDetectionFeatureSet() {
    super();

    for (String acoustic : new String[]{"f0", "I"}) {
      for (String norm : new String[]{"", "norm_"}) {
        for (String slope : new String[]{"", "delta_"}) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            insertRequiredFeature(slope + norm + acoustic + "__" + agg);
          }
        }
      }
    }

    for (String acoustic : new String[]{"bark_tilt_2_20", "bark_2_20"}) {
      for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
        insertRequiredFeature(acoustic + "__" + agg);
      }
    }


    List<ContextDesc> contexts = new ArrayList<ContextDesc>();
    contexts.add(new ContextDesc("f2b2", 2, 2));
    contexts.add(new ContextDesc("f2b1", 2, 1));
    contexts.add(new ContextDesc("f2b0", 2, 0));
    contexts.add(new ContextDesc("f1b2", 1, 2));
    contexts.add(new ContextDesc("f0b2", 0, 2));
    contexts.add(new ContextDesc("f0b1", 0, 1));
    contexts.add(new ContextDesc("f1b0", 1, 0));
    contexts.add(new ContextDesc("f1b1", 1, 1));
    for (ContextDesc context : contexts) {
      for (String acoustic : new String[]{"bark_2_20", "bark_tilt_2_20"}) {
        for (String agg : new String[]{"zMax", "zMean"}) {
          insertRequiredFeature(acoustic + "_" + context.getLabel() + "__" + agg);
        }
      }

      for (String acoustic : new String[]{"f0", "I"}) {
        for (String norm : new String[]{"", "norm_"}) {
          for (String slope : new String[]{"", "delta_"}) {
            for (String agg : new String[]{"zMean", "zMax"}) {
              insertRequiredFeature(slope + norm + acoustic + "_" + context.getLabel() + "__" + agg);
            }
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
    for (String acoustic : new String[]{"norm_log_f0", "rnorm_I", "norm_log_f0rnorm_I"}) {
      for (String slope : new String[]{"", "delta_"}) {
        for (String agg : new String[]{"max", "mean", "min", "stdev", "zMax", "cog", "area", "tilt_amp", "tilt_dur",
            "highLowDiff", "PVAmp", "PVLocation", "risingCurveLikelihood", "fallingCurveLikelihood",
            "peakCurveLikelihood", "valleyCurveLikelihood"}) {
          insertRequiredFeature(slope + acoustic + "__" + agg);
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

    this.class_attribute = "nominal_PitchAccent";
  }
}