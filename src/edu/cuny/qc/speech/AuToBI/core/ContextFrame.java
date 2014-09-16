/*  ContextFrame.java

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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/**
 * ContextFrame is used to slide a word based frame across a list of doubles (like a pitch or intensity contour).
 * <p/>
 * The frame is set at the start of a list of words and is incremented one word at a time.  At each points statistics
 * about the contour can be queried.
 */
@SuppressWarnings("unchecked")
public class ContextFrame {
  protected List<Region> data;           // the word regions
  protected LinkedList<Double> window; // the windowed contour
  protected String feature_name;       // the feature that is analyzed
  private Integer back;                // the amount of back context
  private Integer front;               // the amount of forward context
  private Integer current;             // current point in the word regions
  private Aggregation agg;             // stores the aggregate values

  /**
   * Constructs a ContextFrame
   *
   * @param data         The words to analyze
   * @param feature_name The feature containing the contour information
   * @param back         the amount of back context
   * @param front        the amount of forward context
   */
  public ContextFrame(List<Region> data, String feature_name, Integer back, Integer front) {
    this.back = back;
    this.front = front;
    this.feature_name = feature_name;
    this.data = data;
    this.current = 0;
    init();
  }

  /**
   * Initialize the context frame.
   * <p/>
   * This sets intermediate values and initializes the windowed contour list.
   */
  public void init() {
    this.agg = new Aggregation();
    window = new LinkedList<Double>();
    for (int i = current; i < Math.min(data.size(), current + front + 1); ++i) {
      // only include data read from the same file.
      if (data.get(i).getAttribute(feature_name) instanceof Number) {
        double d = ((Number) data.get(i).getAttribute(feature_name)).doubleValue();
        window.add(d);
        agg.insert(d);
      } else {
        if ((data.get(i).getAttribute(feature_name) instanceof Contour) &&
            (((Contour) data.get(i).getAttribute(feature_name)).size() > 0)) {

          for (Pair<Double, Double> tvp : (Contour) data.get(i).getAttribute(feature_name)) {
            Double d = tvp.second;

            window.add(d);
            agg.insert(d);
          }

        }
      }
    }
  }

  /**
   * Slides the window forward one region.
   */
  public void increment() {
    current++;

    if (current > data.size() - 1) {
      window.clear();
      agg = new Aggregation();
      return;
    }

    if (data.get(0).getAttribute(feature_name) instanceof Number) {// Remove trailing value
      if (window.size() > front + back) {
        Double d = window.removeFirst();
        agg.remove(d);
      }

      // Add van value
      if (current + front < data.size()) {
        double d = ((Number) data.get(current + front).getAttribute(feature_name)).doubleValue();
        window.add(d);
        agg.insert(d);
      }
    } else if (data.get(0).getAttribute(feature_name) instanceof Contour) {
      // remove trailing values
      Integer points_to_remove = 0;
      if (current - back - 1 >= 0 && data.get(current - back - 1).getAttribute(feature_name) instanceof Contour) {
        points_to_remove = ((Contour) data.get(current - back - 1).getAttribute(feature_name)).contentSize();
      }
      for (int i = 0; i < Math.min(window.size(), points_to_remove); ++i) {
        Double d = window.removeFirst();
        agg.remove(d);
      }

      // add van values
      if (current + front < data.size()) {
        if (data.get(current + front).getAttribute(feature_name) != null) {
          for (Pair<Double, Double> tvp : (Contour) data.get(current + front).getAttribute(feature_name)) {
            Double d = tvp.second;

            window.add(d);
            agg.insert(d);
          }
        }
      }
    }
  }

  /**
   * Returns the maximum value in the context frame
   * <p/>
   * Note: this could be made more efficient by tracking the maximum value when it is added to the window
   *
   * @return the maximum value
   */
  public Double getMax() {
    if (agg.getMax() != null && Double.isNaN(agg.getMax())) {
      Double max = -(Double.MAX_VALUE);

      for (Double d : window)
        max = Math.max(d, max);
      agg.setMax(max);
    }

    return agg.getMax();
  }

  /**
   * Returns the minimum value in the window.
   * <p/>
   * Note: this could be made more efficient by tracking the minimum value when it is added to the window
   *
   * @return the minimum value
   */
  public Double getMin() {
    if (agg.getMax() != null && Double.isNaN(agg.getMin())) {
      Double min = Double.MAX_VALUE;

      for (Double d : window)
        min = Math.min(d, min);
      agg.setMin(min);
    }

    return agg.getMin();
  }

  /**
   * Calculates the mean of the window.
   *
   * @return the mean value
   */
  public Double getMean() {
    return agg.getMean();
  }

  /**
   * Calculates the standard deviation of the window.
   *
   * @return the standard deviation.
   */
  public Double getStdev() {
    return agg.getStdev();
  }

  /**
   * Calculates the size of the window
   *
   * @return the size of the window.
   */
  public int getSize() {
    return agg.getSize();
  }
}
