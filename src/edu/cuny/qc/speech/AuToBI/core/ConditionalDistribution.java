/*  ConditionalDistribution.java

    Copyright (c) 2009-20144 Andrew Rosenberg

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

import java.util.HashMap;

/**
 * A ConditionalDistribution is a structure to associate strings and distributions.
 * <p/>
 * This class can be used to calculate conditional probability distributions, or simply count tokens conditioned on a
 * string.
 *
 * @see Distribution
 */
public class ConditionalDistribution extends HashMap<String, Distribution> {


  /**
   * Adds a key value pair to the conditional distribution.
   *
   * @param key   the key
   * @param value the value
   */
  public void add(String key, String value) {
    add(key, value, 1);
  }

  /**
   * Adds a key value pair to the conditional distribution with a specified weight.
   *
   * @param key    the key
   * @param value  the value
   * @param weight the weight
   */
  public void add(String key, String value, double weight) {
    if (!containsKey(key)) {
      put(key, new Distribution());
    }

    get(key).add(value, weight);
  }

  /**
   * Normalizes the conditional distributions.
   */
  public void normalize() throws AuToBIException {
    for (String key : keySet()) {
      get(key).normalize();
    }
  }
}
