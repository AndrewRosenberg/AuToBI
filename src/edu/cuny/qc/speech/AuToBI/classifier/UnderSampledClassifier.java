/*  UnderSampledClassifier.java

    Copyright (c) 2012-2014 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureSet;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.PartitionUtils;

/**
 * UnderSampledClassifier downsamples majority class tokens before training.
 */
public class UnderSampledClassifier extends AuToBIClassifier {
  private AuToBIClassifier classifier;
  private String class_attribute;

  public UnderSampledClassifier(AuToBIClassifier classifier, String class_attribute) {
    this.classifier = classifier;
    this.class_attribute = class_attribute;
  }

  public Distribution distributionForInstance(Word testing_point) throws Exception {
    return classifier.distributionForInstance(testing_point);
  }

  public void train(FeatureSet feature_set) throws Exception {

    FeatureSet undersampled = feature_set.newInstance();
    undersampled.setDataPoints(PartitionUtils.performUnderSampling(feature_set.getDataPoints(), class_attribute));
    classifier.train(undersampled);
  }

  public AuToBIClassifier newInstance() {
    return new UnderSampledClassifier(classifier.newInstance(), class_attribute);
  }
}
