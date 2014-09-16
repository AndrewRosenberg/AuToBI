/*  SpectralPitchAccentDetectionFeatureSet.java

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

/**
 * SpectralPitchAccentDetectionFeatureSet describes a feature set necessary for the corrected weighted majority voting
 * classification used by SpectralPitchAccentDetector.
 *
 * @see edu.cuny.qc.speech.AuToBI.SpectralPitchAccentDetector
 */
public class SpectralPitchAccentDetectionFeatureSet extends FeatureSet {

  public SpectralPitchAccentDetectionFeatureSet() {
    super();

    for (int low = 0; low <= 19; ++low) {
      for (int high = low + 1; high <= 20; ++high) {
        insertRequiredFeature("nominal_bark_" + low + "_" + high + "__prediction");
        insertRequiredFeature("bark_" + low + "_" + high + "__prediction_confidence");
        insertRequiredFeature("nominal_bark_" + low + "_" + high + "__correction_prediction");
        insertRequiredFeature("bark_" + low + "_" + high + "__correction_prediction_confidence");
      }
    }
    this.class_attribute = "nominal_PitchAccent";
  }
}
