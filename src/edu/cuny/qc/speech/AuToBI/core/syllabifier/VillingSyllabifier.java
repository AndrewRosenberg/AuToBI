/*  VillingSyllabifier.java

    Copyright 2009-2014 Andrew Rosenberg
    An implementation of a technique described in
      Villing et al. (2004) Automatic Blind Syllable Segmentation for Continuous Speech In: Irish Signals and Systems
      Conference 2004, 30 June - 2 July 2004, Queens University, Belfast.

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
package edu.cuny.qc.speech.AuToBI.core.syllabifier;

import edu.cuny.qc.speech.AuToBI.core.Pair;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * A class to generate pseudosyllable hypotheses.
 * <p/>
 * This is based on a procedure described in Villing et al. (2004) Automatic Blind Syllable Segmentation for
 * Continuous Speech
 * In: Irish Signals and Systems Conference 2004, 30 June - 2 July 2004, Queens University, Belfast.
 */
public class VillingSyllabifier extends Syllabifier {

  /**
   * Generate pseudosyllable regions based on the Villing (2004) envelope based approach.
   * <p/>
   * Note: currently only works with 16kHz wav files.
   * <p/>
   * Also worth observing, the second normalized envelope and 'cs' coefficients are described in the initial Villing
   * paper,
   * though are not used in the generation of bypotheses.  For completeness, the calculation of this envelope and
   * coefficients are included in comments within this function.  If we find that there is a use for this information
   * this will facilitate insertion of this information with minimal implementation.
   *
   * @param wav WavData
   * @return A list of Regions containing syllable start and end times.
   */
  public List<Region> generatePseudosyllableRegions(WavData wav) {

    // Divide the signal into three channels using low pass filters.
    // TODO: include coefficients for sampling rates other than 16k and include a selection for them. In order 8k,
    // 22.05k, 44.1k
    Pair<double[], double[]> yulewalk_coefs = getYuleWalkFilterCoefficients();
    Pair<double[], double[]> equal_loudness_filter = get150hzHighPassFilterCoefficients();
    Pair<double[], double[]> one_khz_lowpass_filter = get1khzLowPassFilterCoefficients();
    // Pair<double[], double[]> three_khz_lowpass_filter = get3khzLowPassFilterCoefficients();
    Pair<double[], double[]> smoothing_filter = getSmoothingFilterCoefficients();


    // Generate channel envelopes
    double[] signal = wav.getSamples(0);

    double[] tmp = filter(yulewalk_coefs.first, yulewalk_coefs.second, signal);
    double[] wav_3 = filter(equal_loudness_filter.first, equal_loudness_filter.second, tmp);
    double[] wav_1 = filter(one_khz_lowpass_filter.first, one_khz_lowpass_filter.second, wav_3);
    // double[] wav_2 = filter(three_khz_lowpass_filter.first, three_khz_lowpass_filter.second, wav_3);

    // Generate envelope and onset velocities
    double[] env_1 = fullWaveRectification(wav_1);
    // double[] env_2 = fullWaveRectification(wav_2);
    double[] env_3 = fullWaveRectification(wav_3);

    env_3 = reverse(filter(smoothing_filter.first, smoothing_filter.second,
        reverse(filter(smoothing_filter.first, smoothing_filter.second, env_3))));
    // env_2 = reverse(filter(smoothing_filter.first, smoothing_filter.second,
    //                        reverse(filter(smoothing_filter.first, smoothing_filter.second, env_2))));
    env_1 = reverse(filter(smoothing_filter.first, smoothing_filter.second,
        reverse(filter(smoothing_filter.first, smoothing_filter.second, env_1))));

    // Downsample to 100Hz
    env_3 = downsample(env_3, (int) (wav.sampleRate / 100)); //160);
    // env_2 = downsample(env_2, (int) (wav.sampleRate / 100));
    env_1 = downsample(env_1, (int) (wav.sampleRate / 100));// 160);

    env_3 = array_pow(env_3, 0.3);
    // env_2 = array_pow(env_2, 0.3);
    env_1 = array_pow(env_1, 0.3);

    // double[] norm_env_2 = array_div(env_2, env_3);
    double[] norm_env_1 = array_div(env_1, env_3);


    double[] env_vel = array_diff(env_3);
    double[] onset_vel = env_vel;

    // Perform half wave rectification on the onset velocity.
    for (int i = 0; i < onset_vel.length; ++i) {
      if (onset_vel[i] < 0) {
        onset_vel[i] = 0;
      }
    }

    // Identify boundaries
    ArrayList<Integer> onset_peaks = new ArrayList<Integer>();
    ArrayList<Integer> onset_starts = new ArrayList<Integer>();
    ArrayList<Integer> onset_ends = new ArrayList<Integer>();
    identfyOnsets(onset_vel, onset_peaks, onset_starts, onset_ends);

    // Score candidate boundaries
    Integer[] peaks_array = new Integer[onset_peaks.size()];
    onset_peaks.toArray(peaks_array);

    Integer[] start_array = new Integer[onset_starts.size()];
    onset_starts.toArray(start_array);

    Integer[] end_array = new Integer[onset_ends.size()];
    onset_ends.toArray(end_array);

    double[] boundary_scores = array_score(onset_vel, peaks_array, 0.01, 0.1);

    //vowel scores
    // ss score bounds were originally 0.6, 0.7
    // new is 0.3 0.7
    double[] ss = array_score(norm_env_1, end_array, 0.3, 0.7);
//    double[] cs = array_score(norm_env_1, end_array, 0.85, 0.97);
    // vps score bounds were originally 0.01, 0.1
    // new is 0.001 0.1
    double[] vps = array_score(onset_vel, peaks_array, 0.001, 0.1);

    // Note: Including the cs score leads to the system to miss detecting sonorant
    // word internal syllable boundaries (e.g. 'mama' as a two syllable word).  We find the performance to be
    // significantly improved by omitting this scoring factor.
    // As described in the original paper, vs should be calculated as follows.
    //   double[] vs = array_times(ss, array_times(array_minus(1, cs), vps));
    double[] vs = array_times(ss, vps);


    // Convolve with a window y, y = -.5 at i=100 and y = -1 at i=10
    // This has the effect of suppressing smaller scores near larger ones.
    for (int i = 0; i < vs.length; ++i) {
      double score = vs[i];
      // forward convolution region (+100ms)
      for (int j = i + 1; j < vs.length && onset_peaks.get(j) - onset_peaks.get(i) < 10; ++j) {
        double dist = onset_peaks.get(j) - onset_peaks.get(i);
        double x = 0.055555 * dist - 1.055555;
        score += vs[j] * x;
      }

      // backward convolution region (-100ms)
      for (int j = i - 1; j >= 0 && onset_peaks.get(i) - onset_peaks.get(j) < 10; --j) {
        double dist = onset_peaks.get(i) - onset_peaks.get(j);
        double x = 0.055555 * dist - 1.055555;
        score += vs[j] * x;
      }

      vs[i] = score;
    }

    // Construct a list of the best boundaries
    ArrayList<Double> boundary_list = new ArrayList<Double>();

    for (int i = 0; i < boundary_scores.length; ++i) {
      if (boundary_scores[i] > 0 && vs[i] > 0) {
        boundary_list.add(onset_starts.get(i) / 100.);
      }
    }
    boundary_list.add(wav.getDuration());

    // Convert boundaries into regions.
    ArrayList<Region> syllables = generateRegionsFromPoints(boundary_list);

    ArrayList<Region> ret = new ArrayList<Region>();
    // identify silence
    double max_e = 0.0;
    double e[] = new double[syllables.size()];
    for (int i = 0; i < syllables.size(); ++i) {
      Region r = syllables.get(i);
      double sum = 0;
      int sf = (int) (r.getStart() * 100);  // convert to indices based on a 100kHz sr.
      int ef = (int) (r.getEnd() * 100);
      for (int j = sf; j < ef; ++j)
        sum += env_3[j];
      e[i] = sum / (ef - sf);
      max_e = Math.max(e[i], max_e);
    }
    for (int i = 0; i < syllables.size(); ++i) {
      // Silence is defined as 0.3 * max average energy or lower.
      if (e[i] > 0.3 * max_e) {
        ret.add(syllables.get(i));
      }
    }

    return ret;
  }

  /**
   * Generates a list of regions based on the boundaries described boundary_list.
   * <p/>
   * Each boundary point is considered to be the start of one region and the end of the previous.
   *
   * @param boundary_list the list of boundaries.
   * @return a list of regions corresponding to the given boundaries
   */
  public ArrayList<Region> generateRegionsFromPoints(ArrayList<Double> boundary_list) {
    ArrayList<Region> regions = new ArrayList<Region>();
    if (boundary_list.size() == 0) {
      return regions;
    }
    for (int i = 1; i < boundary_list.size(); ++i) {
      regions.add(new Region(boundary_list.get(i - 1), boundary_list.get(i)));
    }
    return regions;
  }

  /**
   * Returns true if the idx is within 100ms (1600 frames) but it not equal to one of the peak frames
   *
   * @param idx         the index to evaluate
   * @param peak_frames the list of frames to compare against
   * @return true iff idx is within the window of one of the key frames.
   */
  public boolean checkWindow(Integer idx, ArrayList<Integer> peak_frames) {
    boolean response = false;
    for (int i = 0; i < peak_frames.size(); ++i) {
      if ((idx > peak_frames.get(i) - 1600 && idx < peak_frames.get(i)) ||
          (idx < peak_frames.get(i) + 1600 && idx > peak_frames.get(i))) {
        response = true;
      }
      if (idx.equals(peak_frames.get(i))) {
        return false;
      }
    }

    return response;
  }

  /**
   * Propagates three lists of indices into the onset_velocity array, onset_vel.
   * <p/>
   * 1) onset_peaks a list of local maxima within each onset region
   * 2) onset_starts a list of onset starts, the first point where the velocity is greater than zero
   * 3) onset_ends a list of onset ends, the first points after an onset where the velocity is zero.
   *
   * @param onset_vel    The onset velocity array
   * @param onset_peaks  An ArrayList of onset_peaks
   * @param onset_starts An ArrayList of onset_starts
   * @param onset_ends   An ArrayList of onset_ends
   */
  public void identfyOnsets(double[] onset_vel, ArrayList<Integer> onset_peaks, ArrayList<Integer> onset_starts,
                            ArrayList<Integer> onset_ends) {
    double max = -Double.MAX_VALUE;
    int max_idx = -1;
    boolean in_peak = false;

    for (int i = 0; i < onset_vel.length; ++i) {
      if (!in_peak) {
        if (onset_vel[i] != 0) {
          onset_starts.add(i);
          in_peak = true;
        }
      } else { // in_peak == true
        if (onset_vel[i] > max) {
          max = onset_vel[i];
          max_idx = i;
        }
        if (onset_vel[i] == 0) {
          onset_ends.add(i);
          assert max_idx != -1;
          onset_peaks.add(max_idx);
          in_peak = false;
          max = -Double.MAX_VALUE;
          max_idx = -1;
        }
      }
    }
    if (in_peak) {
      onset_ends.add(onset_vel.length - 1);
      if (max_idx == -1) {
        onset_peaks.add(onset_vel.length - 1);
      } else {
        onset_peaks.add(max_idx);
      }
    }
  }

  /**
   * Calculates the first order difference of an array.
   * <p/>
   * The resulting array contains n-1 elements corresponding to the difference between array[i+1] and array[i]
   *
   * @param array the array
   * @return the difference array
   */
  public double[] array_diff(double[] array) {
    double[] difference = new double[array.length - 1];
    for (int i = 0; i < array.length - 1; ++i) {
      difference[i] = array[i + 1] - array[i];
    }
    return difference;
  }

  /**
   * Performs element-wise array division.
   * <p/>
   * Returns an array containing num[0]/denom[0], num[1]/denom[1], ..., num[n-1]/denom[n-1]
   *
   * @param num   the numerator array
   * @param denom the denominator array
   * @return the quotient array
   */
  public double[] array_div(double[] num, double[] denom) {
    assert num.length == denom.length;

    double[] result = new double[num.length];
    for (int i = 0; i < num.length; ++i) {
      result[i] = num[i] / denom[i];
    }
    return result;
  }

  /**
   * Performs element-wise array multiplication.
   * <p/>
   * Returns an array containing array1[0]*array2[0],..., array1[n-1]*array2[n-1]
   * <p/>
   * The arrays must be the same size.
   *
   * @param array1 the first array
   * @param array2 the second array
   * @return the element-wise product of the two arrays
   */
  public double[] array_times(double[] array1, double[] array2) {
    assert array1.length == array2.length;

    double[] result = new double[array1.length];
    for (int i = 0; i < array1.length; ++i) {
      result[i] = array1[i] * array2[i];
    }
    return result;
  }

  /**
   * Calculates the difference between a constant, c, and the elements of array.
   *
   * @param c     the constant
   * @param array the array
   * @return the difference between each element and the constsnt
   */
  public double[] array_minus(double c, double[] array) {
    double[] result = new double[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = c - array[i];
    }
    return result;
  }

  /**
   * Applies Math.pow(x, pow) to every element in the array
   *
   * @param array the array
   * @param pow   the power to raise every element to
   * @return the calculated array.
   */
  public double[] array_pow(double[] array, double pow) {
    double[] result = new double[array.length];
    for (int i = 0; i < array.length; ++i) {
      result[i] = Math.pow(array[i], pow);
    }
    return result;
  }

  /**
   * Downsamples a signal by a factor of x
   * <p/>
   * Operates by retaining every x elements of signal.
   * <p/>
   * Note: should be used following a sinc(pi/x) filter to avoid aliasing effects.
   *
   * @param signal the signal to downsample
   * @param x      the downsampling factor
   * @return the downsampled signal
   */
  public double[] downsample(double[] signal, int x) {
    double[] downsampled = new double[(int) Math.ceil(signal.length * 1.0 / x)];
    for (int i = 0; i < signal.length; i += x) {
      downsampled[i / x] = signal[i];
    }

    return downsampled;
  }

  /**
   * Reverses the array
   *
   * @param array an array of doubles
   * @return a reversed array
   */
  public double[] reverse(double[] array) {
    double[] reverse = new double[array.length];
    for (int i = 0; i < array.length; ++i) {
      reverse[i] = array[array.length - 1 - i];
    }
    return reverse;
  }

  /**
   * Performs full wave rectification on the signal.
   * <p/>
   * That is, take the absolute value of every point in the signal
   *
   * @param signal the input signal
   * @return The recitified signal
   */
  public double[] fullWaveRectification(double[] signal) {
    double[] rectified = new double[signal.length];
    for (int i = 0; i < signal.length; ++i) {
      rectified[i] = Math.abs(signal[i]);
    }
    return rectified;
  }

  /**
   * Returns Yule walker filtering coefficients.
   * <p/>
   * This is based on an SPL equal loundess filter, then generating a 8-order filter coefficients using
   * Yule-Walker equations.
   *
   * @return yule walker filter coeffiencients, the first element is the numerator coefficients,
   * the second the denominator
   */
  private Pair<double[], double[]> getYuleWalkFilterCoefficients
  () {
    double[] numerator = {0.5265, -0.0254, -0.2860, -0.1221, -0.0060, 0.1186, 0.0975, -0.0884, -0.0849};

    double[] denominator = {1, -0.4667, 0.0691, -0.2148, -0.0706, 0.1136, 0.0974, -0.1088, 0.0437};
    return new Pair<double[], double[]>(numerator, denominator);
  }


  /**
   * Generates order 2 butterworth filter coefficients corresponding to a 150Hz high pass filter.
   * <p/>
   * Note: only appropriate if the sample rate is 16khz
   *
   * @return butterworth filter coeffiencients, the first element is the numerator coefficients,
   * the second the denominator
   */
  private Pair<double[], double[]> get150hzHighPassFilterCoefficients
  () {
    double[] numerator = {0.9592, -1.9184, 0.9592};

    double[] denominator = {1, -1.9167, 0.9201};
    return new Pair<double[], double[]>(numerator, denominator);
  }

  /**
   * Generate order 2 butterworth filter coefficients corresponding to a 1000Hz low pass filter.
   * <p/>
   * Note: only appropriate if the sample rate is 16khz
   *
   * @return butterworth filter coeffiencients, the first element is the numerator coefficients,
   * the second the denominator
   */
  private Pair<double[], double[]> get1khzLowPassFilterCoefficients
  () {
    double[] numerator = {0.0300, 0.0599, 0.0300};

    double[] denominator = {1, -1.4542, 0.5741};
    return new Pair<double[], double[]>(numerator, denominator);
  }

  /**
   * Generate order 2 butterworth filter coefficients corresponding to a 3000Hz low pass filter.
   * <p/>
   * Note: only appropriate if the sample rate is 16khz
   *
   * @return butterworth filter coeffiencients, the first element is the numerator coefficients,
   * the second the denominator
   */
  private Pair<double[], double[]> get3khzLowPassFilterCoefficients
  () {
    double[] numerator = {0.1867, 0.3734, 0.1867};

    double[] denominator = {1, -0.4629, 0.2097};
    return new Pair<double[], double[]>(numerator, denominator);
  }

  /**
   * Generate order 1 butterworth filter coefficients corresponding to a 12Hz low pass filter to smooth the envelope.
   * <p/>
   * Note: only appropriate if the sample rate is 16khz
   *
   * @return butterworth filter coeffiencients, the first element is the numerator coefficients,
   * the second the denominator
   */
  private Pair<double[], double[]> getSmoothingFilterCoefficients() {
    double[] numerator = {0.0024, 0.0024};

    double[] denominator = {1, -0.9953};
    return new Pair<double[], double[]>(numerator, denominator);
  }


  /**
   * Filters the input signal using filter coefficients described in the numerator and denominator vectors.
   * <p/>
   * An implementation of MATLAB filter.m
   * <p/>
   * <p/>
   * From MATLAB documentation: where x is the input, y the output, b the numerator and a the denominator
   * <p/>
   * y(n) = b(1)*x(n) + b(2)*x(n-1) + ... + b(nb+1)*x(n-nb)
   * - a(2)*y(n-1) - ... - a(na+1)*y(n-na)
   * <p/>
   * where n-1 is the filter order, which handles both FIR and IIR filters
   *
   * @param numerator   The numerator coefficient vector
   * @param denominator The denominator coefficient vector
   * @param input       The data to filter
   * @return The filtered data.
   */
  public double[] filter(double[] numerator, double[] denominator, double[] input) {
    assert numerator.length == denominator.length;

    int order = numerator.length - 1;
    double[] output = new double[input.length];

    output[0] = numerator[0] * input[0];
    for (int i = 1; i <= order; ++i) {
      output[i] = 0.0;
      for (int j = 0; j <= i; ++j) {
        output[i] += numerator[j] * input[i - j];
      }
      for (int j = 1; j <= i; ++j) {
        output[i] -= denominator[j] * output[i - j];
      }
    }

    for (int i = order + 1; i < input.length; ++i) {
      output[i] = 0.0;

      for (int j = 0; j <= order; ++j) {
        output[i] += numerator[j] * input[i - j];
      }
      for (int j = 1; j <= order; ++j) {
        output[i] -= denominator[j] * output[i - j];
      }
    }
    return output;
  }


  /**
   * Scores an array element against a predetermined range (bottom, top).
   * <p/>
   * The score function is a linear interpolation of the array index across the region between bottom and top
   *
   * @param array  the array
   * @param idx    the index to score
   * @param bottom the bottom of the range
   * @param top    the top of the range
   * @return the score
   */

  public double score(double[] array, int idx, double bottom, double top) {
    assert idx >= 0;
    assert idx < array.length;

    double s = (array[idx] - bottom) / (top - bottom);
    s = s < 0 ? 0 : s;
    s = s > 1 ? 1 : s;
    return s;
  }

  /**
   * Scores an array of indices.
   *
   * @param array     the array to score against
   * @param idx_array an array of indices to score
   * @param bottom    the bottom of the score range
   * @param top       the top of the score range
   * @return an array of scores
   * @see VillingSyllabifier#score(double[], int, double, double) score()
   */
  public double[] array_score(double[] array, Integer[] idx_array, double bottom, double top) {
    double[] scores = new double[idx_array.length];
    for (int i = 0; i < idx_array.length; ++i) {
      scores[i] = score(array, idx_array[i], bottom, top);
    }
    return scores;
  }

  public static void main(String[] args) throws Exception {

    File file = new File(args[0]);
    AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));

    WavReader reader = new WavReader();
    WavData wav = reader.read(soundIn);

    if (wav.sampleRate != 16000) {
      System.err.println(
          "Syllabifier only operates on 16khz wav files. Consider changing the sample rate with an external tool.");
    }

    VillingSyllabifier syllableFactory = new VillingSyllabifier();
    List<Region> syllables = syllableFactory.generatePseudosyllableRegions(wav);

    System.out.println("Hypothesized Syllable Regions");
    for (Region r : syllables) {
      System.out.println(r.toString());
    }
  }
}
