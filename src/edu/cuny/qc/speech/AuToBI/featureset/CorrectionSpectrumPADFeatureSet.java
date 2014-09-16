/*  CorrectionSpectrumPADFeatureSet.java

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

import edu.cuny.qc.speech.AuToBI.core.ContextDesc;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A FeatureSet subclass to generate correction classifications.
 * <p/>
 * This feature set describes the FeaturesRequired to generate corrections for a given spectral pitch accent detector.
 */
public class CorrectionSpectrumPADFeatureSet extends FeatureSet {

  /**
   * Constructs a CorrectionSpectrumPADFeatureSet for a spectral region defined by low and high bark values.
   *
   * @param low  the low bark value
   * @param high the high bark value
   */
  public CorrectionSpectrumPADFeatureSet(int low, int high) {
    super();
    insertRequiredFeature("duration__duration");

    insertRequiredFeature("nominal_bark_" + low + "_" + high + "__prediction");
    insertRequiredFeature("bark_" + low + "_" + high + "__prediction_confidence");
    insertRequiredFeature("bark_" + low + "_" + high + "__prediction_confidence_accented");

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
      for (String norm : new String[]{"", "_norm"}) {
        for (String slope : new String[]{"", "_delta"}) {
          for (String agg : new String[]{"__zMax", "__zMean"}) {
            insertRequiredFeature("f0" + slope + norm + "_" + context.getLabel() + agg);
          }
        }
      }
      insertRequiredFeature("duration__duration_" + context.getLabel() + "__zNorm");
      insertRequiredFeature("duration__duration_" + context.getLabel() + "__rNorm");
    }

    for (String acoustic : new String[]{"f0"}) {
      for (String norm : new String[]{"", "_norm"}) {
        for (String slope : new String[]{"", "_delta"}) {
          for (String agg : new String[]{"max", "mean", "stdev", "zMax"}) {
            insertRequiredFeature(acoustic + slope + norm + "__" + agg);
          }
        }
      }
    }

    this.class_attribute = "nominal_PitchAccentCorrect";
  }
}