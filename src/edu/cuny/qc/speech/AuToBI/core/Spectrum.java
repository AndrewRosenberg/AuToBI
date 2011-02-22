/*  Spectrum.java

    Copyright (c) 2009-2010 Andrew Rosenberg

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

import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

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
   * Returns a specific power value in the Spectrum
   *
   * @param time_idx the time index to retrieve
   * @param freq     the frequency to retrieve
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
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException
   *          when invalid times are requested
   */
  public Spectrum getSlice(double time_1, double time_2) throws AuToBIException {
    if (time_1 >= time_2) {
      throw new AuToBIException("Starting time is after ending time. (" + time_1 + " >= " + time_2 + ")");
    }
    if (data.length == 0)
      return null;

    int index_1 = (int) Math.floor((time_1 - starting_time) / frame_size);
    int index_2 = (int) Math.ceil((time_2 - starting_time) / frame_size);

    index_1 = Math.max(0, Math.min(data.length -1, index_1));
    index_2 = Math.max(0, Math.min(data.length -1, index_2));

    double[][] slice_data = new double[index_2 - index_1][data[0].length];

    for (int i = index_1; i < index_2; ++i) {
      slice_data[i - index_1] = data[i];
    }

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
      for (int j = (int) Math.max(0, Math.floor(toFreqBin(low_freq)));
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
   * Retrieves a list of powers in a band as a List.
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
  public Contour getPowerList(double freq_1, double freq_2, boolean log_values) throws AuToBIException {
    double[] power = getPowerInBand(freq_1, freq_2, log_values);

    Contour power_list = new Contour(starting_time, frame_size, power);

    return power_list;
  }

  /**
   * Retrieves a list of powers in a band as a List.
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
  public Contour getPowerTiltList(double freq_1, double freq_2, boolean log_values) throws AuToBIException {
    double[] power_band = getPowerInBand(freq_1, freq_2, log_values);
    double[] power = getPower(log_values);

    Contour power_list = new Contour(starting_time, frame_size, power.length);
    for (int i = 0; i < power.length; ++i) {
      power_list.set(i, power_band[i] / power[i]);
    }
    return power_list;
  }
}
