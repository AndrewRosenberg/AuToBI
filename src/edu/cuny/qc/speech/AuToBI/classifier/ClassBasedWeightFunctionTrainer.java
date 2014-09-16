/* ClassBasedWeightFunctionTrainer.java

  Copyright 2014 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/3/12 Time: 10:46 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassBasedWeightFunctionTrainer {

  private String class_attribute;
  private WeightType type;

  public enum WeightType {LINEAR, ENTROPY}

  public ClassBasedWeightFunctionTrainer(String class_attribute, WeightType type) {
    this.class_attribute = class_attribute;
    this.type = type;
  }

  public ClassBasedWeightFunction trainWeightFunction(List<Word> regions) {
    Map<String, Double> weight_fn = getClassWeightMapping(regions, class_attribute, type);

    return new ClassBasedWeightFunction(class_attribute, weight_fn);
  }

  public static Map<String, Double> getClassWeightMapping(List<Word> regions, String class_attribute, WeightType type) {
    // Calculate the distribution of class values within the regions
    Distribution d = new Distribution();
    for (Region r : regions) {
      if (r.hasAttribute(class_attribute)) {
        d.add(r.getAttribute(class_attribute).toString());
      }
    }
    try {
      d.normalize();
    } catch (AuToBIException e) {
      AuToBIUtils.warn("Error in class weight calculation: " + e.getMessage());
    }

    // Calculate the class weight
    Map<String, Double> weight_fn = new HashMap<String, Double>();
    for (String s : d.keySet()) {
      double p = d.get(s);
      if (type == WeightType.LINEAR) {
        weight_fn.put(s, 1 / p);
      } else if (type == WeightType.ENTROPY) {
        weight_fn.put(s, -p * Math.log(p));
      }
    }
    return weight_fn;
  }
}
