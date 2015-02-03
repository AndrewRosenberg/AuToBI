/* FFVExtractor.java

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
package edu.cuny.qc.speech.AuToBI.core.signalprocessing;

import edu.cuny.qc.speech.AuToBI.SpectrumExtractor;
import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.SignalProcessingUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Implements Kornel Laskowski et al.'s Fundamental Frequency Variation (FFV) Features.
 * <p/>
 */
public class FFVExtractor {

  private double tsep;  // the separation between the two windows (in seconds)
  private double tint;  // the length of the internal window (in seconds)
  private double text;  // the length of the external window (in seconds)
  private double tstep; // ffv frame size (in seconds)

  private static final int FFT_COEFS = 512;
  private double[][] filterbank; // filterbank parameters
  private boolean[] filterbank_mask; // a mask determining if any of the filterbank entries are nonzero

  /**
   * Constructs a new FFVCalculator with appropriate parameters.
   *
   * @param tstep ffv frame size
   * @param tsep  separation between window peaks.
   * @param tint  length of internal Hann Window
   * @param text  length of external Hamming Window
   */
  public FFVExtractor(double tstep, double tsep, double tint, double text) {
    this.tstep = tstep;
    this.tsep = tsep;
    this.tint = tint;
    this.text = text;

    // Construct the filterbank
    constructFilterbank();
  }

  double[][] calculateFFV(WavData wav, int channel) {
    int sint = timeToSample(tint, wav.sampleRate);
    int sext = timeToSample(text, wav.sampleRate);
    int ssep = timeToSample(tsep, wav.sampleRate);
    int sstep = timeToSample(tstep, wav.sampleRate);

    System.err.println("Number of window samples: " + (sext + sint));
    System.err.println("Step size: " + sstep);

    double[] l_win = constructAsymFFVWindow(sint, sext);
    double[] r_win = new double[l_win.length];
    for (int i = 0; i < l_win.length; ++i) {
      r_win[l_win.length - i - 1] = l_win[i];
    }

    int s0 = sext + ssep / 2;

    int num_frames = (wav.getNumSamples() - 2 * s0) / sstep + 1;
    System.err.println("Number of FFV frames: " + num_frames);

    double[][] ffv_out = new double[num_frames][filterbank.length];

    int i = 0;
    for (int si = s0; si < wav.getNumSamples() - s0; si += sstep, i++) {
      double[] l_samps = wav.getSamples(channel, si - ssep / 2 - sext, si - ssep / 2 + sint);
      double[] r_samps = wav.getSamples(channel, si + ssep / 2 - sint, si + ssep / 2 + sext);

      // convert to integer representation and do pre-emphasis
      for (int j = l_samps.length - 1; j >= 0; --j) {
        if (j == l_samps.length - 1) {
          l_samps[j] = l_samps[j] * (1 << (wav.sampleSize - 1));
          r_samps[j] = r_samps[j] * (1 << (wav.sampleSize - 1));
        }
        if (j == 0) {
          l_samps[j] = 0.;
          r_samps[j] = 0.;
        } else {
          l_samps[j - 1] = l_samps[j - 1] * (1 << (wav.sampleSize - 1));
          r_samps[j - 1] = r_samps[j - 1] * (1 << (wav.sampleSize - 1));

          l_samps[j] = l_samps[j] - .97 * l_samps[j - 1];
          r_samps[j] = r_samps[j] - .97 * r_samps[j - 1];
        }
      }

      double[] l_fft = calculateWindowedSpectrum(l_samps, l_win, FFT_COEFS * 2);
      double[] r_fft = calculateWindowedSpectrum(r_samps, r_win, FFT_COEFS * 2);

      double[] ffv_spec = calculateFFVSpectrum(l_fft, r_fft);

      ffv_out[i] = filterbankFFV(ffv_spec, filterbank);
      System.err.format("Calculated frame %d of %d\r", i + 1, num_frames);
    }
    return ffv_out;
  }

  /**
   * Applies each filter in the N element filterbank. Returns an N element vector of the filtered signal.
   *
   * @param signal     the signal
   * @param filterbank the filterbank
   * @return a vector of filtered signals
   */
  private double[] filterbankFFV(double[] signal, double[][] filterbank) {
    double[] out = new double[filterbank.length];

    for (int i = 0; i < filterbank.length; i++) {
      out[i] = 0;
      for (int j = 0; j < filterbank[i].length; j++) {
        out[i] += signal[j] * filterbank[i][j];
      }
    }

    return out;
  }

  /**
   * Calculates the power spectrum of a windowed signal.
   *
   * @param signal the signal
   * @param win    the window
   * @param nCoef  the number of coefficients
   * @return the power spectrum
   */
  private double[] calculateWindowedSpectrum(double[] signal, double[] win, int nCoef) {
    double[] win_sig = SignalProcessingUtils.convolveSignal(signal, win);
    win_sig = SignalProcessingUtils.resizeArray(win_sig, nCoef);
    return SignalProcessingUtils.getPowerSpectrum(nCoef, win_sig);
  }

  /**
   * Constructs the asymmetrical FFV window function.
   * <p/>
   * The FFV comparison compares two windowed signals, where the window function is a half hamming window and a half
   * hann window, where the length of the two half windows are not necessarily equal.
   * <p/>
   * <p/>
   * |---- ext ---------|---- int ----|
   * Hamming             Hann
   *
   * @param sint size of the internal window
   * @param sext size of the external window
   * @return the half hamming, half hann window
   */
  private double[] constructAsymFFVWindow(int sint, int sext) {
    double[] external = SignalProcessingUtils.constructHalfHammingWindow(sext, true);
    double[] internal = SignalProcessingUtils.constructHalfHanningWindow(sint);

    double[] out = new double[external.length + internal.length];
    System.arraycopy(external, 0, out, 0, external.length);
    System.arraycopy(internal, 0, out, external.length, internal.length);

    return out;
  }


  /**
   * Converts a time to a sample index.
   * <p/>
   * This assumes that the 0th sample is at time 0.
   *
   * @param time       the time in second
   * @param sampleRate the sample rate of the signal in frames per second.
   * @return the corresponding index
   */
  private int timeToSample(double time, float sampleRate) {
    return (int) Math.floor(time * sampleRate);
  }


  /**
   * Calculates the FFV spectrum given two windowed power spectra.
   * <p/>
   * The FFV spectrum has the same length as the right and left spectra.
   *
   * @param l_pow the left power spectrum
   * @param r_pow the right power spectrum
   * @return the FFV spectrum
   */
  public double[] calculateFFVSpectrum(double[] l_pow, double[] r_pow) {
    int n = l_pow.length;
    double[] ffv = new double[n];

    for (int r = -n / 2; r < n / 2; r++) {

      // This frame won't be used in the filterbanked calculation, so skip it. (improves runtime by about 3x)
      // TODO: allow a user to generate the full FFV spectrum, not only the filterbanked version.
      if (!filterbank_mask[r + n / 2]) {
        ffv[r + n / 2] = 0.;
        continue;
      }

      double rho = Math.pow(2, 4.0 * -Math.abs(r) / n * (tsep / 0.008));

      double num = 0;
      double denom_l = 0;
      double denom_r = 0;
      if (r < 0) {  // contraction (squeeze the right frame)
        for (int k = 0; k < n; k++) {

          double right = interpolate(r_pow, rho, k);
          num += Math.sqrt(l_pow[k]) * right;

          denom_l += l_pow[k];
          denom_r += right * right;
        }
      } else {  // dilation (squeeze the left frame)
        for (int k = 0; k < n; k++) {
          double left = interpolate(l_pow, rho, k);
          num += left * Math.sqrt(r_pow[k]);

          denom_l += left * left;
          denom_r += r_pow[k];
        }
      }
      ffv[r + n / 2] = num / Math.sqrt(denom_l * denom_r);
    }

    return ffv;
  }

  /**
   * Interpolate the power spectrum for a given set of ffv and fft indices.
   *
   * @param pow the right power spectrum
   * @param rho   ffv dilation coefficient
   * @param k   fft index
   * @return the interpolated power spectrum for the given indices
   */
  private double interpolate(double[] pow, double rho, int k) {
    double rho_k = rho * k;
    int low_idx = (int) Math.floor(rho_k);
    int high_idx = (int) Math.ceil(rho_k);
    double alpha = rho_k - low_idx;

    return (1 - alpha) * Math.sqrt(pow[low_idx]) + (alpha) * Math.sqrt(pow[high_idx]);
  }

  /**
   * Precomputes a fixed filterbank to compress FFV features.
   */
  private void constructFilterbank() {
    filterbank = new double[7][512];
    filterbank_mask = new boolean[512];

    // initialize
    for (int i = 0; i < filterbank.length; i++) {
      for (int j = 0; j < filterbank[i].length; j++) {
        filterbank[i][j] = 0;
      }
    }
    for (int i = 0; i < filterbank_mask.length; i++) {
      filterbank_mask[i] = false;
    }

    // Filter 0
    for (int j = 117; j <= 139; j++) {
      filterbank[0][j] = 1. / 23;
    }

    // Filter 1
    filterbank[1][245] = .5 / 6;
    for (int j = 246; j <= 250; j++) {
      filterbank[1][j] = 1. / 6;
    }
    filterbank[1][251] = .5 / 6;

    // Filter 2
    filterbank[2][249] = .5 / 6;
    for (int j = 250; j <= 254; j++) {
      filterbank[2][j] = 1. / 6;
    }
    filterbank[2][255] = .5 / 6;

    // Filter 3
    filterbank[3][254] = .5 / 4;
    for (int j = 255; j <= 257; j++) {
      filterbank[3][j] = 1. / 4;
    }
    filterbank[3][258] = .5 / 4;

    // Filter 4
    filterbank[4][257] = .5 / 6;
    for (int j = 258; j <= 262; j++) {
      filterbank[4][j] = 1. / 6;
    }
    filterbank[4][263] = .5 / 6;

    // Filter 5
    filterbank[5][261] = .5 / 6;
    for (int j = 262; j <= 266; j++) {
      filterbank[5][j] = 1. / 6;
    }
    filterbank[5][267] = .5 / 6;

    // Filter 6
    for (int j = 373; j <= 395; j++) {
      filterbank[6][j] = 1. / 23;
    }

    for (int j = 0; j < filterbank_mask.length; j++) {
      filterbank_mask[j] = false;
      for (double[] fb : filterbank) {
        if (fb[j] != 0) {
          filterbank_mask[j] = true;
          break;
        }
      }
    }
  }

  public static void main(String[] args) {
    File file = new File(args[0]);
    AudioInputStream soundIn = null;
    try {
      soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
    } catch (UnsupportedAudioFileException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    boolean display = false;
    if (args.length > 2) {
      if (args[2].equals("true")) {
        display = true;
      }
    }


    WavReader reader = new WavReader();
    WavData wav;
    try {

      long startTime = System.currentTimeMillis();

//        if (args.length > 1) {
//        wav = reader.read(soundIn, Double.parseDouble(args[1]), Double.parseDouble(args[2]));
//      } else {
      wav = reader.read(soundIn);
//      }
      System.out.println(wav.sampleRate);
      System.out.println(wav.sampleSize);
      System.out.println(wav.getFrameSize());
      System.out.println(wav.getDuration());
      System.out.println(wav.getNumSamples());

      /*
      ./ffv --tfra 0.01 --fs 16000 -tint 0.011 --text 0.015 --tsep 0.01 --fbFileName filterbank.ffv
      /Users/andrew/code/AuToBI/release/test_data/bdc-test.wav /Users/andrew/code/AuToBI/release/test_data/bdc-test
      .wav.txt
       */

      FFVExtractor ffvc = new FFVExtractor(0.01, 0.01, 0.011, 0.015);
      double[][] ffv = ffvc.calculateFFV(wav, 0);

      System.out.println("FFV points:" + ffv.length);
      for (double[] aFfv : ffv) {
        for (double anAFfv : aFfv) {
          System.out.print(anAFfv + " ");
        }
        System.out.println("");
      }

      if (display) {
        SpectrumExtractor.SpectrogramPanel specgram = new SpectrumExtractor.SpectrogramPanel(ffv);

        JFrame frame = new JFrame();
        frame.getContentPane().add(specgram);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(specgram.getWidth(), specgram.getHeight() + 22);
        frame.setVisible(true);
      }
      long endTime = System.currentTimeMillis();

      System.out.println("That took " + (endTime - startTime) / 1000.0 + " seconds");
//
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
