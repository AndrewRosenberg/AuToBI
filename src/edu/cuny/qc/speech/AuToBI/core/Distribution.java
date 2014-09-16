/*  Distribution.java

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
package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.HashMap;

/**
 * A class to extend commonly used distribution functions to HashMap<String, Integer>.
 * <p/>
 * Distribution can be used to represent histogram information as well as multinomial distributions
 */
public class Distribution extends HashMap<String, Double> {
  /**
   * Adds a string to the distribution
   *
   * @param s the string to add
   */
  public void add(String s) {
    Double value = 1.0;
    if (containsKey(s)) {
      value = get(s) + 1;
    }
    put(s, value);
  }

  /**
   * Adds a string to the distribution with a weight
   *
   * @param s      the string
   * @param weight the weight
   */
  public void add(String s, Double weight) {
    Double value = weight;
    if (containsKey(s)) {
      value = get(s) + weight;
    }
    put(s, value);
  }

  /**
   * Identify the key with the greatest value.
   * <p/>
   * The behavior is undefined when there are multiple equal valued keys.
   *
   * @return A key with the greatest associated value
   */
  public String getKeyWithMaximumValue() {
    String best_key = null;
    Double max_value = -Double.MAX_VALUE;
    for (String s : keySet()) {
      if (get(s) > max_value) {
        max_value = get(s);
        best_key = s;
      }
    }
    if (best_key == null) {
      AuToBIUtils.warn("null maximum value key");
      AuToBIUtils.warn(this.toString());
    }
    return best_key;
  }

  /**
   * Normalize the distribution such that it sums to 1.
   * <p/>
   * If there is no mass assigned to the distribution, this function will do nothing, and send a warning message.
   * <p/>
   * Before normalization, the object represents a histogram rather than a multinomial distribution
   */
  public void normalize() throws AuToBIException {
    Double sum = 0.0;
    for (String s : keySet()) {
      sum += get(s);
    }

    if (sum == 0) {
      throw new AuToBIException("sum is zero. no normalization possible.");
    }

    for (String s : keySet()) {
      put(s, get(s) / sum);
    }
  }
}
