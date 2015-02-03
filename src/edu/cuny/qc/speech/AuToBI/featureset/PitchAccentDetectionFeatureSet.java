/*  PitchAccentDetectionFeatureSet.java

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
package edu.cuny.qc.speech.AuToBI.featureset;

import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

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

    for (String acoustic : new String[]{"f0", "log[f0]", "I"}) {
      for (String norm : new String[]{"", "znormC"}) {
        for (String slope : new String[]{"", "delta"}) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            String f = AuToBIUtils.makeFeatureName(agg, AuToBIUtils
                .makeFeatureName(slope, AuToBIUtils.makeFeatureName(norm, acoustic)));
            insertRequiredFeature(f);
          }
        }
      }
    }

    for (String acoustic : new String[]{"spectralTilt[2,20]", "spectrumBand[2,20]"}) {
      for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
        insertRequiredFeature(AuToBIUtils.makeFeatureName(agg, acoustic));
      }
    }


    List<String> contexts = new ArrayList<String>();
    contexts.add("f2b2");
    contexts.add("f2b1");
    contexts.add("f2b0");
    contexts.add("f1b2");
    contexts.add("f0b2");
    contexts.add("f0b1");
    contexts.add("f1b0");
    contexts.add("f1b1");

    for (String context : contexts) {
      for (String acoustic : new String[]{"spectralTilt[2,20]", "spectrumBand[2,20]"}) {
        for (String agg : new String[]{"zMaxWordContext", "zMeanWordContext"}) {
          insertRequiredFeature(AuToBIUtils.makeFeatureName(agg, acoustic, context));
        }
      }

      for (String acoustic : new String[]{"f0", "log[f0]", "I"}) {
        for (String norm : new String[]{"", "znormC"}) {
          for (String slope : new String[]{"", "delta"}) {
            for (String agg : new String[]{"zMeanWordContext", "zMaxWordContext"}) {
              insertRequiredFeature(AuToBIUtils
                  .makeFeatureName(agg, AuToBIUtils.makeFeatureName(slope, AuToBIUtils.makeFeatureName(norm, acoustic)),
                      context));
            }
          }
        }
      }
    }

    /**
     * AT&T Specific features
     */
    insertRequiredFeature("voicingRatio[f0]");

    insertRequiredFeature("duration");

    // Aggregations, center of gravity, area
    for (String acoustic : new String[]{"znormC[log[f0]]", "rnormC[I]", "prodC[znormC[log[f0]],rnormC[I],0.1]"}) {
      for (String slope : new String[]{"", "delta"}) {
        for (String agg : new String[]{"max", "mean", "min", "stdev", "zMax", "cog", "area", "tiltAmp", "tiltDur",
            "highLowDiff", "PVAmp", "PVLocation", "risingLL", "fallingLL",
            "peakLL", "valleyLL"}) {
          insertRequiredFeature(AuToBIUtils.makeFeatureName(agg, AuToBIUtils.makeFeatureName(slope, acoustic)));
        }
      }
    }

    // skew
    insertRequiredFeature("skewAmp[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("skewDur[znormC[log[f0]],rnormC[I]]");

    // location and area difference features
    insertRequiredFeature("minus[area[znormC[log[f0]]],area[rnormC[I]]]");
    insertRequiredFeature("ratio[area[znormC[log[f0]]],area[rnormC[I]]]");
    insertRequiredFeature("minus[PVLocation[znormC[log[f0]]],PVLocation[rnormC[I]]]");
    insertRequiredFeature("ratio[PVLocation[znormC[log[f0]]],PVLocation[rnormC[I]]]");

    // rmse and error features
    insertRequiredFeature("rmse[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("meanError[znormC[log[f0]],rnormC[I]]");

    // twoway shape likelihood features
    insertRequiredFeature("rrLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("rfLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("rpLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("rvLL[znormC[log[f0]],rnormC[I]]");

    insertRequiredFeature("frLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("ffLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("fpLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("fvLL[znormC[log[f0]],rnormC[I]]");

    insertRequiredFeature("prLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("pfLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("ppLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("pvLL[znormC[log[f0]],rnormC[I]]");

    insertRequiredFeature("vrLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("vfLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("vpLL[znormC[log[f0]],rnormC[I]]");
    insertRequiredFeature("vvLL[znormC[log[f0]],rnormC[I]]");

    this.class_attribute = "nominal_PitchAccent";
  }
}