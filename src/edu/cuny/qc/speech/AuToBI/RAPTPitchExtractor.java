/* RAPTPitchExtractor.java
 *
 * Copyright 2014 Andrew Rosenberg
 *
 * Implementation of the RAPT Pitch Tracking algorithm.
 * As described in A Robust Algorithm for Pitch Tracking by David Talkin 1995
 *
 * This is a translation of the c implementation of RAPT distributed as part of the SPTK
 * http://sp-tk.sourceforge.net/
 *
 *     This file is part of the AuToBI prosodic analysis package.

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
 *
 *
 * SPTK is covered by a BSD-style licenxe included in sptk-license.txt.  The SPTK copyright notice is included here
 *
 /******  BEGIN SPTK COPYRIGHT NOTICE ******
 / * This software has been licensed to the Centre of Speech Technology, KTH
 / * by Microsoft Corp. with the terms in the accompanying file BSD.txt,
 / * which is a BSD style license.
 / *
 / *    "Copyright (c) 1990-1996 Entropic Research Laboratory, Inc.
 / *                   All rights reserved"
 / *
 / * Written by:  Derek Lin
 / * Checked by:
 / * Revised by:  David Talkin
 / *
 / * Brief description:  Estimates F0 using normalized cross correlation and
 / *   dynamic programming.
 / *
 / *
 /* ----------------------------------------------------------------- *
 /*             The Speech Signal Processing Toolkit (SPTK)           *
 /*             developed by SPTK Working Group                       */
/*             http://sp-tk.sourceforge.net/                         */
/* ----------------------------------------------------------------- */
/*                                                                   */
/*  Copyright (c) 1984-2007  Tokyo Institute of Technology           */
/*                           Interdisciplinary Graduate School of    */
/*                           Science and Engineering                 */
/*                                                                   */
/*                1996-2013  Nagoya Institute of Technology          */
/*                           Department of Computer Science          */
/*                                                                   */
/* All rights reserved.                                              */
/*                                                                   */
/* Redistribution and use in source and binary forms, with or        */
/* without modification, are permitted provided that the following   */
/* conditions are met:                                               */
/*                                                                   */
/* - Redistributions of source code must retain the above copyright  */
/*   notice, this list of conditions and the following disclaimer.   */
/* - Redistributions in binary form must reproduce the above         */
/*   copyright notice, this list of conditions and the following     */
/*   disclaimer in the documentation and/or other materials provided */
/*   with the distribution.                                          */
/* - Neither the name of the SPTK working group nor the names of its */
/*   contributors may be used to endorse or promote products derived */
/*   from this software without specific prior written permission.   */
/*                                                                   */
/* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND            */
/* CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,       */
/* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF          */
/* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE          */
/* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS */
/* BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,          */
/* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED   */
/* TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,     */
/* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON */
/* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,   */
/* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY    */
/* OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE           */
/* POSSIBILITY OF SUCH DAMAGE.                                       */
/* ----------------------------------------------------------------- */
/******  END SPTK COPYRIGHT NOTICE ******/


package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.PitchContour;
import edu.cuny.qc.speech.AuToBI.core.WavData;
import edu.cuny.qc.speech.AuToBI.io.WavReader;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class RAPTPitchExtractor {
  public Params par;

  public RAPTPitchExtractor() {
    par = new Params();
    par.cand_thresh = 0.3f;
    par.lag_weight = 0.3f;
    par.freq_weight = 0.02f;
    par.trans_cost = 0.005f;
    par.trans_amp = 0.5f;
    par.trans_spec = 0.5f;
    par.voice_bias = 0.0f;
    par.double_cost = 0.35f;


    par.wind_dur = 0.0075f;
    par.n_cands = 20;
    par.mean_f0 = 200;     /* unused */
    par.mean_f0_weight = 0.0f;  /* unused */
    par.conditioning = 0;    /*unused */

    // Should be parameterized
    par.minF0 = 50;  // Default voice range.
    par.maxF0 = 500;
    par.frame_step = 0.01f;
  }

  public RAPTPitchExtractor(float minF0, float maxF0) {
    this();
    par.minF0 = minF0;
    par.maxF0 = maxF0;
  }

  class Params {
    public float trans_cost;
    public float trans_amp;
    public float trans_spec;
    public float voice_bias;
    public float double_cost;
    public float mean_f0;
    public float mean_f0_weight;

    public float maxF0;
    public float minF0;
    public float frame_step;
    public float wind_dur;

    public float lag_weight;
    public float freq_weight;

    public int n_cands;
    public int conditioning;

    public float cand_thresh;
  }

  class DPFrame {
    public DPRecord dp;
    public Cross cp;
    public float rms;

    public DPFrame next;
    public DPFrame prev;

    public DPFrame(int nlags, int ncands) {
      this.dp = new DPRecord();
      this.cp = new Cross();
      this.dp.ncands = 0;
      this.dp.locs = new int[ncands];
      this.dp.prept = new int[ncands];
      this.dp.pvals = new float[ncands];
      this.dp.mpvals = new float[ncands];
      this.dp.dpvals = new float[ncands];

      this.cp.correl = new float[nlags];
      this.next = null;
      this.prev = null;
    }
  }

  class DPRecord {
    public int ncands;
    public int[] locs;
    public float[] pvals;
    public float[] mpvals;
    public int[] prept;
    public float[] dpvals;
  }

  class Cross {
    public float[] correl;
    public int maxloc;
    public float maxval;
    public float rms;
    public int firstlag;
  }

  class WindowStat {
    float err = 0;
    float rms = 0;
    float[] rho = null;
  }

  class Stat {
    public float[] stat;
    public float[] rms;
    public float[] rms_ratio;

    public Stat(int nframes) {
      this.stat = new float[nframes];
      this.rms = new float[nframes];
      this.rms_ratio = new float[nframes];
    }
  }

  /*
 * READ_SIZE: length of input data frame in sec to read
 * DP_CIRCULAR: determines the initial size of DP circular buffer in sec
 * DP_HIST: stored frame history in second before checking for common path
 *      DP_CIRCULAR > READ_SIZE, DP_CIRCULAR at least 2 times of DP_HIST
 * DP_LIMIT: in case no convergence is found, DP frames of DP_LIMIT secs
 *      are kept before output is forced by simply picking the lowest cost
 *      path
 */
  public static float READ_SIZE = 0.2f;
  public static float DP_CIRCULAR = 1.5f;
  public static float DP_HIST = 0.5f;
  public static float DP_LIMIT = 1.0f;

  public static float FLT_MAX = Float.MAX_VALUE;

  int wReuse = 0;  /* number of windows seen before resued */
  WindowStat[] windstat = null;

  // Member variables for the RAPT algorithm
  protected DPFrame headF = null;   // Current frame in the circular buffer
  protected DPFrame tailF = null;   // Frame where tracks start
  protected DPFrame cmpthF = null;  // the starting frame of converged paths to backtrack

  int[] pcands = null;

  int size_circ_buf;
  int size_frame_hist;
  int size_frame_out;
  int num_active_frames;
  int output_buf_size;

  // DP Parameters
  float tcost, tfact_a, tfact_s, vbias, fdouble, wind_dur, ln2, freqwt, lagwt;
  int step, size, nlags, start, stop, ncomp;
  int[] locs = null;
  short maxpeaks;

  float[] f0p = null, vuvp = null, rms_speech = null, acpkp = null, peaks = null;

  boolean first = true;

  // Signal processing windows that can be stored.
  public float[] hamwind = null;
  public float[] hanwind = null;
  public float[] coswind = null;

  public float[] dwind;
  public int nwind;

  public static final int BIGSORD = 100; // According to sptk

  public int pad;

  /**
   * Round to the nearest integer
   *
   * @param f float
   * @return rounded to int
   */
  int round(double f) {
    return f >= 0.0 ? (int) (f + 0.5) : (int) (f - 0.5);
  }

  int initDP(double freq, Params par, long[] buffsize, long[] sdstep) {

    int nframes;
    int stat_wsize, agap, ind, downpatch;

    tcost = par.trans_cost;
    tfact_a = par.trans_amp;
    tfact_s = par.trans_spec;
    vbias = par.voice_bias;
    fdouble = par.double_cost; // doubling cost
    float frame_int = par.frame_step;

    step = round(frame_int * freq);
    size = round(par.wind_dur * freq);
    frame_int = (float) ((float) step / freq);
    wind_dur = (float) ((float) size / freq);

    start = round(freq / par.maxF0);
    stop = round(freq / par.minF0);

    nlags = stop - start + 1;
    ncomp = size + stop + 1;  // number of samples necessary for xcorr

    maxpeaks = (short) (2 + (nlags / 2));
    ln2 = (float) Math.log(2);

    size_frame_hist = (int) (DP_HIST / frame_int);
    size_frame_out = (int) (DP_LIMIT / frame_int);

    lagwt = par.lag_weight / stop; // lag dependent weighting factor to emphasize early peaks

    freqwt = par.freq_weight / frame_int; // penalty for a freq. skip in F0 per frame
    int i = (int) (READ_SIZE * freq);

    if (ncomp >= step) {
      nframes = ((i - ncomp) / step) + 1;
    } else {
      nframes = i / step;
    }

    downpatch = (((int) (freq * 0.005)) + 1) / 2;
    stat_wsize = (int) (STAT_WSIZE * freq);
    agap = (int) (STAT_AINT * freq);
    ind = (agap - stat_wsize) / 2;
    i = stat_wsize + ind;
    pad = downpatch + ((i > ncomp) ? i : ncomp);
    buffsize[0] = nframes * step + pad;
    sdstep[0] = nframes * step;

    // Allocate a circular buffer of DPFrames
    size_circ_buf = (int) (DP_CIRCULAR / frame_int);

    // Construct the circular frame structure
    tailF = new DPFrame(nlags, par.n_cands);
    headF = tailF;
    for (int j = 0; j < size_circ_buf; j++) {
      headF.next = new DPFrame(nlags, par.n_cands);
      headF.next.prev = headF;
      headF = headF.next;
    }
    headF.next = tailF;
    tailF.prev = headF;
    headF = tailF;

    pcands = new int[par.n_cands];

    output_buf_size = size_circ_buf;

    rms_speech = new float[output_buf_size];
    f0p = new float[output_buf_size];
    vuvp = new float[output_buf_size];
    acpkp = new float[output_buf_size];

    peaks = new float[maxpeaks];
    locs = new int[maxpeaks];

    if (agap / step > 0) {
      windstat = new WindowStat[agap / step];
    }

//    System.err.println("done with initialization:");
//    System.err.format(" size_cir_buffer:%d  xcorr frame size:%d start lag:%d nlags:%d\n",
//        size_circ_buf, size, start, nlags);


    num_active_frames = 0;
    first = true;

    return 0;
  }

  public int getNFrames(long buffsize, int pad, int step) {
    if (buffsize < pad) {
      return 0;
    } else {
      return (int) ((buffsize - pad) / step);
    }
  }

  public int dpF0(float[] fdata, int buff_size, int sdstep, double freq, Params par,
                  float[] f0p, float[] vuvp, float[] rms_speech, float[] ackpkp, int[] vecsize,
                  boolean last_time) throws
      AuToBIException {
    float[] sta, rms_ratio, dsdata;
    float ttemp, ftemp, ft1, ferr, err, errmin;
    int j, k, loc1, loc2;
    int nframes, ncandp, minloc, decimate;

    float[] engref = new float[1];
    float[] maxval = new float[1];
    int[] samsds = new int[1];
    int[] maxloc = new int[1];
    int[] ncand = new int[1];

    nframes = getNFrames(buff_size, pad, step);

    // Downsample the signal for coarse peak estimates
    decimate = (int) (freq / 2000.0);
    if (decimate <= 1) {
      dsdata = fdata;
    } else {
      samsds[0] = ((nframes - 1) * step + ncomp) / decimate;
      if (samsds[0] < 1) return 1;

      dsdata = downsample(fdata, buff_size, sdstep, freq, samsds, decimate, first, last_time);

      if (dsdata == null) {
        AuToBIUtils.error("Problem with downsampling data");
        return 1;
      }
    }

    // Get a function of the stationarity of the speech signal
    Stat stat = getStationarity(fdata, 0, freq, buff_size, nframes, step, first);
    sta = stat.stat;
    rms_ratio = stat.rms_ratio;

    /***********************************************************************/
        /* MAIN FUNDAMENTAL FREQUENCY ESTIMATION LOOP */
    /***********************************************************************/

    // advance to the next frame
    if (!first && nframes > 0) {
      headF = headF.next;
    }

    for (int i = 0; i < nframes; i++) {
      // head has been advanced to the end of the buffer
      if (headF == tailF.prev) {
        // keep head and tail, but insert up to size_circ_buf blank frames between head and tail.

        // include this buffer growth
        DPFrame frm = new DPFrame(nlags, par.n_cands);
        headF.next = frm;
        frm.prev = headF;
        for (k = 1; k < size_circ_buf; k++) {
          frm.next = new DPFrame(nlags, par.n_cands);
          frm.next.prev = frm;
          frm = frm.next;
        }
        frm.next = tailF;
        tailF.prev = frm;
      }
      headF.rms = stat.rms[i];
      getFastCands(fdata, dsdata, i, step, size, decimate, start, nlags, engref, maxloc, maxval, headF.cp, peaks, locs,
          ncand, par);

      // move peak and location arrays into DP structure.
      int idx = 0;
      for (j = 0; j < ncand[0]; j++) {// possibly this?
        headF.dp.pvals[idx] = peaks[idx];
        headF.dp.locs[idx] = locs[idx];
        idx++;
      }
      headF.dp.locs[idx] = -1;
      headF.dp.pvals[idx] = maxval[0];
      headF.dp.mpvals[ncand[0]] = vbias + maxval[0];

      /* Apply a lag-dependent weight to the peaks to encourage the selection
       of the first major peak.  Translate the modified peak values into
       costs (high peak ==> low cost). */
      for (j = 0; j < ncand[0]; j++) {
        ftemp = 1.0f - ((float) locs[j] * lagwt);
        headF.dp.mpvals[j] = 1.0f - (peaks[j] * ftemp);
      }
      ncand[0]++;			/* include the unvoiced candidate */
      headF.dp.ncands = ncand[0];

//      System.err.format(" - initial DP values -\n");
//      System.err.format(" - ncands:%d -\n", headF.dp.ncands);
//      for (j = 0; j < ncand[0]; j++) {
//        System.err.format(" - %d - mpvals: %.4f pvals: %.4f locs: %d\n",
//            j, headF.dp.mpvals[j], headF.dp.pvals[j], headF.dp.locs[j]);
//      }

      ncandp = headF.prev.dp.ncands;
//      System.err.format("Debugging DP score.  ncandp: %d\n", ncandp);
      for (k = 0; k < ncand[0]; k++) {
//        System.err.format("  -- %d \n", k);
        minloc = 0;
        errmin = FLT_MAX;
        if ((loc2 = headF.dp.locs[k]) > 0) {  // current is voiced
//          System.err.format("  -- current is voiced \n");
          for (j = 0; j < ncandp; j++) {
            loc1 = headF.prev.dp.locs[j];
            if (loc1 > 0) {  // prev was voiced
              ftemp = (float) Math.log(((double) loc2) / loc1);
              ttemp = Math.abs(ftemp);
              ft1 = fdouble + Math.abs(ftemp + ln2);
              if (ttemp > ft1) {
                ttemp = ft1;
              }
              ft1 = fdouble + Math.abs(ftemp - ln2);
              if (ttemp > ft1) {
                ttemp = ft1;
              }
              ferr = ttemp * freqwt;
            } else {  // prev was unvoiced
              ferr = tcost + (tfact_s * sta[i]) + (tfact_a / rms_ratio[i]);
            }
            err = ferr + headF.prev.dp.dpvals[j];   // accumulate previous error.
            if (err < errmin) {
              errmin = err;
              minloc = j;
            }
          }
        } else {  // current is unvoiced.
          for (j = 0; j < ncandp; j++) {  // for each previous candidate
            // get voicing cost
            if (headF.prev.dp.locs[j] > 0) { // prev was voiced
              ferr = tcost + (tfact_s * sta[i]) + (tfact_a * rms_ratio[i]);
            } else {
              ferr = 0.0f;
            }
            err = ferr + headF.prev.dp.dpvals[j];
            if (err < errmin) {
              errmin = err;
              minloc = j;
            }
          }
        }
        // have now found the best path from this candidate to previous frame
        if (first && i == 0) {
          headF.dp.dpvals[k] = headF.dp.mpvals[k];
          headF.dp.prept[k] = 0;
        } else {
          headF.dp.dpvals[k] = errmin + headF.dp.mpvals[k];
          headF.dp.prept[k] = minloc;
        }
      }

      if (i < nframes - 1) {
        headF = headF.next;
      }
//      System.err.format("%d engref:%10.0f max:%7.5f loc:%4d\n", i, engref[0], maxval[0], maxloc[0]);
    } // end for i.

    // done propagating dp structure for the current set of sampled data.

    vecsize[0] = 0;
    num_active_frames += nframes;

    if (num_active_frames >= size_frame_hist || last_time) {
      DPFrame frm;
      int num_paths;
      int best_cand;
      boolean checkpath_done = true;
      float patherrmin;

      patherrmin = FLT_MAX;
      best_cand = 0;
      num_paths = headF.dp.ncands;

      // get the best candidate for the final frame and initialize back pointers
      frm = headF;
      for (k = 0; k < num_paths; k++) {
        if (patherrmin > headF.dp.dpvals[k]) {
          patherrmin = headF.dp.dpvals[k];
          best_cand = k;
        }
        pcands[k] = frm.dp.prept[k];
      }


      if (last_time) { // input data was exhausted force final outputs
        cmpthF = headF;
      } else {
        /* Starting from the most recent frame, trace back each candidate's
   best path until reaching a common candidate at some past frame. */
        while (true) {
          frm = frm.prev;
          checkpath_done = true;
          for (k = 1; k < num_paths; k++) {  // check for convergence.
            if (pcands[0] != pcands[k]) {
              checkpath_done = false;
            }
          }
          if (!checkpath_done) { // prepare for checking at previous frame
            for (k = 0; k < num_paths; k++) {
              pcands[k] = frm.dp.prept[pcands[k]];
            }
          } else { // all paths have converged
            cmpthF = frm;
            best_cand = pcands[0];
            break;
          }
          if (frm == tailF) { // used all available data
            if (num_active_frames < size_frame_out) {   // delay some more?
              checkpath_done = false;
              cmpthF = null;
            } else { // force best guess output
              checkpath_done = true;
              cmpthF = headF;
            }
            break;
          }
        }
      }

      // Backtracking from cmpthF (best cand) to tailf
      int i = 0;
      frm = cmpthF;  // start where convergence was found
      while (frm != tailF.prev && checkpath_done) {
        if (i == output_buf_size) {
          output_buf_size *= 2;
          rms_speech = Arrays.copyOf(rms_speech, output_buf_size);
          f0p = Arrays.copyOf(f0p, output_buf_size);
          vuvp = Arrays.copyOf(vuvp, output_buf_size);
          acpkp = Arrays.copyOf(acpkp, output_buf_size);
        }
        rms_speech[i] = frm.rms;
        acpkp[i] = frm.dp.pvals[best_cand];
        loc1 = frm.dp.locs[best_cand];
        vuvp[i] = 1.0f;  // why always 1.0?
        best_cand = frm.dp.prept[best_cand];
        ftemp = loc1;

        if (loc1 > 0) { // was f0 actually estimated for this frame
          if (loc1 > start && loc1 < stop) {
            float cormax, cprev, cnext, den;
            j = loc1 - start;
            cormax = frm.cp.correl[j];
            cprev = frm.cp.correl[j + 1];  // AR these seem inverted. but not worth changing for now.
            cnext = frm.cp.correl[j - 1];

            den = (float) (2.0 * (cprev + cnext - (2 * cormax)));
            // parabolic interpolation (to smooth out errors?)
            if (Math.abs(den) > 0.000001) {
              ftemp += 2.0f - ((((5.0f * cprev) + (3.0f * cnext) - (8.0f * cormax)) / den));
            }
          }
          f0p[i] = (float) (freq / ftemp);
        } else {
          f0p[i] = 0;
          vuvp[i] = 0;
        }
        frm = frm.prev;
        i++;
      }
      if (checkpath_done) {
        vecsize[0] = i;
        tailF = cmpthF.next;
        num_active_frames -= vecsize[0];
      }
    }

    if (first) {
      first = false;
    }
    return 0;
  }

  public void getFastCands(float[] fdata, float[] fdsdata, int ind, int step, int size, int dec, int start, int nlags,
                           float[] engref, int[] maxloc, float[] maxval, Cross cp, float[] peaks, int[] locs,
                           int[] ncand, Params par) {


    int decind, decstart, decnlags, decsize, i, j;
    float lag_wt;

    float[] xp = new float[1];
    float[] yp = new float[1];

    int lp, pe;
    float[] corp;

    lag_wt = par.lag_weight / nlags;
    decnlags = 1 + (nlags / dec);
    if ((decstart = start / dec) < 1) decstart = 1;

    decind = (ind * step) / dec;
    decsize = 1 + (size / dec);
    corp = cp.correl;


    crossf(fdsdata, decind, decsize, decstart, decnlags, engref, maxloc, maxval, corp);

    // Confirm corp is consistent.
//    for (int idx = 0; idx < corp.length; idx++) {
//      System.err.format(" i:%d corp[i]:%.5f\n", idx, corp[idx]);
//    }

    cp.maxloc = maxloc[0];	/* location of maximum in correlation */
    cp.maxval = maxval[0];	/* max. correlation value (found at maxloc) */
    cp.rms = (float) Math.sqrt(engref[0] / size); /* rms in reference window */
    cp.firstlag = decstart;

    get_cand(cp, peaks, locs, decnlags, ncand, par.cand_thresh); /* return high peaks in xcorr */

    /* Interpolate to estimate peak locations and values at high sample rate. */
    for (i = ncand[0], lp = 0, pe = 0; i-- > 0; pe++, lp++) {
      j = locs[lp] - decstart - 1;
      peak(corp, j, xp, yp);
      locs[lp] = (locs[lp] * dec) + (int) (0.5 + (xp[0] * dec)); /* refined lag */
      peaks[pe] = yp[0] * (1.0f - (lag_wt * locs[lp])); /* refined amplitude */
    }

    if (ncand[0] >= par.n_cands) {	/* need to prune candidates? */
      int loc, locm, lt;
      float smaxval;
      int pem;
      int outer, inner, lim;
      for (outer = 0, lim = par.n_cands - 1; outer < lim; outer++)
        for (inner = ncand[0] - 1 - outer,
                 pe = ncand[0] - 1, pem = pe - 1,
                 loc = ncand[0] - 1, locm = loc - 1;
             inner-- > 0;
             pe--, pem--, loc--, locm--)
          if ((smaxval = peaks[pe]) > peaks[pem]) {
            peaks[pe] = peaks[pem];
            peaks[pem] = smaxval;
            lt = locs[loc];
            locs[loc] = locs[locm];
            locs[locm] = lt;
          }
      ncand[0] = par.n_cands - 1;  /* leave room for the unvoiced hypothesis */
    }
    crossfi(fdata, (ind * step), size, start, nlags, 7, engref, maxloc,
        maxval, corp, locs, ncand[0]);

    // Confirm corp is consistent.
//    for (int idx = 0; idx < corp.length; idx++) {
//      System.err.format(" post crossfi i:%d corp[i]:%.5f\n", idx, corp[idx]);
//    }

    cp.maxloc = maxloc[0];	/* location of maximum in correlation */
    cp.maxval = maxval[0];	/* max. correlation value (found at maxloc) */
    cp.rms = (float) Math.sqrt(engref[0] / size); /* rms in reference window */
    cp.firstlag = start;
    get_cand(cp, peaks, locs, nlags, ncand, par.cand_thresh); /* return high peaks in xcorr */
    if (ncand[0] >= par.n_cands) {	/* need to prune candidates again? */
      int loc, locm, lt;
      float smaxval;
      int pem;
      int outer, inner, lim;
      for (outer = 0, lim = par.n_cands - 1; outer < lim; outer++) {
        for (inner = ncand[0] - 1 - outer,
                 pe = ncand[0] - 1, pem = pe - 1,
                 loc = ncand[0] - 1, locm = loc - 1;
             inner-- > 0;
             pe--, pem--, loc--, locm--)
          if ((smaxval = peaks[pe]) > peaks[pem]) {
            peaks[pe] = peaks[pem];
            peaks[pem] = smaxval;
            lt = locs[loc];
            locs[loc] = locs[locm];
            locs[locm] = lt;
          }
      }
      ncand[0] = par.n_cands - 1;  /* leave room for the unvoiced hypothesis */
    }

  }

  float[] dbdata = null;
  int dbsize = 0;

  /**
   * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
   * /* Return a sequence based on the normalized crosscorrelation of the
   * signal in data.  This is similar to crossf(), but is designed to
   * compute only small patches of the correlation sequence.  The length of
   * each patch is determined by nlags; the number of patches by nlocs, and
   * the locations of the patches is specified by the array locs.  Regions
   * of the CCF that are not computed are set to 0.
   * <p/>
   * data is the input speech array
   * size is the number of samples in each correlation
   * start0 is the first (virtual) lag to compute (governed by highest F0)
   * nlags0 is the number of lags (virtual+actual) in the correlation sequence
   * nlags is the number of cross correlations to compute at each location
   * engref is the energy computed at lag=0 (i.e. energy in ref. window)
   * maxloc is the lag at which the maximum in the correlation was found
   * maxval is the value of the maximum in the CCF over the requested lag interval
   * correl is the array of nlags cross-correlation coefficients (-1.0 to 1.0)
   * locs is an array of indices pointing to the center of a patches where the
   * cross correlation is to be computed.
   * nlocs is the number of correlation patches to compute.
   */

  void crossfi(float[] data, int doff, int size, int start0, int nlags0, int nlags, float[] engref, int[] maxloc,
               float[] maxval, float[] correl, int[] locs, int nlocs) {
    float sum, st;
    int j;
    int dbi, di, ci;
    float t, engr, amax;
    double engc;
    int i, iloc, start, total;

    /* Compute mean in reference window and subtract this from the
     entire sequence. */
    if ((total = size + start0 + nlags0) > dbsize) {
      dbdata = new float[total];
      dbsize = total;
    }
    for (engr = 0.0f, j = size, di = doff; j-- > 0; )
      engr += data[di++];
    engr /= size;

    for (j = size + nlags0 + start0, dbi = 0, di = doff; j-- > 0; ) {
      dbdata[dbi++] = data[di++] - engr;
    }

  /* Zero the correlation output array to avoid confusing the peak
     picker (since all lags will not be computed). */
    for (ci = 0, i = nlags0; i-- > 0; )
      correl[ci++] = 0.f;

  /* compute energy in reference window */
    for (j = size, dbi = 0, sum = 0.0f; j-- > 0; ) {
      st = dbdata[dbi++];
      sum += st * st;
    }

    engref[0] = engr = sum;
    amax = 0.0f;
    iloc = -1;
    if (engr > 0.0) {
      int li = 0;
      for (; nlocs > 0; nlocs--, li++) {
        start = locs[li] - (nlags >> 1);
        if (start < start0) {
          start = start0;
        }
        ci = start - start0;
      /* compute energy at first requested lag */
        for (j = size, dbi = start, sum = 0.0f; j-- > 0; ) {
          st = dbdata[dbi++];
          sum += st * st;
        }
        engc = sum;

      /* COMPUTE CORRELATIONS AT ALL REQUESTED LAGS */
        for (i = 0; i < nlags; i++) {
          int dds, ds;                                      // dbdata +
          for (j = size, sum = 0.0f, dbi = 0, dds = ds = i + start; j-- > 0; )
            sum += dbdata[dbi++] * dbdata[ds++];
          if (engc < 1.0) {
            engc = 1.0;		/* in case of roundoff error */
          }
          correl[ci++] = t = (float) (sum / Math.sqrt(10000.0 + (engc * engr)));
          engc -= (double) (dbdata[dds] * dbdata[dds]);
          engc += (double) (dbdata[ds] * dbdata[ds]);
          if (t > amax) {
            amax = t;
            iloc = i + start;
          }
        }
      }
      maxloc[0] = iloc;
      maxval[0] = amax;
    } else {
      maxloc[0] = 0;
      maxval[0] = 0.0f;
    }
  }


  /**
   * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
   * Return a sequence based on the normalized crosscorrelation of the signal
   * in data.
   * <p/>
   * data is the input speech array
   * size is the number of samples in each correlation
   * start is the first lag to compute (governed by the highest expected F0)
   * nlags is the number of cross correlations to compute (set by lowest F0)
   * engref is the energy computed at lag=0 (i.e. energy in ref. window)
   * maxloc is the lag at which the maximum in the correlation was found
   * maxval is the value of the maximum in the CCF over the requested lag interval
   * correl is the array of nlags cross-correlation coefficients (-1.0 to 1.0)
   */
  public void crossf(float[] data, int doff, int size, int start, int nlags, float[] engref,
                     int[] maxloc, float[] maxval, float[] correl) {

    int j;
    int ds, dds;
    int di, dbi, ci;
    float t, engr, amax, sum, st;
    double engc;
    int i, iloc, total;

  /* Compute mean in reference window and subtract this from the
     entire sequence.  This doesn't do too much damage to the data
     sequenced for the purposes of F0 estimation and removes the need for
     more principled (and costly) low-cut filtering. */
    if ((total = size + start + nlags) > dbsize) {
      // Reallocate if we need to.
      dbdata = new float[total];
      dbsize = total;
    }
    for (engr = 0.0f, j = size, di = 0; j-- > 0; ) {
      engr += data[doff + di++];
    }
    engr /= size;
    for (j = size + nlags + start, dbi = 0, di = 0; j-- > 0; ) {
      dbdata[dbi++] = data[doff + di++] - engr;
    }

  /* Compute energy in reference window. */
    for (j = size, dbi = 0, sum = 0.0f; j-- > 0; ) {
      st = dbdata[dbi++];
      sum += st * st;
    }

    engref[0] = engr = sum;
    if (engr > 0.0) {    /* If there is any signal energy to work with... */
    /* Compute energy at the first requested lag. */
      for (j = size, dbi = start, sum = 0.0f; j-- > 0; ) {
        st = dbdata[dbi++];
        sum += st * st;
      }
      engc = sum;

    /* COMPUTE CORRELATIONS AT ALL OTHER REQUESTED LAGS. */
      for (i = 0, ci = 0, amax = 0.0f, iloc = -1; i < nlags; i++) {
        for (j = size, sum = 0.0f, dbi = 0, dds = ds = i + start; j-- > 0; )
          sum += dbdata[dbi++] * dbdata[ds++];
        correl[ci++] = t = (float) (sum / Math.sqrt(engc * engr)); /* output norm. CC */
        engc -= (double) (dbdata[dds] * dbdata[dds]); /* adjust norm. energy for next lag */
        if ((engc += (double) (dbdata[ds] * dbdata[ds])) < 1.0) {
          engc = 1.0;		/* (hack: in case of roundoff error) */
        }
        if (t > amax) {		/* Find abs. max. as we go. */
          amax = t;
          iloc = i + start;
        }
      }
      maxloc[0] = iloc;
      maxval[0] = amax;
    } else {	/* No energy in signal; fake reasonable return vals. */
      maxloc[0] = 0;
      maxval[0] = 0.0f;
      for (i = 0; i < nlags; i++)
        correl[i] = 0;
    }
  }

  /**
   * -----------------------------------------------------------------------
   * * Use parabolic interpolation over the three points defining the peak
   * vicinity to estimate the "true" peak.
   */
  public static void peak(float[] y, int idx, float[] xp, float[] yp) {

    float a, c;

    a = (float) ((y[2 + idx] - y[1 + idx]) + (.5 * (y[idx] - y[2 + idx])));
    if (Math.abs(a) > .000001) {
      xp[0] = c = (float) ((y[idx] - y[2 + idx]) / (4.0 * a));
      yp[0] = y[1 + idx] - (a * c * c);
    } else {
      xp[0] = 0.0f;
      yp[0] = y[1 + idx];
    }
  }

  /**
   * -----------------------------------------------------------------------
   * /* Get likely candidates for F0 peaks.
   */

  public static void get_cand(Cross cross, float[] peak, int[] loc, int nlags, int[] ncand, float cand_thresh) {

    int i, lastl, t_idx;
    float o, p, q, clip;
    int r_idx, s_idx;
    int start, ncan;

    clip = (cand_thresh * cross.maxval);

    lastl = nlags - 2;
    start = cross.firstlag;

    r_idx = 0;  // cross.correl
    s_idx = 0;  // peak
    t_idx = 0;  // loc

    o = cross.correl[r_idx++];			/* first point */
    q = cross.correl[r_idx++];	    /* middle point */
    p = cross.correl[r_idx++];
    ncan = 0;
    for (i = 1; i < lastl; i++, o = q, q = p, p = cross.correl[r_idx++]) {
      if ((q > clip) &&		/* is this a high enough value? */
          (q >= p) && (q >= o)) { /* NOTE: this finds SHOULDERS and PLATEAUS
                                   as well as peaks (is this a good idea?) */
        peak[s_idx++] = q;		/* record the peak value */
        loc[t_idx++] = i + start;	/* and its location */
        ncan++;			/* count number of peaks found */
      }
    }
    ncand[0] = ncan;
  }


  /**
   * stationarity parameters -
   * STAT_WSIZE: window size in sec used in measuring frame energy/stationarity
   * STAT_AINT: analysis interval in sec in measuring frame energy/stationarity
   */

  public static final float STAT_WSIZE = 0.030f;
  public static final float STAT_AINT = 0.020f;

  public int nframes_old = 0;
  public Stat stat = null;
  public float[] mem = null;
  public float[] b = new float[2048];
  public float[] foutput = null;

  public int[] ncoeff = new int[]{127};
  public int ncoefft = 0;

  public float[] downsample(float[] input, int buff_size, int state_idx, double freq, int[] samsds /* scalar */,
                            int decimate,
                            boolean first, boolean last_time) {

    float beta;
    int init;

    if (input != null && buff_size > 0 && decimate > 0 && samsds != null) {
      if (decimate == 1) {
        return input;
      }

      if (first) {
        int nbuff = (buff_size / decimate);
        ncoeff[0] = ((int) (freq * .005)) | 1;
        beta = .5f / decimate;

        foutput = new float[nbuff];
        for (int i = 0; i < nbuff; i++)
          foutput[i] = 0.0f;

        if (!lc_lin_fir(beta, ncoeff, b)) {
          AuToBIUtils.error("Problems computing interpolation filter.");
          return null;
        }

        ncoefft = (ncoeff[0] / 2) + 1;
      }

      if (first) {
        init = 1;
      } else if (last_time) {
        init = 2;
      } else {
        init = 0;
      }

      if (downsamp(input, foutput, buff_size, samsds, state_idx, decimate, ncoefft, b, init)) {
        return foutput;
      } else {
        AuToBIUtils.error("Problems with downsamp called from downsample");
        return null;
      }
    }

    return null;
  }

  /**
   * create the coefficients for a symmetric FIR lowpass filter using the
   * window technique with a Hanning window.
   */
  private boolean lc_lin_fir(float fc, int[] nf, float[] coef) {
    int i, n;
    double twopi, fn, c;

    if (((nf[0] % 2) != 1)) {
      nf[0] = nf[0] + 1;
    }

    n = (nf[0] + 1) / 2;

    /*  Compute part of the ideal impulse response (the sin(x)/x kernel). */
    twopi = Math.PI * 2.0;
    coef[0] = (float) (2.0 * fc);
    c = Math.PI;
    fn = twopi * fc;
    for (i = 1; i < n; i++) {
      coef[i] = (float) (Math.sin(i * fn) / (c * i));
    }

    /* Now apply a Hanning window to the (infinite) impulse response. */
    /* (Probably should use a better window, like Kaiser...) */
    fn = twopi / (double) (nf[0]);
    for (i = 0; i < n; i++) {
      coef[n - i - 1] *= (float) ((.5 - (.5 * Math.cos(fn * ((double) i + 0.5)))));
    }

    return true;
  }

  public boolean downsamp(float[] input, float[] output, int insize, int[] outsize, int state_idx, int decimate,
                          int ncoefft, float[] b, int init) {
    if (input != null && output != null) {
      doFFIR(input, insize, output, outsize, state_idx, ncoefft, b, false, decimate, init);
      return true;
    } else {
      AuToBIUtils.error("null input or output signal passed to downsamp().");
      return false;
    }
  }


  int FFIR_fsize = 0;
  float[] FFIR_co = null;
  float[] FFIR_mem = null;
  float[] FFIR_state = new float[1000];

  /**
   * From jkGetF0.c
   * <p/>
   * fc contains 1/2 the coefficients of a symmetric FIR filter with unity
   * passband gain.
   * This filter is convolved with the signal in input.
   * The output is placed in output.
   * <p/>
   * If(invert), the filter magnitude
   * response will be inverted.
   * <p/>
   * If(init&1), beginning of signal is in input;
   * if(init&2), end of signal is in input.
   * outsize is set to the number of
   *
   * @param input     input data
   * @param insize    size of the input data
   * @param output    output data
   * @param outsize   size of the output data
   * @param state_idx index into the state array (storing previous input data)
   * @param ncoef     number of fir coefficients
   * @param fc        filter ceofficients
   * @param invert    return inverted filter magnitude response?
   * @param skip      number of samples to skip when downsampling
   * @param init      Is the beginning of the signal is already loaded? Is the end?
   */
  public void doFFIR(float[] input, int insize, float[] output, int[] outsize, int state_idx, int ncoef, float[] fc,
                     boolean invert, int skip, int init) {

    // Reallocate static FIR filter parameters.
    if (ncoef > FFIR_fsize) {
      int ful_ffir_size = (ncoef + 1) * 2;

      FFIR_co = new float[ful_ffir_size];
      FFIR_mem = new float[ful_ffir_size];
      FFIR_fsize = ncoef;
    }

    // Fill the second half of mem with input data.
    int in_idx = 0;
    int dp1_memidx = ncoef - 1;
    for (int i = 0; i < ncoef; i++) {
      FFIR_mem[dp1_memidx + i] = input[in_idx++];
    }
    float sum;

    if ((init & 1) != 0) { /* Is the beginning of the signal in buf? */
      /* Copy the half-filter and its mirror image into the coefficient array. */
      int dp3_fcidx = ncoef - 1;
      int dp2_coidx = 0;
      int dp1_coidx = (ncoef - 1) * 2;
      float integral = 0.0f;

      for (int i = 0; i < ncoef - 1; i++) {
        if (!invert) {
          FFIR_co[dp1_coidx--] = FFIR_co[dp2_coidx++] = fc[dp3_fcidx--];
        } else {
          integral += (sum = fc[dp3_fcidx--]);
          FFIR_co[dp1_coidx--] = FFIR_co[dp2_coidx++] = -sum;
        }
      }
      if (!invert) { // set point of symmetry
        FFIR_co[dp1_coidx] = fc[dp3_fcidx];
      } else {
        integral *= 2;
        integral += fc[dp3_fcidx];
        FFIR_co[dp1_coidx] = integral - fc[dp3_fcidx];
      }

      for (int i = 0; i < ncoef - 1; i++) {
        FFIR_mem[i] = 0;
      }
    } else {
      System.arraycopy(FFIR_state, 0, FFIR_mem, 0, ncoef - 1);
    }

    int resid;
    int k = (ncoef << 1) - 1;

    int out_idx = 0;

    for (int l = 0; l < outsize[0]; l++) {
      dp1_memidx = 0;
      int dp2_coidx = 0;
      int dp3_memidx = skip;
      sum = 0.0f;
      for (int j = k - skip; j-- > 0; ) {

        sum += FFIR_co[dp2_coidx++] * FFIR_mem[dp1_memidx];
        FFIR_mem[dp1_memidx++] = FFIR_mem[dp3_memidx++];
      }

      for (int j = skip; j-- > 0; ) {/* new data to memory */
        sum += FFIR_co[dp2_coidx++] * FFIR_mem[dp1_memidx];
        FFIR_mem[dp1_memidx++] = input[in_idx++];
      }
      output[out_idx++] = (sum < 0.0) ? sum - 0.5f : sum + 0.5f;
    }

    if ((init & 2) != 0) {  // we're at the end, and there are still some samples left.
      resid = insize - outsize[0] * skip;

      for (int l = resid / skip; l-- > 0; ) {
        dp1_memidx = 0;
        int dp2_coidx = 0;
        int dp3_memidx = skip;
        sum = 0.0f;
        for (int j = k - skip; j-- > 0; ) {
          sum += FFIR_co[dp2_coidx++] * FFIR_mem[dp1_memidx];
          FFIR_mem[dp1_memidx++] = FFIR_mem[dp3_memidx++];
        }
        for (int j = skip; j-- > 0; FFIR_mem[dp1_memidx++] = 0.0f)
          sum += FFIR_co[dp2_coidx++] * FFIR_mem[dp1_memidx];
        output[out_idx++] = (sum < 0.0) ? sum - 0.5f : sum + 0.5f;
        outsize[0]++;
      }
    } else {  // we're not at the end, let's keep some (ncoef-1) of the signal in "state"
      int dp3_inidx = state_idx - ncoef + 1;
      int s_idx = 0;
      for (int l = ncoef - 1; l-- > 0; ) {
        FFIR_state[s_idx++] = input[dp3_inidx++];
      }
    }
  }


  public Stat getStationarity(float[] fdata, int fidx, double freq, int buff_size, int nframes, int frame_step,
                              boolean first) throws AuToBIException {

    // AR: may need a representation of fdata that can be indexed before 0.

    /* static */
    float preemp = 0.4f;
    float stab = 30.0f;

    int p, q, r, datend; // indices into fdata
    int ind, i, j, m, size, order, agap, w_type = 3;

    agap = (int) (STAT_AINT * freq);   // number of frames in the analysis interval
    size = (int) (STAT_WSIZE * freq);  // number of frames in a window
    ind = (agap - size) / 2;           // starting index

    // initialize stat and memory space
    if (nframes_old < nframes || stat == null || first) {
      nframes_old = nframes;
      stat = new Stat(nframes);
      mem = new float[(int) (STAT_WSIZE * freq) + (int) (STAT_AINT * freq)];
    }

    if (nframes == 0) {
      return stat;
    }

    q = fidx + ind;             // index into fdata   AR: what if ind is negative???
    datend = fidx + buff_size;  // index into fdata

    if ((order = (int) (2.0 + (freq / 1000.0))) > BIGSORD) {
      order = BIGSORD;
    }

    // prepare first frame
    for (j = mem.length / 2, i = 0; j < mem.length; j++, i++) {
      mem[j] = fdata[i];
    }

    for (j = 0, p = q - agap; j < nframes; j++, p += frame_step, q += frame_step) {
      if ((p >= fidx) && (q >= fidx) && (q + size <= datend)) {
        // fdata and p and q offsets correspond to p and q arrays in SPTK.
        stat.stat[j] = getSimilarity(order, size, fdata, p, q, j, preemp, stab, w_type, false);
      } else {
        if (first) {
          if ((p < fidx) && (q >= fidx) && (q + size <= datend)) {
            stat.stat[j] = getSimilarity(order, size, fdata, -1, q, j, preemp, stab, w_type, true);
          } else {  // either q is starting too early, or datend is too short.
            stat.rms[j] = 0;
            stat.stat[j] = 0.1f * 0.2f; // a big transition
            stat.rms_ratio[j] = 1.0f;   // no amplitude change.
          }
        } else {
          if ((p < fidx) && (q + size <= datend)) {
            stat.stat[j] = getSimilarity(order, size, mem, 0, (mem.length / 2) + ind, j, preemp, stab, w_type, false);
            // Prepare the next frame.
            if (p + frame_step < fidx) {
              for (m = 0; m < (mem.length - frame_step); m++) {
                mem[m] = mem[m + frame_step]; // slide memory to the left
              }
              r = q + size;
              for (m = 0; m < frame_step; m++) {
                mem[mem.length - frame_step + m] = fdata[r++];
              }
            }
          }
        }
      }
    }

    // last frame, prepare for next call, by filling the first half of mem
    for (j = (mem.length / 2) - 1, p = fidx + (nframes * frame_step) - 1; j >= 0 && p >= fidx; j--) {
      mem[j] = fdata[p--];
    }
    return stat;
  }

  public float getSimilarity(int order, int size, float[] data, int dprev, int dcur, int statidx, float preemp,
                             float stab, int w_type, boolean init) throws AuToBIException {

    float[] rho1 = new float[BIGSORD + 1];
    float[] rho3 = new float[BIGSORD + 1];
    float[] a1 = new float[BIGSORD + 1];
    float[] a2 = new float[BIGSORD + 1];
    float[] b = new float[BIGSORD + 1];

    // Unit length arrays to handle C-style multiple return values.
    float[] rms1 = new float[1];
    float[] err1 = new float[1];
    float[] err3 = new float[1];
    float[] rmsd1 = new float[1];
    float[] rmsd3 = new float[1];
    float[] b0 = new float[1];
    float rms3, t;

    xlpc(order, stab, size - 1, data, dcur, a2, rho3, null, err3, rmsd3, preemp, w_type);
    rms3 = windEnergy(data, dcur, size, w_type);
    rms1[0] = 0.f;
    if (!init) {
      // Get previous window stat
      if (!retrieveWindowStat(rho1, order, err1, rms1)) {
        xlpc(order, stab, size - 1, data, dprev, a1, rho1, null, err1, rmsd1, preemp, w_type);
        rms1[0] = windEnergy(data, dprev, size, w_type);
      }
      xaToaca(a2, b, b0, order);
      t = xitakura(order, b, b0, rho1, 1, err1) - 0.8f;

      if (rms1[0] > 0.0) {
        stat.rms_ratio[statidx] = 2f;
      } else {
        stat.rms_ratio[statidx] = 1f;
      }
    } else {
      t = 10.0f;
      stat.rms_ratio[statidx] = 1.0f;
    }

    stat.rms[statidx] = rms3;
    saveWindowStat(rho3, order, err3[0], rms3);
    return 0.2f / t;
  }

  public boolean retrieveWindowStat(float[] rho, int order, float[] err, float[] rms) {
    WindowStat wstat;
    int i;

    if (wReuse > 0) {
      wstat = windstat[0];
      for (i = 0; i <= order; i++)
        rho[i] = wstat.rho[i];
      err[0] = wstat.err;
      rms[0] = wstat.rms;
      return true;
    } else {
      return false;
    }
  }

  /* push window stat to stack, and pop the oldest one */

  public int saveWindowStat(float[] rho, int order, float err, float rms) {
    int i, j;

    if (wReuse > 1) {               /* push down the stack */
      for (j = 1; j < wReuse; j++) {
        for (i = 0; i <= order; i++)
          windstat[j - 1].rho[i] = windstat[j].rho[i];
        windstat[j - 1].err = windstat[j].err;
        windstat[j - 1].rms = windstat[j].rms;
      }
      for (i = 0; i <= order; i++)
        windstat[wReuse - 1].rho[i] = rho[i]; /*save*/
      windstat[wReuse - 1].err = err;
      windstat[wReuse - 1].rms = rms;
      return 1;
    } else if (wReuse == 1) {
      for (i = 0; i <= order; i++)
        windstat[0].rho[i] = rho[i];  /* save */
      windstat[0].err = err;
      windstat[0].rms = rms;
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Compute the autocorrelations of the p LP coefficients in a.
   * (a[0] is assumed to be = 1 and not explicitely accessed.)
   * The magnitude of a is returned in c.
   * 2* the other autocorrelation coefficients are returned in b.
   */
  void xaToaca(float[] a, float[] b, float[] c, int p) {
    float s;
    int ap_idx = 0;
    int a0_idx;
    int b_idx = 0;

    int i, j;

    for (s = 1.f, i = p; i-- > 0; ap_idx++)
      s += a[ap_idx] * a[ap_idx];

    c[0] = s;
    for (i = 1; i <= p; i++) {
      s = a[i - 1];
      for (a0_idx = 0, ap_idx = i, j = p - i; j-- > 0; )
        s += (a[a0_idx++] * a[ap_idx++]);
      b[b_idx++] = (float) (2. * s);
    }
  }

  /**
   * Compute the Itakura LPC distance between the model represented
   * by the signal autocorrelation (r) and its residual (gain) and
   * the model represented by an LPC autocorrelation (c, b).
   * Both models are of order p.
   * r is assumed normalized and r[0]=1 is not explicitly accessed.
   * Values returned by the function are >= 1.
   */
  float xitakura(int p, float[] b, float[] c, float[] r, int r_offset, float[] gain) {
    float s;

    int r_idx = 0;
    int b_idx = 0;
    for (s = c[0]; p-- > 0; )
      s += r[r_offset + r_idx++] * b[b_idx++];

    return (s / gain[0]);
  }

  /**
   * Compute the time-weighted RMS of a size segment of data.  The data
   * is weighted by a window of type w_type before RMS computation.  w_type
   * is decoded above in window().
   *
   * @param data   input data
   * @param didx   offset into the input data
   * @param size   size of the window overwhich to calculate energy
   * @param w_type window type
   * @return the energy in the window
   */
  public float windEnergy(float[] data, int didx, int size, int w_type) {
    if (nwind < size) {
      dwind = new float[size];
    }
    if (nwind != size) {
      getWindow(dwind, size, w_type);
      nwind = size;
    }
    float sum = 0.f;
    for (int i = 0; i < size; i++) {
      float f = dwind[i] * data[i + didx];
      sum += f * f;
    }

    return (float) Math.sqrt(sum / size);
  }

  /**
   * Generate a time weighted window with type and length n.
   *
   * @param dout output data
   * @param n    the size of the window
   * @param type type of window function
   * @return true if the window was allocated.  False if there was a problem.
   */
  public boolean getWindow(float[] dout, int n, int type) {
    float[] din = new float[n];
    for (int i = 0; i < n; i++) {
      din[i] = 1;
    }
    return window(din, 0, dout, n, 0.f, type);
  }

  public boolean xlpc(int lpc_ord, float lpc_stabl, int wsize, float[] data, int didx, float[] lpca, float[] ar,
                      float[] lpck, float normerr[] /* scalar */, float rms[], float preemp, int type) throws
      AuToBIException {

    float[] rho = new float[BIGSORD + 1];
    float[] k = new float[BIGSORD];
    float[] a = new float[BIGSORD + 1];
    float[] r = null;
    float[] er = new float[1];
    float[] en = new float[1];
    en[0] = 1f;
    float wfact = 1f;


    if ((wsize <= 0) || (data == null) || (lpc_ord > BIGSORD)) return false;
    if (nwind != wsize) {
      if (dwind != null) {
        // resize array to fit.
        // AR: look into arraylists for this.
        dwind = Arrays.copyOf(dwind, wsize);
      } else {
        dwind = new float[wsize];
      }
      nwind = wsize;
    }

    window(data, didx, dwind, wsize, preemp, type);
    if (ar != null) r = rho;
    if (lpca != null) lpca = a;
    xautoc(wsize, dwind, lpc_ord, r, en);

    if (lpc_stabl > 1) {  // add a little to the diagonal
      float ffact = (float) (1 / (1 + Math.exp((-lpc_stabl / 20) * Math.log(10))));
      if (r == null || r.length < lpc_ord) throw new AuToBIException("Problem with LPC calculation.");
      for (int i = 1; i <= lpc_ord; i++) rho[i] = ffact * r[i];
      rho[0] = r[0];
      r = rho;
      System.arraycopy(r, 0, ar, 0, lpc_ord + 1);
    }
    xdurbin(r, k, a, lpc_ord, er);

    switch (type) {
      case 0:
        wfact = 1f;         // rectangular
        break;
      case 1:
        wfact = 0.630397f;  // Hamming
        break;
      case 2:
        wfact = 0.443149f;  // (0.5 - 0.5*cos)^4
        break;
      case 3:
        wfact = 0.612372f;  // Hanning
        break;
    }
    assert lpca != null;
    lpca[0] = 1f;

    // Java hacks to return multiple optional values
    if (rms != null) rms[0] = en[0] / wfact;
    if (normerr != null) normerr[0] = er[0];
    return true;
  }

  public boolean window(float[] din, int didx, float[] dout, int n, float preemp, int type) {

    switch (type) {
      case 0:  // rectangular
        xrwindow(din, didx, dout, n, preemp);
        break;
      case 1:  // Hamming
        xhamwindow(din, didx, dout, n, preemp);
        break;
      case 2:  // cos^4
        xcwindow(din, didx, dout, n, preemp);
        break;
      case 3:  // Hanning
        xhanwindow(din, didx, dout, n, preemp);
        break;
      default:
        AuToBIUtils.error("Unknown window type requested in window(): " + type);
        return false;
    }

    return true;
  }

  /**
   * Apply a rectangular window and optionally preemphasize
   *
   * @param din    input data
   * @param didx   starting index
   * @param dout   output data
   * @param n      number of output data points
   * @param preemp degree of preemphasis
   */
  public void xrwindow(float[] din, int didx, float[] dout, int n, float preemp) {
    if (preemp != 0) {
      for (int i = 0; i < n; i++) {
        dout[i] = din[didx + i + 1] - (preemp * din[didx + i]);
      }
    } else {
      System.arraycopy(din, didx, dout, 0, n);
    }
  }

  /**
   * Apply a Hamming window to the signal in din.
   * <p/>
   * Uses a member variable to avoid repeated computation of hamming windows of the same size.
   *
   * @param din    input data
   * @param didx   offset into the input data
   * @param dout   output data
   * @param n      the size of the window
   * @param preemp the preemphasis to apply (if any)
   */
  public void xhamwindow(float[] din, int didx, float[] dout, int n, float preemp) {
    // Generate the window if it doesn't exist
    if (hamwind == null || hamwind.length != n) {
      hamwind = new float[n];

      float arg = (float) ((Math.PI * 2.) / hamwind.length);

      for (int i = 0; i < n; i++) {
        hamwind[i] = (float) (.54 - .46 * Math.cos((0.5 + i) * arg));
      }
    }

    // Apply window and any preemphasis.
    if (preemp != 0) {
      for (int i = 0; i < n; i++) {
        dout[i] = hamwind[i] * (din[didx + i + 1] - (preemp * din[didx + i]));
      }
    } else {
      for (int i = 0; i < n; i++) {
        dout[i] = hamwind[i] * din[didx + i];
      }
    }
  }

  /**
   * Apply a Hanning window to the signal in din.
   * <p/>
   * Uses a member variable to avoid repeated computation of hamming windows of the same size.
   *
   * @param din    input data
   * @param didx   offset into the input data
   * @param dout   output data
   * @param n      the size of the window
   * @param preemp the preemphasis to apply (if any)
   */
  public void xhanwindow(float[] din, int didx, float[] dout, int n, float preemp) {
    // Generate the window if it doesn't exist
    if (hanwind == null || hanwind.length != n) {
      hanwind = new float[n];

      float arg = (float) ((Math.PI * 2.) / hanwind.length);

      for (int i = 0; i < n; i++) {
        hanwind[i] = (float) (0.5 - 0.5 * Math.cos((0.5 + i) * arg));
      }
    }

    // Apply window and any preemphasis.
    if (preemp != 0) {
      for (int i = 0; i < n; i++) {
        dout[i] = hanwind[i] * (din[didx + i + 1] - (preemp * din[didx + i]));
      }
    } else {
      for (int i = 0; i < n; i++) {
        dout[i] = hanwind[i] * din[didx + i];
      }
    }
  }

  /**
   * Apply a cos^4 window to the signal in din.
   * <p/>
   * Uses a member variable to avoid repeated computation of hamming windows of the same size.
   *
   * @param din    input data
   * @param didx   offset into the input data
   * @param dout   output data
   * @param n      the size of the window
   * @param preemp the preemphasis to apply (if any)
   */
  public void xcwindow(float[] din, int didx, float[] dout, int n, float preemp) {
    // Generate the window if it doesn't exist
    if (coswind == null || coswind.length != n) {
      coswind = new float[n];

      float arg = (float) ((Math.PI * 2.) / coswind.length);

      for (int i = 0; i < n; i++) {
        float cos = (float) (0.5 * (1 - Math.cos((0.5 + i) * arg)));
        coswind[i] = cos * cos * cos * cos;
      }
    }

    // Apply window and any preemphasis.
    if (preemp != 0) {
      for (int i = 0; i < n; i++) {
        dout[i] = coswind[i] * (din[didx + i + 1] - (preemp * din[didx + i]));
      }
    } else {
      for (int i = 0; i < n; i++) {
        dout[i] = coswind[i] * din[didx + i];
      }
    }
  }

  /**
   * Compute the pp+1 autocorrelation lags of the windowsize samples in s.
   * Return the normalized autocorrelation coefficients in r.
   * The rms is returned in e.
   */
  public void xautoc(int wsize, float[] s, int p, float[] r, float[] e /* scalar*/) {

    float sum, sum0 = 0;
    for (int j = 0; j < wsize; j++) {
      sum = s[j];
      sum0 += sum * sum;
    }
    r[0] = 1f;
    if (sum0 == 0.0f) {  // no energy.  fake low-energy white noise autocorr
      e[0] = 1.0f;
      for (int i = 1; i <= p; i++) {
        r[i] = 0.0f;
      }
      return;
    }
    e[0] = (float) Math.sqrt((double) (sum0 / wsize));
    sum0 = (float) (1.0 / sum0);
    for (int i = 1; i <= p; i++) {
      sum = 0.f;
      for (int j = 0; j < wsize - i; j++) {
        sum += s[j] * s[j + i];
      }
      r[i] = sum * sum0;
    }
  }

  /**
   * Using Durbin's recursion, convert the autocorrelation sequence in r
   * to reflection coefficients in k and predictor coefficients in a.
   * The prediction error energy (gain) is left in ex[0].
   * Note: durbin returns the coefficients in normal sign format.
   * (i.e. a[0] is assumed to be = +1.)
   */
  public void xdurbin(float[] r, float[] k, float[] a, int p, float[] ex /*scalar*/) {

    float[] b = new float[BIGSORD];
    float e = r[0];
    k[0] = -r[1] / e;
    a[0] = k[0];
    e *= (float) (1. - (k[0] * k[0]));

    for (int i = 1; i < p; i++) {
      float s = 0;
      for (int j = 0; j < i; j++) {
        s -= a[j] * r[i - j];
      }
      k[i] = (s - r[i + 1]) / e;
      a[i] = k[i];
      System.arraycopy(a, 0, b, 0, i + 1);
      for (int j = 0; j < i; j++) {
        a[j] += k[i] * b[i - j - 1];
      }
      e *= (float) (1. - (k[i] * k[i]));
    }

    ex[0] = e;
  }


  int global_count = 0;

  /**
   * Calculates the pitch of the wave file, wav, and constructs a PitchContour.
   * <p/>
   * Uses the RAPT (aka esps get_f0) algorithm.
   *
   * @param wav the wave data
   * @return a pitch contour object
   * @throws AuToBIException
   */
  public PitchContour getPitch(WavData wav) throws AuToBIException {
    double sf = wav.sampleRate;
    int frame_shift = (int) Math.round(par.frame_step * sf);

    int fnum = (int) (Math.ceil((double) wav.getNumSamples() / (double) frame_shift));

    double fsp = sf * (10.0 / (double) frame_shift);
    int alpha = (int) (0.00275 * fsp + 0.5);
    int beta = (int) ((9600.0 / par.minF0 - 168.0) * fsp / 96000.0 + 0.5);
    if (beta < 0) {
      beta = 0;
    }
    int padding = (alpha + beta + 3) * frame_shift;

    int total_samples = wav.getNumSamples() + padding;
    if (total_samples < ((par.frame_step * 2) + par.wind_dur) * sf) {
      throw new AuToBIException("Sound is too short given the frame and window sizes");
    }

    /*
     Initialize DP table:   int init_dp_f0()
        this fills in buff_size and sdstep
      */
    long[] buffsize = new long[1], sdstep = new long[1];
    int initd = initDP(sf, par, buffsize, sdstep);

    double t0 = par.wind_dur / 2;   // Initial time is the center of the first window.
    if (initd > 0 || buffsize[0] > Integer.MAX_VALUE || sdstep[0] > Integer.MAX_VALUE) {
      throw new AuToBIException("Problem with DP initialization.");
    }

    if (buffsize[0] > total_samples) {
      buffsize[0] = total_samples;
    }

    int max = (int) (buffsize[0] > sdstep[0] ? buffsize[0] : sdstep[0]);
    long actsize = buffsize[0] < total_samples ? buffsize[0] : total_samples;

    boolean done;
    int[] vecsize = new int[1];
    float[] fdata = new float[max];
    int length = total_samples;
    int ndone = 0;

    ArrayList<Double> f0 = new ArrayList<Double>();
    ArrayList<Double> vuv = new ArrayList<Double>();
    while (true) {

      done = ((actsize < buffsize[0]) || (total_samples == buffsize[0]));

      loadData(wav, fdata, ndone, actsize);

      global_count++;
      if (dpF0(fdata, (int) actsize, (int) sdstep[0], sf, par, f0p, vuvp, rms_speech, acpkp, vecsize, done) != 0) {
        System.out.println("dpF0 failed, but you can't see the error.");
      }

      // Accumulate f0p and vuvp in reverse order...
      for (int i = vecsize[0] - 1; i >= 0; i--) {
        f0.add((double) f0p[i]);
        vuv.add((double) vuvp[i]);
      }

      if (done) break;

      ndone += sdstep[0];
      actsize = Math.min(buffsize[0], length - ndone);
      total_samples -= sdstep[0];

      if (actsize > total_samples) {
        actsize = total_samples;
      }
    }

    // put f0p into a PitchContour object.
    // TODO: put vuvp into pitchContour.

    // The pitch extractor will calculate extra frames based on padded noise at the end of the audio.
    // only output the frames corresponding to the original file.
    PitchContour out = new PitchContour(t0, par.frame_step, Math.min(fnum, f0.size()));
    for (int i = 0; i < Math.min(fnum, f0.size()); i++) {
      if (f0.get(i) > 0) {
        out.set(i, f0.get(i));
      }
    }
    return out;
  }

  /**
   * Copies audio data from wav data into float based on an offset.
   *
   * @param wav      The wave data object.
   * @param fdata    a preallocated array to store the raw PCM data as a float.
   * @param pos      pcm frame offset
   * @param nsamples the number of samples to load
   */
  public void loadData(WavData wav, float[] fdata, int pos, long nsamples) {
    Random rand = new Random();

    for (int i = 0; i < nsamples; i++) {
      // AR: this is a kind of crappy way to convert back to the raw sample.
      if (i + pos < wav.getNumSamples()) {
        fdata[i] = (float) wav.getSample(0, i + pos) * (1 << (wav.sampleSize - 1));
      } else { // Pad the end of fdata with some extra noise to enable analysis at the end of the file
        fdata[i] = 0.0f;
      }

      // Add gaussian noise.  According to RAPT documentation and paper, this provides some noise robustness.
      fdata[i] += rand.nextGaussian() * 50;
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

      RAPTPitchExtractor pitchExtractor = new RAPTPitchExtractor();
      PitchContour pitch = pitchExtractor.getPitch(wav);

      System.out.println("pitch points:" + pitch.size());

      for (int i = 0; i < pitch.size(); ++i) {
        System.out
            .println(
                "point[" + i + "]: " + pitch.get(i) + " -- " + pitch.timeFromIndex(i) + ":" + pitch.getStrength(i));
      }
      System.out.println(wav.getDuration());
    } catch (AuToBIException e) {
      e.printStackTrace();
    }
  }
}
