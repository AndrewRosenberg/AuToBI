/* ClassBasedWeightFunction.java

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

import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/3/12 Time: 10:45 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassBasedWeightFunction extends WeightFunction {
  private String attribute;
  private Map<String, Double> fn;

  public ClassBasedWeightFunction(String attribute, Map<String, Double> weight_fn) {
    this.attribute = attribute;
    this.fn = weight_fn;
  }

  public double weight(Region r) {
    if (r.hasAttribute(attribute) && fn.containsKey(r.getAttribute(attribute).toString())) {
      return fn.get(r.getAttribute(attribute).toString());
    }
    return 0;
  }
}
