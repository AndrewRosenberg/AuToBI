/* SignalProcessingUtils.java
  Copyright 2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI.util;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * A class to hold Signal Processing Utilities.
 * <p/>
 * AR: this is a very generic grouping of methods.  It will probably become necessary to move some of these out to
 * more specific utility classes.
 */
public class SignalProcessingUtils {
  /**
   * Constructs a Hann or Hanning window.
   * <p/>
   * This is usually used to convolve with a signal.
   * <p/>
   * Hann(x) = .5 * 1 - cos(2 pi (x / (N-1))
   *
   * @param hanning_window_samples The size of the hanning window
   * @return The convolution window
   */
  public static double[] constructHanningWindow(int hanning_window_samples) {

    double[] window = new double[hanning_window_samples];
    for (int i = 0; i < hanning_window_samples; ++i) {
      double phase = i * 1.0 / (hanning_window_samples - 1);
      window[i] = 0.5 + 0.5 * Math.cos(Math.PI * phase);
    }

    return window;
  }

  /**
   * Constructs a half Hann window from 0 up to hanning_window_samples of a hanning_window_samples*2 window.
   * <p/>
   * This is usually used to convolve with a signal.
   * <p/>
   * Hann(x) = .5 * 1 - cos(2 pi (x / (N-1))
   *
   * @param hanning_window_samples The size of the hanning window
   * @return The convolution window
   */
  public static double[] constructHalfHanningWindow(int hanning_window_samples) {

    double[] window = new double[hanning_window_samples];
    for (int i = 0; i < hanning_window_samples; ++i) {
      double phase = i * 1.0 / hanning_window_samples;
      window[i] = 0.5 + 0.5 * Math.cos(Math.PI * phase);
    }

    return window;
  }

  /**
   * Constructs a Hamming window.
   * <p/>
   * This is usually used to convolve with a signal.
   * <p/>
   * Hann(x) = .5 * 1 - cos(2 pi (x / (N-1))
   *
   * @param hamming_window_samples The size of the Hamming window
   * @return The convolution window
   */
  public static double[] constructHammingWindow(int hamming_window_samples) {
    double[] window = new double[hamming_window_samples];
    for (int i = 0; i < hamming_window_samples; ++i) {
      double phase = i * 1.0 / (hamming_window_samples - 1);
      window[i] = 0.54 + 0.46 * Math.cos(Math.PI * phase);
    }
    return window;
  }

  /**
   * Constructs a half Hamming window from 0 to the peak of a hamming_window_samples*2 window.
   * <p/>
   * This is usually used to convolve with a signal.
   * <p/>
   * Hann(x) = .5 * 1 - cos(2 pi (x / (N-1))
   *
   * @param hamming_window_samples The size of the Hamming window
   * @return The convolution window
   */
  public static double[] constructHalfHammingWindow(int hamming_window_samples, boolean rev) {
    double[] window = new double[hamming_window_samples];
    for (int i = 0; i < hamming_window_samples; ++i) {
      double phase = i * 1.0 / hamming_window_samples;
      int tgt_i = i;
      if (rev) {
        tgt_i = hamming_window_samples - i - 1;
      }
      window[tgt_i] = 0.54 + 0.46 * Math.cos(Math.PI * phase);
    }
    return window;
  }

  /**
   * Convolves a signal with a window function.
   *
   * @param signal The signal
   * @param window The window function
   * @return The convolved data
   */
  public static double[] convolveSignal(double[] signal, double[] window) {
    double[] convolved = new double[signal.length];
    for (int i = 0; i < Math.min(signal.length, window.length); ++i) {
      convolved[i] = signal[i] * window[i];
    }
    for (int i = window.length; i < signal.length; ++i) {
      convolved[i] = 0.0;
    }
    return convolved;
  }

  /**
   * Calculates the absolute value squared for each element of a array produced by DoubleFFT_1D
   * This is a single array containing the real and imaginary components of the FFT in
   * position 2*i and 2*i+1.
   *
   * @param data The complex array
   * @return an array of the same size containing the absolute value squared
   */
  public static double[] absoluteValueSquared(double[] data) {
    double[] result = new double[data.length / 2];
    for (int i = 2; i < data.length - 1; i += 2) {
      result[i / 2] = data[i] * data[i] + (data[i + 1] * data[i + 1]);
    }
    return result;
  }

  /**
   * Extracts the power spectrum from a signal
   *
   * @param nCoef the number of coefficients
   * @param s     the signal
   * @return the power spectrum, an array of nCoef doubles
   */
  public static double[] getPowerSpectrum(int nCoef, double[] s) {
    DoubleFFT_1D window_fft = new DoubleFFT_1D(nCoef);
    window_fft.realForward(s);

    return absoluteValueSquared(s);
  }

  /**
   * Resizes an array.
   * <p/>
   * This is used to deliver an array with size = 2^d to the FFT operation
   *
   * @param data         The data
   * @param desired_size The desired size of the array
   * @return a new array of complex values
   */
  public static double[] resizeArray(double[] data, int desired_size) {
    double[] array = new double[desired_size];
    System.arraycopy(data, 0, array, 0, Math.min(desired_size, data.length));
    for (int i = data.length; i < desired_size; ++i) {
      array[i] = 0.0;
    }
    return array;
  }
}
