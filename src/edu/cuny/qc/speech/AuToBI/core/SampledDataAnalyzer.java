/*  SampledDataAnalyzer.java

    Copyright (c) 2009-2014 Andrew Rosenberg

    Inspired by Sampled.c distributed as part of the Praat package Copyright (C) 1992-2008 Paul Boersma

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

/**
 * A class to implement utility functions used by factory classes that perform sampled data analysis.
 * <p/>
 * These include PitchExtractor and IntensityExtractor.  This class mostly contains utility functions to
 * convert from indices to times and vice versa.
 */
public abstract class SampledDataAnalyzer {
  protected WavData wav;

  /**
   * Convert an array index to the time domain given an initial time, t0, and a timestep, timeStep.
   *
   * @param t0        the initial time
   * @param time_step the timestep for each array element
   * @param i         the index
   * @return the corresponding time value
   */
  protected double indexToX(double t0, double time_step, int i) {
    return t0 + i * time_step;
  }

  /**
   * Convert a time to an array index -- rounding down.
   *
   * @param t0        the time of the initial index in the array
   * @param time_step the timestep between each array element
   * @param x         the time to convert
   * @return the corresponding index -- rounded down to the closest integer
   */
  protected int xToLowIndex(double t0, double time_step, double x) {
    return (int) Math.floor((x - t0) / time_step);
  }

  /**
   * Convert a time to an array index -- rounding up.
   *
   * @param t0        the time of the initial index in the array
   * @param time_step the timestep between each array element
   * @param x         the time to convert
   * @return the corresponding index -- rounded down to the closest integer
   */
  protected int xToHighIndex(double t0, double time_step, double x) {
    return (int) Math.ceil((x - t0) / time_step);
  }

  /**
   * Convert a time to the nearest array index.
   *
   * @param t0        the time of the initial index in the array
   * @param time_step the timestep between each array element
   * @param x         the time to convert
   * @return the corresponding index -- rounded down to the closest integer
   */
  protected int xToNearestIndex(double t0, double time_step, double x) {
    return (int) Math.floor((x - t0) / time_step + 0.5);
  }

  /**
   * Generate the number of frames and the midpoint in time for the first frame.
   * <p/>
   * These parameters are calculated using the wav file to be analysed, the time step of the desired analysis and the
   * length (in time) of the window of analysis.
   *
   * @param time_step   The time step of the desired analysis.
   * @param window_size The length of the analysis window.
   * @return a Pair containing the number of frames (first) and the initial time (second)
   */
  protected Pair<Integer, Double> getNFramesAndStartTime(double time_step, double window_size) {
    Pair<Integer, Double> pair = new Pair<Integer, Double>();
    // Determine the number of frames
    pair.first = (int) (Math.floor((wav.getDuration() - window_size) / time_step) + 1);

    // Determine the midpoint of the first frame
    double mid_time = 0.5 * wav.getDuration() - 0.5 * wav.getFrameSize();
    // mid time of the wav file is the midpoint of the file, minus half a wav frame.
    pair.second = mid_time - 0.5 * (pair.first * time_step) + 0.5 * time_step;
    // identify the mid point of the first frame by calculating back from the middle by the number of analysis frames
    // and then adding half an analysis frame.
    return pair;
  }

  /**
   * Returns a frame of acoustic information from the wave data.
   * <p/>
   * The frame index is independent of the size of the window to allow for the extraction of overlapping frames
   *
   * @param starting_sample The starting frame
   * @param frame_index     The requested frame index
   * @param frame_samples   The number of samples in the frame
   * @param window_samples  The number of samples in the window.
   * @return A single analysis frame from the wave data.
   */
  protected double[] getWindowedFrame(int starting_sample, int frame_index, int frame_samples, int window_samples) {
    double[] frame = new double[window_samples];

    int low_idx = starting_sample + frame_index * frame_samples - window_samples / 2;
    for (int j = 0; j < window_samples; ++j) {
      frame[j] = wav.getSample(0, j + low_idx);
    }

    return frame;
  }

}
