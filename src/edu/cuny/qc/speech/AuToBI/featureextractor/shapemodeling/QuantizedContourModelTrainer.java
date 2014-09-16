/*  QuantizedContourModelTrainer.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.ConditionalDistribution;
import edu.cuny.qc.speech.AuToBI.core.Pair;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.List;

/**
 * A class to train a QuantizedContourModel.
 */
public class QuantizedContourModelTrainer {

  public int time_bins; // the number of time bins
  public int value_bins; // the number of value bins
  public double omit_rate; // omit the top and bottom n% of data points when quantizing value

  /**
   * Constructs a new QuantizedContourModelTrainer using a specified number of time and value bins.
   * <p/>
   * Also the top and bottom x% of values across all bins are omitted.
   *
   * @param time_bins  the number of time bins
   * @param value_bins the number of value bins
   * @param omit_rate  the percentage of high and low points to omit.
   */
  public QuantizedContourModelTrainer(int time_bins, int value_bins, double omit_rate) {
    this.time_bins = time_bins;
    this.value_bins = value_bins;
    this.omit_rate = omit_rate;
  }

  /**
   * Trains a QuantizedContourModel from a set of training contours.
   *
   * @param contours the training contours
   * @return a quantized contour model
   */
  public QuantizedContourModel train(List<Contour> contours) {

    Pair<Double, Double> limits = identifyLimits(contours);
    ContourQuantizer cq = new ContourQuantizer(time_bins, value_bins, limits.first, limits.second);
    ConditionalDistribution[] slices = new ConditionalDistribution[time_bins];
    for (int i = 0; i < time_bins; ++i) {
      slices[i] = new ConditionalDistribution();
    }

    for (Contour c : contours) {
      try {
        // quantize the contour.
        int[] quantized = cq.quantize(c);
        assert quantized.length == slices.length;

        String prev_value = "";
        for (int i = 0; i < quantized.length; ++i) {
          String value = Integer.toString(quantized[i]);
          slices[i].add(prev_value, value);
          prev_value = value;
        }

      } catch (ContourQuantizerException e) {
        e.printStackTrace();
      }
    }

    // Normalize to use as a QCM.
    for (ConditionalDistribution cd : slices) {
      try {
        cd.normalize();
      } catch (AuToBIException e) {
        AuToBIUtils.error("Error normalizing QCM distributions: " + e.getMessage());
      }
    }

    return new QuantizedContourModel(cq, slices);
  }

  /**
   * Identifies the upper and lower limits of the quantized contour model based on the training contours and the trainer
   * omit rate.
   *
   * @param contours the training contours.
   * @return a pair containing the lower and upper limits.
   */
  protected Pair<Double, Double> identifyLimits(List<Contour> contours) {
    int n = 0;
    for (Contour c : contours) {
      n += c.size();
    }

    int omit_size = (int) Math.floor(n * omit_rate + 1);

    double[] top_n = new double[omit_size];
    for (int i = 0; i < top_n.length; ++i)
      top_n[i] = -Double.MAX_VALUE;

    double[] bottom_n = new double[omit_size];
    for (int i = 0; i < bottom_n.length; ++i)
      bottom_n[i] = Double.MAX_VALUE;

    double high_limit = -Double.MAX_VALUE;
    double low_limit = Double.MAX_VALUE;

    for (Contour c : contours) {

      for (Pair<Double, Double> tvp : c) {
        double v = tvp.second;

        if (v > high_limit) {
          boolean set = false;
          double new_limit = Double.MAX_VALUE;
          for (int i = 0; i < top_n.length; ++i) {
            if (!set && top_n[i] == high_limit) {
              top_n[i] = v;
              set = true;
            }
            if (top_n[i] < new_limit) {
              new_limit = top_n[i];
            }
          }
          high_limit = new_limit;
        }

        if (v < low_limit) {
          boolean set = false;
          double new_limit = -Double.MAX_VALUE;
          for (int i = 0; i < bottom_n.length; ++i) {
            if (!set && bottom_n[i] == low_limit) {
              bottom_n[i] = v;
              set = true;
            }
            if (bottom_n[i] > new_limit) {
              new_limit = bottom_n[i];
            }
          }
          low_limit = new_limit;
        }

      }

    }

    return new Pair<Double, Double>(low_limit, high_limit);
  }
}
