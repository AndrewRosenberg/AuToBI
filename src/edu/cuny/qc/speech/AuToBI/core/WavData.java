/*  WavData.java

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

import java.util.Arrays;

/**
 * WavData is used to store Wav file data.
 */
public class WavData {
  public double[][] samples;   // Normalized Audio Data
  public int numberOfChannels; // Number of stored channels
  public int sampleSize;       // Size of each sample in bits
  public float sampleRate;     // Number of raw_samples per second.
  public double t0;            // The time of the first sample.
  private String filename;     // The filename containing the this audio data.

  /**
   * Constructs a new WavData object with no data.
   */
  public WavData() {
    this.t0 = 0.0;
  }

  /**
   * Gets the duration of the wav file in seconds.
   *
   * @return the duration of the file
   */
  public double getDuration() {
    return samples[0].length / sampleRate;
  }

  /**
   * Gets the frame size in seconds.
   *
   * @return the frame size
   */
  public float getFrameSize() {
    return 1 / sampleRate;
  }

  /**
   * Gets the total number of samples in the file.
   *
   * @return the number of samples
   */
  public int getNumSamples() {
    return samples[0].length;
  }

  /**
   * Retries a specific sample indexed by channel and index number.
   *
   * @param channel the desired channel
   * @param index   the desired index
   * @return the sample stored at the specified channel and index
   */
  public double getSample(int channel, int index) {
    return samples[channel][index];
  }

  /**
   * Retrieves the full list of samples stored in a give channel.
   *
   * @param channel the desired channel
   * @return the wav samples in the specified channel
   */
  public double[] getSamples(int channel) {
    return samples[channel];
  }

  /**
   * Get the filename where the data was found.
   *
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the filename.
   *
   * @param filename the filename
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * Return a slice of samples from the wav data, between start and end.
   *
   * @param start the starting index of the sample
   * @param end   the ending (inclusive) index of the sample
   * @return
   */
  public double[] getSamples(int channel, int start, int end) {
    // Takes a slice of the samples array with some simple bounds checking.
    return Arrays.copyOfRange(samples[channel], Math.max(0, start), Math.min(samples[channel].length, end));
  }
}
