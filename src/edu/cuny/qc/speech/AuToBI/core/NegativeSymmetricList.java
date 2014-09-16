/*  NegativeSymmetricList.java

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

import java.util.ArrayList;

/**
 * NegativeSymmetricList is a wrapper around an ArrayList<Double> that is symmetric for positive and negative indices.
 * <p/>
 * This allows for retrieval of negative indices, since they are the same as the corresponding positive index.
 * <p/>
 * This could also be accomplished using ArrayList.get(Math.abs(i)).
 */
public class NegativeSymmetricList extends ArrayList<Double> {
  /**
   * Retrieves a value from the list.
   *
   * @param i the index of the value
   * @return the associated value
   */
  public Double get(int i) {
    if (i >= 0) {
      return super.get(i);
    } else {
      return super.get(-i);
    }
  }

  /**
   * Sets the value of an index in the list.
   * <p/>
   * Since the list is symmetric, the value for the negative index is also set.
   *
   * @param i     the index
   * @param value the new value
   * @return the element previously at position i
   */
  public Double set(int i, Double value) {
    if (i >= 0) {
      return super.set(i, value);
    } else {
      return super.set(-i, value);
    }
  }
}
