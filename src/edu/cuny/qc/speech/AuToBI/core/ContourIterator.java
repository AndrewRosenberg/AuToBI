/*  ContourIterateor.java

    Copyright 2009-2014 Andrew Rosenberg

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

import java.util.Iterator;

/**
 * An Iterator class to traverse Contour objects.  This is used to retrieve time value pairs for a given contour.
 *
 * @see Contour
 */
public class ContourIterator implements Iterator<Pair<Double, Double>> {
  private int i;      // the current index
  private Contour c;  // the associated contour

  /**
   * Constructs a new ContourIterator for a given Contour.
   *
   * @param contour the contour
   */
  public ContourIterator(Contour contour) {
    i = 0;
    c = contour;
  }

  /**
   * Determines if the contour has another item.
   *
   * @return true if there are more entries in the contour, false otherwise
   */
  public boolean hasNext() {
    while (i < c.size() && c.isEmpty(i)) {
      ++i;
    }
    return i < c.size();
  }

  /**
   * Retrieves the next entry in the contour.
   *
   * @return a pair containing time and value information for the next contour entry
   */
  public Pair<Double, Double> next() {
    while (c.isEmpty(i)) {
      ++i;
    }
    Pair<Double, Double> next_pair = c.getPair(i);
    ++i;
    return next_pair;
  }


  /**
   * Removes the current entry in the contour, by setting it to empty.
   */
  public void remove() {
    c.setEmpty(i);
  }
}
