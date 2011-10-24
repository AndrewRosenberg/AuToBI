/*  WavData.java

    Copyright 2009-2010 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cuny.qc.speech.AuToBI.core;

/**
 * WavData is used to store Wav file data.
 */
public class WavData {
  // Audio Data
  public int[][] raw_samples;

  public double[][] samples;
  // Number of stored channels
  public int numberOfChannels;
  // Size of each sample in bits
  public int sampleSize;
  // Number of raw_samples per second.
  public float sampleRate;

  // The time of the first sample.
  public double t0;

  // The filename containing the this audio data.
  private String filename;

  /**
   * Constructs a new WavData object with no data.
   */
  public WavData() {
    this.t0 = 0.0;
  }

  /**
   * Return the raw audio samples on a given channel.
   *
   * @param channel The requested channel.
   * @return An array of raw audio samples.
   */
  public int[] getRawData(int channel) {
    return raw_samples[channel];
  }

  /**
   * Return the normalized audio samples on a given channel.
   *
   * @param channel The requested channel.
   * @return An array of raw audio samples.
   */
  public double[] getNormalizedData(int channel) {
    return samples[channel];
  }

  /**
   * Gets the duration of the wav file in seconds.
   *
   * @return the duration of the file
   */
  public double getDuration() {
    return raw_samples[0].length / sampleRate;
  }

  /**
   * Returns the sample size in bits.
   *
   * @return the sample size
   */
  public int getSampleSize() {
    return sampleSize;
  }

  /**
   * Sets the sample size.
   *
   * @param sampleSize the desired sample size
   */
  public void setSampleSize(int sampleSize) {
    this.sampleSize = sampleSize;
  }

  /**
   * Gets the sample rate in Hertz.
   *
   * @return the sample rate
   */
  public float getSampleRate() {
    return sampleRate;
  }

  /**
   * Sets teh sample rate.
   *
   * @param sampleRate the desired sample rate
   */
  public void setSampleRate(float sampleRate) {
    this.sampleRate = sampleRate;
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
   * Gets the number of channels.
   *
   * @return the number of channels
   */
  public int getNumberOfChannels() {
    return numberOfChannels;
  }

  /**
   * Sets the number of channels.
   *
   * @param numberOfChannels teh desired number of channels.
   */
  public void setNumberOfChannels(int numberOfChannels) {
    this.numberOfChannels = numberOfChannels;
  }

  /**
   * Gets the total number of samples in the file.
   *
   * @return the number of samples
   */
  public int getNumSamples() {
    return raw_samples[0].length;
  }

  /**
   * Generates floating point samples from the raw integer samples read from the file.
   */
  public void generateSamples() {
    for (int channel = 0; channel < raw_samples.length; ++channel) {
      for (int i = 0; i < raw_samples[channel].length; ++i) {
        samples[channel][i] = raw_samples[channel][i] * 1.0 / (1 << (getSampleSize() - 1));
      }
    }
  }

  /**
   * Retries a specific sample indexed by channel and index number.
   *
   * @param channel the desired channel
   * @param index the desired index
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
}
