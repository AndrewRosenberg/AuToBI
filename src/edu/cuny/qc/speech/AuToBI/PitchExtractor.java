/*  PitchExtractor.java

    Copyright (c) 2014 Andrew Rosenberg

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

package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import jnt.FFT.RealDoubleFFT_Radix2;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A class to extract Pitch from WavData using Paul Boersma's Sound_to_Pitch algorithm included in Praat.
 * <p/>
 * PitchExtractor can be used as a stand alone, entry point class or as an object within code.
 * <p/>
 * The main function reads a wave file and generates prints a listing of the pitch information to the console.
 */
@Deprecated
public class PitchExtractor extends SampledDataAnalyzer {

  private final static int NUM_VALUE_INTERPOLATE_NEAREST = 0;
  private final static int NUM_VALUE_INTERPOLATE_LINEAR = 1;
  private final static int NUM_VALUE_INTERPOLATE_CUBIC = 2;
  // Higher values than 2 yield a true sinc interpolation. Here are some examples:
  private final static int NUM_VALUE_INTERPOLATE_SINC70 = 70;
  private final static int NUM_VALUE_INTERPOLATE_SINC700 = 700;

  private final static int NUM_PEAK_INTERPOLATE_NONE = 0;
  private final static int NUM_PEAK_INTERPOLATE_PARABOLIC = 1;
  private final static int NUM_PEAK_INTERPOLATE_CUBIC = 2;
  private final static int NUM_PEAK_INTERPOLATE_SINC70 = 3;
  private final static int NUM_PEAK_INTERPOLATE_SINC700 = 4;

  /**
   * Constructs a new PitchExtractor and associate wave data to process.
   *
   * @param wav the wave data.
   */
  public PitchExtractor(WavData wav) {
    super();
    this.wav = wav;
  }

  /**
   * Call Paul Boersma's getPitch with no parameters.
   *
   * @return A list of TimeValuePairs containing pitch information
   * @throws edu.cuny.qc.speech.AuToBI.core.AuToBIException if there are problems
   */
  public Contour soundToPitch() throws AuToBIException {
    // Default min and max pitch values.
    double min_pitch = 50;
    double max_pitch = 500;
    return soundToPitch(0.01, min_pitch, max_pitch);
  }

  /**
   * Call Paul Boersma's getPitch function with default parameters
   *
   * @param time_step The time step to extract values at
   * @param min_pitch The minimum valid pitch
   * @param max_pitch The maximum valid pitch
   * @return a list of pitch points
   * @throws AuToBIException if there are problems
   */
  public Contour soundToPitch(double time_step, double min_pitch, double max_pitch)
      throws AuToBIException {
    return soundToPitchAc(time_step, min_pitch, 3.0, 15, 0.03, 0.45, 0.01, 0.35, 0.14, max_pitch);
  }

  /**
   * Calls getPitch with no paramters, automatically setting the min and max values for more precise estimation.
   * <p/>
   * This is based on the DeLooze and Rauzy (Automatic Detection and Prediction of Topic Changes
   * Through Automatic Detection of Register variations and Pause Duration. De Looze and Rauzy. 2009)
   * It's usefulness was described by The importance of optimal parameter setting for pitch extraction. 2010. Acoustical
   * Society of America. Evanini, Lai and Zechner.
   */
  public Contour soundToPitchTwoPass() throws AuToBIException {
    // initial min and max values.
    Contour c = soundToPitch(0.01, 50, 400);

    // identify relevant percentile values
    Percentile p = new Percentile();
    double[] values = new double[c.contentSize()];
    int i = 0;
    for (Pair<Double, Double> tvp : c) {
      values[i++] = tvp.second;
    }
    double q35 = p.evaluate(values, 35.);
    double q65 = p.evaluate(values, 65.);

    // run getPitch again
    return soundToPitch(0.01, q35 * 0.72 - 10, q65 * 1.9 + 10);
  }

  /**
   * A Java implementation of Paul Boersma's pitch extraction algorithm. Implemented in Praat, and called by To
   * Pitch(ac)...
   *
   * @param time_step            The time step to extract pitch values at
   * @param min_pitch            The minimum valid pitch
   * @param periods_per_window   Number of periods per window
   * @param max_candidates       The number of pitch candidates to consider
   * @param silence_thresh       The silence threshold
   * @param voicing_thresh       The voicing threshold
   * @param octave_cost          The octave cost
   * @param octave_jump_cost     The octave jump cost
   * @param voiced_unvoiced_cost The voiced to unvoiced transition cost
   * @param max_pitch            THe maximum valid pitch
   * @return A list of TimeValuePairs containing pitch information
   * @throws AuToBIException if there's a problem
   */
  public Contour soundToPitchAc(double time_step, double min_pitch, double periods_per_window,
                                int max_candidates, double silence_thresh,
                                double voicing_thresh, double octave_cost, double octave_jump_cost,
                                double voiced_unvoiced_cost, double max_pitch) throws AuToBIException {
    double duration;
    double t0;
    int i, j;
    double dt_window;   /* Window length in seconds. */
    int nsamp_window, halfnsamp_window;   /* Number of samples per window. */
    int nFrames;
    int maximumLag;
    int iframe, nsampFFT;
    double frame[][];
    double ac[];
    double window[];
    double windowR[];
    double globalPeak;
    double interpolation_depth;
    int nsamp_period, halfnsamp_period;   /* Number of samples in longest period. */
    int brent_ixmax, brent_depth;

    if (max_candidates < max_pitch / min_pitch) max_candidates = (int) Math.floor(max_pitch / min_pitch);

    if (time_step <= 0.0) {
      time_step = periods_per_window / min_pitch / 4.0;   /* e.g. 3 periods, 75 Hz: 10 milliseconds. */
    }


    // Exclusively implementing the AC_HANNING case
    brent_depth = NUM_PEAK_INTERPOLATE_SINC70;
    interpolation_depth = 0.5;
    duration = wav.getDuration();
    if (min_pitch < periods_per_window / duration) {
      throw new AuToBIException("For this Sound, the parameter 'minimum pitch' may not be less than " +
          (periods_per_window / duration) + " Hz.");
    }

    /*
    * Determine the number of samples in the longest period.
    * We need this to compute the local mean of the sound (looking one period in both directions),
    * and to compute the local peak of the sound (looking half a period in both directions).
    */
    nsamp_period = (int) Math.floor(1 / wav.getFrameSize() / min_pitch);
    halfnsamp_period = nsamp_period / 2 + 1;

    if (max_pitch > 0.5 / wav.getFrameSize()) max_pitch = 0.5 / wav.getFrameSize();

    /*
    * Determine window length in seconds and in samples.
    */
    dt_window = periods_per_window / min_pitch;
    nsamp_window = (int) Math.floor(dt_window / wav.getFrameSize());
    halfnsamp_window = nsamp_window / 2 - 1;
    if (halfnsamp_window < 2) {
      throw new AuToBIException("Analysis window too short.");
    }
    nsamp_window = halfnsamp_window * 2;

    /*
    * Determine the maximum lag.
    */
    maximumLag = (int) (Math.floor(nsamp_window / periods_per_window) + 2);
    if (maximumLag > nsamp_window) maximumLag = nsamp_window;

    if (wav.getDuration() < dt_window) {
      throw new AuToBIException("Wav data is shorter than pitch analysis window.");
    }

    Pair<Integer, Double> pair = getNFramesAndStartTime(time_step, dt_window);
    nFrames = pair.first;
    t0 = pair.second;

    /*
    * Create the resulting pitch contour.
    */
    Contour pitch = new Contour(t0, time_step, nFrames);

    /*
    * Compute the global absolute peak for determination of silence threshold.
    */
    globalPeak = 0.0;
    for (int channel = 0; channel < wav.numberOfChannels; ++channel) {
      double mean = 0.0;
      for (i = 0; i < wav.getNumSamples(); ++i) {
        mean += wav.getSample(channel, i);
      }
      mean /= wav.getNumSamples();
      for (i = 0; i < wav.getNumSamples(); ++i) {
        double value = Math.abs(wav.getSample(channel, i) - mean);
        if (value > globalPeak) globalPeak = value;
      }
    }
    if (globalPeak == 0.0) {
      return pitch;
    }

    /*
    * Compute the number of samples needed for doing FFT.
    * To avoid edge effects, we have to append zeroes to the window.
    * The maximum lag considered for maxima is maximumLag.
    * The maximum lag used in interpolation is nsamp_window * interpolation_depth.
    */
    nsampFFT = 1;
    while (nsampFFT < nsamp_window * (1 + interpolation_depth)) nsampFFT *= 2;

    /*
    * Create buffers for autocorrelation analysis.
    */
    frame = new double[wav.numberOfChannels][nsampFFT];

    windowR = new double[nsampFFT];
    window = new double[nsamp_window];
    ac = new double[nsampFFT];

    /* Hanning window. */
    for (i = 0; i < nsamp_window; i++) {
      window[i] = 0.5 - 0.5 * Math.cos((i + 1) * 2 * Math.PI / (nsamp_window + 1));
    }

    /*
    * Compute the normalized autocorrelation of the window.
    */
    for (i = 0; i < nsamp_window; i++) {
      windowR[i] = window[i];
    }
    for (i = nsamp_window; i < nsampFFT; ++i) {
      windowR[i] = 0.0;
    }

    // Forward FFT
    RealDoubleFFT_Radix2 window_fft = new RealDoubleFFT_Radix2(nsampFFT);
    window_fft.transform(windowR);

    windowR[0] *= windowR[0];
    for (i = 1; i < nsampFFT; ++i) {
      // calculate the power spectrum
      // absolute value of the fft
      if (i <= nsampFFT / 2) {
        // The real part
        windowR[i] = windowR[i] * windowR[i] + windowR[nsampFFT - i] * windowR[nsampFFT - i];
      } else {
        // The imaginary part
        windowR[i] = 0;
      }
    }
    window_fft.inverse(windowR);


    for (i = 1; i < nsamp_window; i++) {
      windowR[i] = windowR[i] / windowR[0];   /* Normalize. */
    }
    windowR[0] = 1.0;

    brent_ixmax = (int) (nsamp_window * interpolation_depth);
    int[] imax = new int[max_candidates];


    // Start to calculate pitch
    ArrayList<PitchFrame> pitchFrames = new ArrayList<PitchFrame>();
    for (iframe = 0; iframe < nFrames; iframe++) {

      // It's unclear to me what Sound_to_Pitch.c:224 means
      //  Pitch_Frame pitchFrame = & thy frame [iframe];
      // it seems as though there are two 'frame' variables.
      // 'thy frame' is an array of Pitch_Frames with nFrames elements
      // 'frame' is a channels by nsampFFT matrix

      PitchFrame pitchFrame = new PitchFrame();
      double t = indexToX(t0, time_step, iframe);
      double localPeak;
      int leftSample = xToLowIndex(0.0, wav.getFrameSize(), t);
      int rightSample = leftSample + 1;
      int startSample, endSample;

      double localMean[] = new double[wav.numberOfChannels];
      for (int channel = 0; channel < wav.numberOfChannels; ++channel) {
        /*
        * Compute the local mean; look one longest period to both sides.
        */
        startSample = Math.max(0, rightSample - nsamp_period);
        endSample = Math.min(wav.getNumSamples(), leftSample + nsamp_period);

        localMean[channel] = 0.0;
        for (i = startSample; i <= endSample; i++) {
          localMean[channel] += wav.getSample(channel, i);
        }
        localMean[channel] /= 2 * nsamp_period;

        /*
        * Copy a window to a frame and subtract the local mean.
        * We are going to kill the DC component before windowing.
        */
        startSample = Math.max(0, rightSample - halfnsamp_window);
        //endSample = Math.min(wav.getNumSamples()-1, leftSample + halfnsamp_window);
        for (j = 0, i = startSample; j < nsamp_window; j++)
          frame[channel][j] = (wav.getSample(channel, i++) - localMean[channel]) * window[j];
        for (j = nsamp_window + 1; j < nsampFFT; j++)
          frame[channel][j] = 0.0;
      }

      /*
      * Compute the local peak; look half a longest period to both sides.
      */
      localPeak = 0.0;
      if ((startSample = halfnsamp_window + 1 - halfnsamp_period) < 1) {
        startSample = 0;
      }
      if ((endSample = halfnsamp_window + halfnsamp_period) > nsamp_window) endSample = nsamp_window;

      for (int channel = 0; channel < wav.numberOfChannels; ++channel) {
        for (j = startSample; j <= endSample; j++) {
          double value = Math.abs(frame[channel][j]);
          if (value > localPeak) localPeak = value;
        }
      }

      pitchFrame.setIntensity(localPeak > globalPeak ? 1.0 : localPeak / globalPeak);

      /*
      * The FFT of the autocorrelation is the power spectrum.
      */
      for (i = 0; i < nsampFFT; i++) {
        ac[i] = 0.0;
      }

      for (int channel = 0; channel < wav.numberOfChannels; ++channel) {

        // FFT forward
        RealDoubleFFT_Radix2 frame_fft = new RealDoubleFFT_Radix2(nsampFFT);
        frame_fft.transform(frame[channel]);
        ac[0] += frame[channel][0] * frame[channel][0];  /* DC component. */
        for (i = 1; i < nsampFFT - 1; ++i) {
          /* Power spectrum. */
          if (i <= nsampFFT / 2) {
            // The real part
            ac[i] +=
                frame[channel][i] * frame[channel][i] + frame[channel][nsampFFT - i] * frame[channel][nsampFFT - i];
          }
        }
      }
      // FFT backward
      RealDoubleFFT_Radix2 ac_fft = new RealDoubleFFT_Radix2(nsampFFT);
      ac_fft.inverse(ac);

      /*
      * Normalize the autocorrelation to the value with zero lag,
      * and divide it by the normalized autocorrelation of the window.
      */
      NegativeSymmetricList r = new NegativeSymmetricList();
      r.add(1.0);
      for (i = 0; i < brent_ixmax; i++) {
        r.add(ac[i + 1] / (ac[0] * windowR[i + 1]));
      }

      /*
      * Register the first candidate, which is always present: voicelessness.
      */
      pitchFrame.addCandidate();
      pitchFrame.getCandidate(0).frequency = 0.0;  // Voiceless: always present.
      pitchFrame.getCandidate(0).strength = 0.0;

      /*
      * Shortcut: absolute silence is always voiceless.
      * Go to next frame.
      */
      if (localPeak == 0) {
        pitchFrames.add(pitchFrame);
        continue;
      }

      /*
      * Find the strongest maxima of the correlation of this frame,
      * and register them as candidates.
      */
      imax[1] = 0;
      for (i = 1; i < maximumLag && i < brent_ixmax; i++) {
        if (r.get(i) > 0.5 * voicing_thresh && /* Not too unvoiced? */
            r.get(i) > r.get(i - 1) && r.get(i) >= r.get(i + 1))   /* Maximum? */ {
          int place = 0;

          /*
          * Use parabolic interpolation for first estimate of frequency,
          * and sin(x)/x interpolation to compute the strengths of this frequency.
          */
          double dr = 0.5 * (r.get(i + 1) - r.get(i - 1)), d2r = 2 * r.get(i) - r.get(i - 1) - r.get(i + 1);

          double frequencyOfMaximum = 1.0 / wav.getFrameSize() / (i + dr / d2r);
          int offset = -brent_ixmax - 1;
          double strengthOfMaximum = /* method & 1 ? */
              interpolateSinc(r, offset, brent_ixmax - offset, 1.0 / wav.getFrameSize() / frequencyOfMaximum - offset,
                  30)
              /* : r [i] + 0.5 * dr * dr / d2r */;

          /* High values due to short windows are to be reflected around 1. */
          if (strengthOfMaximum > 1.0) strengthOfMaximum = 1.0 / strengthOfMaximum;

          /*
          * Find a place for this maximum.
          */
          if (pitchFrame.getNumCandidates() < max_candidates) { /* Is there still a free place? */
            place = pitchFrame.getNumCandidates();
            pitchFrame.addCandidate();
          } else {
            /* Try the place of the weakest candidate so far. */
            double weakest = 1;
            int iweak;
            for (iweak = 1; iweak < max_candidates; ++iweak) {
              /* High frequencies are to be favoured */
              /* if we want to analyze a perfectly periodic signal correctly. */
              double localStrength = pitchFrame.getCandidate(iweak).strength - octave_cost *
                  Math.log(min_pitch / pitchFrame
                      .getCandidate(iweak)
                      .frequency) / Math.log(2);
              if (localStrength < weakest) {
                weakest = localStrength;
                place = iweak;
              }
            }
            /* If this maximum is weaker than the weakest candidate so far, give it no place. */
            if (strengthOfMaximum - octave_cost * Math.log(min_pitch / frequencyOfMaximum) / Math.log(2) <= weakest) {
              place = -1;
            }
          }
          /* Have we found a place for this candidate? */
          if (place >= 0) {
            pitchFrame.getCandidate(place).frequency = frequencyOfMaximum;
            pitchFrame.getCandidate(place).strength = strengthOfMaximum;
            imax[place] = i;
          }
        }
      }
      /*
      * Second pass: for extra precision, maximize sin(x)/x interpolation ('sinc').
      */
      for (i = 1; i < pitchFrame.getNumCandidates(); i++) {
        if (pitchFrame.getCandidate(i).frequency > 0.0) {
          double xmid;
          double ymid;
          int offset = -brent_ixmax - 1;

          Pair<Double, Double> max_results = improveMaximum(r, offset, brent_ixmax - offset, imax[i] - offset,
              pitchFrame.getCandidate(i).frequency >
                  0.3 / wav.getFrameSize() ? NUM_PEAK_INTERPOLATE_SINC700 :
                  brent_depth);

          ymid = max_results.first;
          xmid = max_results.second;

          xmid += offset;
          pitchFrame.getCandidate(i).frequency = 1.0 / wav.getFrameSize() / xmid;

          if (ymid > 1.0) ymid = 1.0 / ymid;
          pitchFrame.getCandidate(i).strength = ymid;
        }
      }
      pitchFrames.add(pitchFrame);
    }   /* Next frame. */

    // Use path finding with constraints to find the lowest cost path through pitch candidates
    pitch = pathFinder(pitchFrames, silence_thresh, voicing_thresh, octave_cost, octave_jump_cost, voiced_unvoiced_cost,
        max_pitch, max_candidates, time_step, t0);

    return pitch;
  }

  /**
   * Identify the lowest cost path through the PitchFrames given the supplied costs.  The result is a sequence of pitch
   * values corresponding to this lowest cost path through the candidates.
   *
   * @param pitchFrames        The pitch frames with candidate frequencies and strengths
   * @param silenceThresh      a threshold to determine if a frame is silent or not
   * @param voicingThresh      a threshold to determine if a frame contains voicing or not.
   * @param octaveCost         a cost associated with a frequency an octave from the maximum pitch
   * @param octaveJumpCost     a cost associated with being an octave from the previous frame -- pitch halving and
   *                           doubling
   * @param voicedUnvoicedCost the cost for converting from voiced to unvoiced
   * @param maxPitch           The maximum pitch value
   * @param max_candidates     The maximum number of candidates for each frame
   * @param time_step          The timestep for each frame in PitchFrames
   * @param t0                 the time of the initial pitch frame
   * @return The most likely path through the pitch candidates.
   */
  private PitchContour pathFinder(ArrayList<PitchFrame> pitchFrames, double silenceThresh,
                                  double voicingThresh, double octaveCost, double octaveJumpCost,
                                  double voicedUnvoicedCost, double maxPitch, int max_candidates,
                                  double time_step, double t0) {
    PitchContour pitch = new PitchContour(t0, time_step, pitchFrames.size());
    int place;
    double maximum, value;
    double delta[][];
    int psi[][];
    /* Next three lines 20011015 */
    double timeStepCorrection = 0.01 / time_step;
    octaveJumpCost *= timeStepCorrection;
    voicedUnvoicedCost *= timeStepCorrection;

    delta = new double[pitchFrames.size()][max_candidates];
    psi = new int[pitchFrames.size()][max_candidates];

    for (int iframe = 0; iframe < pitchFrames.size(); iframe++) {
      PitchFrame frame = pitchFrames.get(iframe);
      double unvoicedStrength = silenceThresh <= 0 ? 0 :
          2 - frame.getIntensity() / (silenceThresh / (1 + voicingThresh));
      unvoicedStrength = voicingThresh + (unvoicedStrength > 0 ? unvoicedStrength : 0);
      for (int icand = 0; icand < frame.getNumCandidates(); ++icand) {
        PitchCandidate candidate = frame.getCandidate(icand);
        boolean voiceless = candidate.frequency == 0 || candidate.frequency > maxPitch;
        delta[iframe][icand] = voiceless ? unvoicedStrength :
            candidate.strength -
                octaveCost * Math.log(maxPitch / candidate.frequency) / Math.log(2);
      }
    }

    /* Look for the most probable path through the maxima. */
    /* There is a cost for the voiced/unvoiced transition, */
    /* and a cost for a frequency jump. */

    for (int iframe = 1; iframe < pitchFrames.size(); iframe++) {
      PitchFrame prevFrame = pitchFrames.get(iframe - 1);
      PitchFrame curFrame = pitchFrames.get(iframe);
      double prevDelta[] = delta[iframe - 1];
      double curDelta[] = delta[iframe];
      int curPsi[] = psi[iframe];
      for (int icand2 = 0; icand2 < curFrame.getNumCandidates(); icand2++) {
        double f2 = curFrame.getCandidate(icand2).frequency;
        maximum = -1e30;
        place = 0;
        for (int icand1 = 0; icand1 < prevFrame.getNumCandidates(); icand1++) {
          double f1 = prevFrame.getCandidate(icand1).frequency;
          double transitionCost;
          boolean previousVoiceless = f1 <= 0 || f1 >= maxPitch;
          boolean currentVoiceless = f2 <= 0 || f2 >= maxPitch;
          if (currentVoiceless) {
            if (previousVoiceless) {
              transitionCost = 0;   // both voiceless
            } else {
              transitionCost = voicedUnvoicedCost;   // voiced-to-unvoiced transition
            }
          } else {
            if (previousVoiceless) {
              transitionCost = voicedUnvoicedCost;   // unvoiced-to-voiced transition
            } else {
              transitionCost = octaveJumpCost * Math.abs(Math.log(f1 / f2) / Math.log(2));   // both voiced
            }
          }
          value = prevDelta[icand1] - transitionCost + curDelta[icand2];
          if (value > maximum) {
            maximum = value;
            place = icand1;
          }
        }
        curDelta[icand2] = maximum;
        curPsi[icand2] = place;
      }
    }

    /* Find the end of the most probable path. */
    place = 0;
    maximum = delta[pitchFrames.size() - 1][place];
    for (int icand = 1; icand < pitchFrames.get(pitchFrames.size() - 1).getNumCandidates(); icand++) {
      if (delta[pitchFrames.size() - 1][icand] > maximum) {
        place = icand;
        maximum = delta[pitchFrames.size() - 1][place];
      }
    }

    /* Backtracking: follow the path backwards. */
    for (int iframe = pitchFrames.size() - 1; iframe >= 0; iframe--) {
      PitchFrame frame = pitchFrames.get(iframe);
      PitchCandidate help = frame.getCandidate(0);
      frame.setCandidate(0, frame.getCandidate(place));
      frame.setCandidate(place, help);
      place = psi[iframe][place];
    }

    /* Pull formants: devoice frames with frequencies between maxPitch and ceiling2. */
    for (int i = 0; i < pitchFrames.size(); ++i) {
      if (pitchFrames.get(i).getCandidate(0).frequency > 0) { // voiced
        pitch.set(i, pitchFrames.get(i).getCandidate(0).frequency);
      }
      // Set voicing strength whether voiced or unvoiced.
      pitch.setStrength(i, pitchFrames.get(i).getCandidate(0).strength);
    }
    return pitch;
  }

  /**
   * Update the maximum extimation using some interpolation strategy.
   * <p/>
   * In this implementation we only use sinc (sin(x)/x) interpolation.
   *
   * @param r             The List to identify a new maximum for array to find the
   * @param offset        The starting point into r
   * @param nx            The size of the window to analyse to find a new maximum
   * @param ixmid         The index of the current maximum.
   * @param interpolation The interpolation strategy
   * @return a pair containing the new maximum value and its corresponding index in the x domain -- which may not
   * correspond to a valid index.
   */
  private Pair<Double, Double> improveMaximum(NegativeSymmetricList r, int offset, int nx, int ixmid,
                                              int interpolation) {
    if (ixmid <= 0) {
      double ixmid_real = 0;
      return new Pair<Double, Double>(r.get(offset), ixmid_real);
    }

    if (ixmid >= nx) {
      return new Pair<Double, Double>(r.get(nx + offset), (double) nx);
    }
    if (interpolation <= NUM_PEAK_INTERPOLATE_NONE) {
      return new Pair<Double, Double>(r.get(ixmid + offset), (double) ixmid);
    }

    if (interpolation == NUM_PEAK_INTERPOLATE_PARABOLIC) {
      double dy = 0.5 * (r.get(ixmid + 1 + offset) - r.get(ixmid - 1 + offset));
      double d2y = 2 * r.get(ixmid + offset) - r.get(ixmid - 1 + offset) - r.get(ixmid + 1 + offset);
      double ixmid_real = ixmid + dy / d2y;
      return new Pair<Double, Double>(r.get(ixmid) + 0.5 * dy * dy / d2y, ixmid_real);
    }

    // Sinc interpolation
    int depth = interpolation == NUM_PEAK_INTERPOLATE_SINC70 ? 70 : 700;
    Pair<Double, Double> brent_results = minimizeBrent(r, offset, depth, nx, ixmid - 1, ixmid + 1, 1e-10);
    double ixmid_real = brent_results.first;
    return new Pair<Double, Double>(-brent_results.second, ixmid_real);
  }


  /**
   * Use Brent's method to minimize the sinc interpolation function.
   *
   * @param r      The array to find the minimum
   * @param offset the offset into r
   * @param depth  the maximum depth of interpolation
   * @param ixmax  the maximum index in the window used for interpolation
   * @param a      The current lower bound to analyse to find a new minimum
   * @param b      The current upper bound to analyse to find a new minimum
   * @param tol    a numerical tolerance term
   * @return a new minimum paired by the x value and the y value.
   */
  private Pair<Double, Double> minimizeBrent(NegativeSymmetricList r, int offset, int depth, int ixmax, double a,
                                             double b, double tol) {

    final double NUM_goldenSection = 0.6180339887498948482045868343656381177203;
    final double epsilon = 0.00001;
    double x, v, fv, w, fw;
    final double golden = 1 - NUM_goldenSection;
    final double sqrt_epsilon = Math.sqrt(epsilon);
    long iter, itermax = 60;

    /* First step - golden section */

    v = a + golden * (b - a);
    fv = improve_evaluate(v, r, offset, ixmax, depth);
    x = v;
    w = v;
    double fx = fv;
    fw = fv;

    for (iter = 1; iter <= itermax; iter++) {
      double range = b - a;
      double middle_range = (a + b) / 2;
      double tol_act = sqrt_epsilon * Math.abs(x) + tol / 3;
      double new_step; /* Step at this iteration */

      if (Math.abs(x - middle_range) + range / 2 <= 2 * tol_act) {
        return new Pair<Double, Double>(x, fx);
      }

      /* Obtain the golden section step */
      new_step = golden * (x < middle_range ? b - x : a - x);

      /* Decide if the parabolic interpolation can be tried	*/
      if (Math.abs(x - w) >= tol_act) {
        /*
           Interpolation step is calculated as p/q;
           division operation is delayed until last moment.
        */
        double p, q, t;

        t = (x - w) * (fx - fv);
        q = (x - v) * (fx - fw);
        p = (x - v) * q - (x - w) * t;
        q = 2 * (q - t);

        if (q > 0) {
          p = -p;
        } else {
          q = -q;
        }

        /*
           If x+p/q falls in [a,b], not too close to a and b,
           and isn't too large, it is accepted.
           If p/q is too large then the golden section procedure can
           reduce [a,b] range.
        */
        if (Math.abs(p) < Math.abs(new_step * q) &&
            p > q * (a - x + 2 * tol_act) &&
            p < q * (b - x - 2 * tol_act)) {
          new_step = p / q;
        }
      }

      /* Adjust the step to be not less than tolerance. */
      if (Math.abs(new_step) < tol_act) {
        new_step = new_step > 0 ? tol_act : -tol_act;
      }

      /* Obtain the next approximation to min	and reduce the enveloping range */
      double t = x + new_step;    /* Tentative point for the min	*/
      double ft = improve_evaluate(t, r, offset, ixmax, depth);

      /*
             If t is a better approximation, reduce the range so that
             t would fall within it. If x remains the best, reduce the range
             so that x falls within it.
      */

      if (ft <= fx) {
        if (t < x) {
          b = x;
        } else {
          a = x;
        }

        v = w;
        w = x;
        x = t;
        fv = fw;
        fw = fx;
        fx = ft;
      } else {
        if (t < x) {
          a = t;
        } else {
          b = t;
        }

        if (ft <= fw || w == x) {
          v = w;
          w = t;
          fv = fw;
          fw = ft;
        } else if (ft <= fv || v == x || v == w) {
          v = t;
          fv = ft;
        }
      }
    }
    return new Pair<Double, Double>(x, fx);
  }

  /**
   * Improve the maximum value using sinewave interpolation rather than identifying the maximum value in the array r.
   *
   * @param x      the index of the current maximum
   * @param r      the array containing the data
   * @param offset the offset to start analysing the data
   * @param ixmax  the length of the analysis window
   * @param depth  the maximum depth of the interpolation.
   * @return the new maximal value
   */
  private double improve_evaluate(double x, NegativeSymmetricList r, int offset, int ixmax, int depth) {
    double y = interpolateSinc(r, offset, ixmax, x, depth);
    return -y;
  }

  /**
   * Sinewave interpolation.
   *
   * @param r      The data.
   * @param offset An offset into the data array to start from
   * @param nx     The highest index to analyze for the interpolation
   * @param x      The point to interpolate
   * @param depth  The maximum interpolation depth.
   * @return sinewave interpolation value
   */
  private double interpolateSinc(NegativeSymmetricList r, int offset, int nx, double x, int depth) {
    int ix, midleft = (int) Math.floor(x), midright = midleft + 1, left, right;
    double result = 0.0, a, halfsina, aa, daa, cosaa, sinaa, cosdaa, sindaa;

    // Simple interpolation cases.
    if (nx < 1) return Double.NaN;
    if (x > nx) return r.get(nx + offset);
    if (x < 0) return r.get(offset);
    if (x == midleft) return r.get(midleft + offset);
    /* 1 < x < nx && x not integer: interpolate. */
    if (depth > midright - 1) depth = midright - 1;
    if (depth > nx - midleft) depth = nx - midleft;
    if (depth <= NUM_VALUE_INTERPOLATE_NEAREST) return r.get((int) Math.floor(x + 0.5));
    if (depth == NUM_VALUE_INTERPOLATE_LINEAR) {
      return r.get(midleft + offset) + (x - midleft) * (r.get(midright + offset) - r.get(midleft + offset));
    }
    if (depth == NUM_VALUE_INTERPOLATE_CUBIC) {
      double yl = r.get(midleft + offset), yr = r.get(midright + offset);
      double dyl = 0.5 * (yr - r.get(midleft - 1 + offset)), dyr = 0.5 * (r.get(midright + 1 + offset) - yl);
      double fil = x - midleft, fir = midright - x;
      return yl * fir + yr * fil - fil * fir * (0.5 * (dyr - dyl) + (fil - 0.5) * (dyl + dyr - 2 * (yr - yl)));
    }

    left = midright - depth;
    right = midleft + depth;

    a = Math.PI * (x - midleft);
    halfsina = 0.5 * Math.sin(a);
    aa = a / (x - left + 1);
    cosaa = Math.cos(aa);
    sinaa = Math.sin(aa);
    daa = Math.PI / (x - left + 1);
    cosdaa = Math.cos(daa);
    sindaa = Math.sin(daa);
    for (ix = midleft; ix >= left; ix--) {
      double d = halfsina / a * (1.0 + cosaa), help;
      result += r.get(ix + offset) * d;
      a += Math.PI;
      help = cosaa * cosdaa - sinaa * sindaa;
      sinaa = cosaa * sindaa + sinaa * cosdaa;
      cosaa = help;
      halfsina = -halfsina;
    }
    a = Math.PI * (midright - x);
    halfsina = 0.5 * Math.sin(a);
    aa = a / (right - x + 1);
    cosaa = Math.cos(aa);
    sinaa = Math.sin(aa);
    daa = Math.PI / (right - x + 1);
    cosdaa = Math.cos(daa);
    sindaa = Math.sin(daa);
    for (ix = midright; ix <= right; ix++) {
      double d = halfsina / a * (1.0 + cosaa), help;
      result += r.get(ix + offset) * d;
      a += Math.PI;
      help = cosaa * cosdaa - sinaa * sindaa;
      sinaa = cosaa * sindaa + sinaa * cosdaa;
      cosaa = help;
      halfsina = -halfsina;
    }
    return result;
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
      System.out.println(wav.getNumSamples());

      PitchExtractor pitchExtractor = new PitchExtractor(wav);
      PitchContour pitch = (PitchContour) pitchExtractor.soundToPitch();

      System.out.println("pitch points:" + pitch.size());

      for (int i = 0; i < pitch.size(); ++i) {
        System.out
            .println(
                "point[" + i + "]: " + pitch.get(i) + " -- " + pitch.timeFromIndex(i) + ":" + pitch.getStrength(i));
      }
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
