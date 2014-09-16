/*  IntensityExtractor.java

    Copyright (c) 2014 Andrew Rosenberg
    
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

package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.SignalProcessingUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Intensity extractor an intensity contour from a wave file.
 * <p/>
 * This is derived from Praat's description of the get Intensity... behavior in documentation
 * and papers and inspection of its output.
 * <p/>
 * To be respectful of Praat's GPL license, the code itself has been written without reference to
 * Praat's source code.
 */
public class IntensityExtractor extends SampledDataAnalyzer {
  public IntensityExtractor(WavData wav) {
    this.wav = wav;
  }


  /**
   * Generate an intensity contour from a wav file using default parameters.
   * <p/>
   * The default settings are a timestep of 0.01 secs (10ms) and a windowsize of 0.04.
   * Consistent with Praat's description of "four-times oversampling".
   *
   * @return a list of intensity points.
   */
  public Contour soundToIntensity() {
    return getIntensity(0.01, 0.04, true, 0);
  }

  /**
   * Generate an intensity contour from a wav file using default parameters.
   * <p/>
   * Uses the RMS value amplitude of the signal weighted by a bessel-function defined window.
   * <p/>
   *
   * @param time_step              the time_step for the analysis
   * @param win_dur                the Hanning window size to use
   * @param subtract_mean_pressure Wether or not to subtract the mean pressure from the intensity contour.
   * @return a list of intensity points.
   */
  public Contour getIntensity(double time_step, double win_dur, boolean subtract_mean_pressure, int channel) {
    int win_samples = (int) (win_dur / wav.getFrameSize());
    int time_samples = (int) (time_step / wav.getFrameSize());
    double t0 = win_dur / 2.0;  // the first intensity frame is half way between the first window.
    int s0 = xToNearestIndex(0.0, wav.getFrameSize(), t0);

    int num_frames = (int) Math.floor((wav.getNumSamples() - win_samples) / time_samples) + 1;

    if (wav.getDuration() < time_step || wav.getDuration() < win_dur) {
      return new Contour(t0, time_step, 0);
    }

    double[] window = SignalProcessingUtils.constructHanningWindow(win_samples);
    double[] intensity = new double[num_frames];

    int mid_sample = s0;
    for (int i = 0; i < num_frames; i++, mid_sample += time_samples) {
      int bottom_sample = mid_sample - win_samples / 2;
      int top_sample = mid_sample + win_samples / 2;
      if (top_sample >= wav.getNumSamples()) break;

      double ssq = 0.0;
      double win = 0.0;
      double mean = 0;
      if (subtract_mean_pressure) {
        for (int idx = bottom_sample; idx <= top_sample; idx++) {
          mean += wav.getSample(channel, idx);
        }
        mean /= (top_sample - bottom_sample + 1);
      }
      for (int idx = bottom_sample; idx < top_sample; idx++) {
        double energy = wav.getSample(channel, idx) - mean;
        ssq += energy * energy * window[idx - bottom_sample];
        win += window[idx - bottom_sample];
      }
      double ms = ssq / win; // Rather than calculating RMS, calculate MS and square the reference db value
      final double refsq = 4e-10;  // Auditory threshold pressure = 2e-5 squared.
      intensity[i] = Math.max(-300, 10 * Math.log10(ms / refsq));
    }
    return new Contour(t0, time_step, intensity);
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

    WavReader reader = new WavReader();
    WavData wav;
    try {

      if (args.length > 1) {
        wav = reader.read(soundIn, Double.parseDouble(args[1]), Double.parseDouble(args[2]));
      } else {
        wav = reader.read(soundIn);
      }
      System.out.println(wav.sampleRate);
      System.out.println(wav.sampleSize);
      System.out.println(wav.getFrameSize());
      System.out.println(wav.getDuration());

      IntensityExtractor intensityFactory = new IntensityExtractor(wav);
      Contour intensity = intensityFactory.soundToIntensity();

      System.out.println("intensity points:" + intensity.size());

      for (int i = 0; i < intensity.size(); ++i) {
        System.out
            .println(
                "intensity point[" + i + "]: " + intensity.get(i) + " -- " + intensity.timeFromIndex(i));
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }

  }

}
