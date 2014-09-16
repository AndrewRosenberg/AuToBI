/*  Spectrum.java

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

/**
 * Spectrum objects contain acoustic spectrum information.
 * <p/>
 * The spectrum is a three dimensional representation of acoustic energy, containing power indexed by time and
 * frequency.
 */
public class Spectrum {
  private double[][] data;
  private double starting_time;
  private double frame_size;
  private double freq_resolution;

  /**
   * Constructs a Spectrum object.
   *
   * @param data            The spectrum data
   * @param starting_time   The time of the initial index
   * @param frame_size      The size (in seconds) of each frame
   * @param freq_resolution the frequency resolution for the spectrum
   */
  public Spectrum(double[][] data, double starting_time, double frame_size, double freq_resolution) {
    this.data = data;
    this.starting_time = starting_time;
    this.frame_size = frame_size;
    this.freq_resolution = freq_resolution;
  }

  /**
   * Returns the number of frames in the Spectrum.
   *
   * @return the number of frames
   */
  public int numFrames() {
    return data.length;
  }

  /**
   * Returns the size of the frequency dimension in the Spectrum
   *
   * @return the number of frequencies
   */
  public int numFreqs() {
    return data[0].length;
  }

  /**
   * Returns the starting time of the Spectrum
   *
   * @return the starting time
   */
  public double getStartingTime() {
    return starting_time;
  }

  /**
   * Returns the frame size of the Spectrum
   *
   * @return the frame size
   */
  public double getFrameSize() {
    return frame_size;
  }


  /**
   * Returns a specific power value in the Spectrum
   *
   * @param time_idx the time index to retrieve
   * @param freq     the frequency index to retrieve
   * @return the power in the spectrum
   */
  public double get(int time_idx, int freq) {
    return data[time_idx][freq];
  }

  /**
   * Returns one spectrum time sample
   *
   * @param time_idx The time index to retrieve
   * @return the power in the spectrum
   */
  public double[] get(int time_idx) {
    return data[time_idx];
  }

  /**
   * Return a subset of the spectrum between two times.
   *
   * @param time_1 The lower time (in seconds).
   * @param time_2 The upper time (in seconds).
   * @return A subspectrum between two times
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException when invalid times are requested
   */
  public Spectrum getSlice(double time_1, double time_2) throws AuToBIException {
    if (time_1 >= time_2) {
      throw new AuToBIException("Starting time is after ending time. (" + time_1 + " >= " + time_2 + ")");
    }
    if (data.length == 0) {
      return null;
    }

    int index_1 = (int) Math.ceil((time_1 - starting_time) / frame_size);
    // index_2 will point one index position higher than necessary.
    int index_2 = (int) Math.ceil((time_2 - starting_time) / frame_size);

    if (index_1 >= data.length) {
      return new Spectrum(new double[][]{}, index_1 * frame_size + starting_time, frame_size, freq_resolution);
    }

    index_1 = Math.max(0, index_1);
    index_2 = Math.max(0, Math.min(data.length, index_2));

    double[][] slice_data = new double[index_2 - index_1][data[0].length];

    System.arraycopy(data, index_1, slice_data, 0, index_2 - index_1);

    return new Spectrum(slice_data, index_1 * frame_size + starting_time, frame_size, freq_resolution);
  }


  /**
   * Retrieves an array of total power in the spectrum between two frequencies.
   * <p/>
   * Returns an array of doubles, one for each frame.
   * <p/>
   * Note: this is total power, not power density.  To construct power density from this list normalize the power by
   * Math.ceil(freq_2) - Math.floor(freq_1)
   *
   * @param low_freq   The bottom frequency
   * @param high_freq  The top frequency
   * @param log_values If true, return log power, else return raw power.
   * @return An array of powers in a frequency band across the whole spectrum.
   * @throws AuToBIException if an invalid band is requested.
   */
  public double[] getPowerInBand(double low_freq, double high_freq, boolean log_values) throws AuToBIException {
    if (low_freq > high_freq) {
      throw new AuToBIException(
          "Bottom frequency is greater than top frequency. (" + low_freq + " > " + high_freq + ")");
    }
    double[] power_spectrum = new double[data.length];
    for (int i = 0; i < data.length; ++i) {
      for (int j = (int) Math.max(0, Math.ceil(toFreqBin(low_freq)));
           j < (int) Math.min(data[i].length, Math.ceil(toFreqBin(high_freq))); ++j) {
        power_spectrum[i] += data[i][j];
      }
      if (log_values) {
        power_spectrum[i] = Math.log(power_spectrum[i]);
      }
    }

    return power_spectrum;
  }

  /**
   * Identify the spectrum frequency bin corresponding to a given frequency
   * <p/>
   * Note: Returns a double to allow a user function to decide whether to round up or down.
   *
   * @param frequency the frequency
   * @return the frequency bin
   */
  private double toFreqBin(double frequency) {
    return frequency / freq_resolution;
  }

  /**
   * Retrieves an array of total power in the spectrum.
   * <p/>
   * Returns an array of doubles, one for each frame.
   * <p/>
   * Note: this is total power, not power density.  To construct power density from this list normalize the power by
   * Math.ceil(freq_2) - Math.floor(freq_1)
   *
   * @param log_values If true, return log power, else return raw power.
   * @return An array of powers in a frequency band across the whole spectrum.
   * @throws AuToBIException if an invalid band is requested.
   */
  public double[] getPower(boolean log_values) throws AuToBIException {
    double[] power_spectrum = new double[data.length];
    for (int i = 0; i < data.length; ++i) {
      for (int j = 0; j < data[i].length; ++j) {
        power_spectrum[i] += data[i][j];
      }
      if (log_values) {
        power_spectrum[i] = Math.log(power_spectrum[i]);
      }
    }

    return power_spectrum;
  }

  /**
   * Retrieves a list of powers in a band as a Contour.
   * <p/>
   * Note: this is total power, not power density.  To construct power density from this list normalize the power by
   * Math.ceil(freq_2) - Math.floor(freq_1)
   *
   * @param freq_1     The bottom frequency
   * @param freq_2     The top frequency
   * @param log_values If true, return log power, else return raw power.
   * @return An array of powers in a frequency band across the whole spectrum.
   * @throws AuToBIException if an invalid band is requested.
   */
  public Contour getPowerContour(double freq_1, double freq_2, boolean log_values) throws AuToBIException {
    double[] power = getPowerInBand(freq_1, freq_2, log_values);

    Contour power_contour = new Contour(starting_time, frame_size, power);

    return power_contour;
  }

  /**
   * Retrieves a list of power tilts in a band as a Contour.
   * <p/>
   * Power tilt is calculated as the power within a particular frequency divided by the power in the whole frame
   *
   * @param freq_1     The bottom frequency
   * @param freq_2     The top frequency
   * @param log_values If true, return log power, else return raw power.
   * @return An array of powers in a frequency band across the whole spectrum.
   * @throws AuToBIException if an invalid band is requested.
   */
  public Contour getPowerTiltContour(double freq_1, double freq_2, boolean log_values) throws AuToBIException {
    double[] power_band = getPowerInBand(freq_1, freq_2, log_values);
    double[] power = getPower(log_values);

    Contour power_list = new Contour(starting_time, frame_size, power.length);
    for (int i = 0; i < power.length; ++i) {
      power_list.set(i, power_band[i] / power[i]);
    }
    return power_list;
  }

  /**
   * Generates the spectral balance from a spectrum object.
   * <p/>
   * The spectral balance contour is comprised of the slope of the spectrum at each frame.
   *
   * @return a contour containing the spectral balance for each frame.
   */
  public Contour getSpectralTiltContour() {
    double[] spectral_tilt = new double[numFrames()];

    for (int i = 0; i < numFrames(); ++i) {
      spectral_tilt[i] = calculateTilt(data[i], true);
    }

    return new Contour(starting_time, frame_size, spectral_tilt);
  }

  /**
   * Calculates the slope of an array of doubles.
   * <p/>
   * Used in the calculation of spectral tilt.
   *
   * @param frame the array of doubles
   * @param log   if true, calculate tilt using log power
   * @return the slope of the array.
   */
  private double calculateTilt(double[] frame, boolean log) {
    double n = frame.length;
    double s_x = 0.0;
    double s_y = 0.0;
    double s_xy = 0.0;
    double s_xx = 0.0;

    for (int i = 0; i < n; ++i) {
      double x = i * freq_resolution;
      double y = frame[i];
      if (log) {
        if (y == 0) continue;
        y = Math.log(y);
      }
      s_x += x;
      s_y += y;
      s_xx += x * x;
      s_xy += x * y;
    }

    return (s_xy - (s_x * s_y) / n) / (s_xx - s_x * s_x / n);
  }
}
