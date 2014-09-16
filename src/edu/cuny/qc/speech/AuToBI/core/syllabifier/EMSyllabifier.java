/**
 EMSyllabifier.java

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
package edu.cuny.qc.speech.AuToBI.core.syllabifier;

import edu.cuny.qc.speech.AuToBI.IntensityExtractor;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Pair;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.WavData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * EMSyllabifier is an Expectation-Maximization based pseudosyllabification routine.  This was developed at Dan Ellis's
 * LabROSA at Columbia University and described in "Stylization of Pitch with Syllable-based Linear Segments" by Suman
 * Ravuri and Dan Ellis, ICASSP 2008.  The method implemented here was described via personal communication with Dan
 * Ellis and differs from
 * the approach described in this paper.
 * <p/>
 * The approach here is as follows: seed a Gaussian every 100ms, with a
 * (100ms)^2 variance, then do EM re-estimation from there.  Then prune
 * out any highly-overlapped Gaussian that is weaker than its immediate
 * neighbor (unlikely to be separate syllables), and all Gaussians with peak values smaller than 5% of the
 * largest (silence).
 */
public class EMSyllabifier extends Syllabifier {

  private double thresh = 0.1;

  @Override
  public List<Region> generatePseudosyllableRegions(WavData wav) {
    IntensityExtractor ie = new IntensityExtractor(wav);
    Contour intensity = ie.soundToIntensity();

    Contour energy = expContour(intensity);
    List<GMMComponent> components = initializeComponents(wav.getDuration());
    fitComponents(components, energy);
    pruneOverlappingComponents(components);
    markSmallComponents(components);
    return toSegmentBoundaries(components, wav.getDuration());
  }

  private Contour expContour(Contour c) {
    Contour energy = new Contour(c.getStart(), c.getStep(), c.size());
    for (Pair<Double, Double> p : c) {
      energy.set(p.first, Math.pow(p.second / 10, 10));
    }
    return energy;
  }

  private List<Region> toSegmentBoundaries(List<GMMComponent> components, double max_time) {
    List<Region> regions = new LinkedList<Region>();
    double start = 0.0;
    for (int i = 0; i < components.size() - 1; i++) {
      GMMComponent m1 = components.get(i);
      GMMComponent m2 = components.get(i + 1);
      // the end of the component is the intersection of the gaussians.
      double end = intersection(m1, m2);

      if (!m1.isSilence) {
        regions.add(new Region(start, end));
      }
      start = end;
    }
    if (!components.get(components.size() - 1).isSilence) {
      regions.add(new Region(start, max_time));
    }
    return regions;
  }

  /**
   * Calculates the intersection point between two gaussians represented by MixtureComponents
   *
   * @param m1 mixture component 1
   * @param m2 mixture component 2
   * @return intersection x-coordinate
   */
  public double intersection(GMMComponent m1, GMMComponent m2) {

    if (m1.variance == m2.variance) {
      // Equal variances require a different calculation.
      if (m1.weight != m2.weight) {
        return (2 * m1.variance * (Math.log(m2.weight) - Math.log(m1.weight)) + m1.mean * m1.mean - m2.mean * m2.mean) /
            (2.0 * (m1.mean - m2.mean));
      } else {
        return (m1.mean + m2.mean) / 2;
      }
    }

    double v1 = m1.variance;
    double v2 = m2.variance;
    double u1 = m1.mean;
    double u2 = m2.mean;
    double w1 = m1.weight;
    double w2 = m2.weight;

    // make sure component one is smaller
    if (u2 < u1) {
      u1 = m2.mean;
      v1 = m2.variance;
      w1 = m2.weight;

      u2 = m1.mean;
      v2 = m1.variance;
      w2 = m1.weight;
    }


    // Closed form solution for the intersection of two gaussians.
    double denom = v1 - v2;
    double a = (v2 * u1 - v1 * u2) / denom;
    double b =
        (2 * v1 * v2 * (Math.log(w2 * Math.sqrt(v1)) - Math.log(w1 * Math.sqrt(v2))) + v2 * u1 * u1 - v1 * u2 * u2) /
            denom;

    double xp = Math.sqrt(b + a * a) - a;
    double xm = -Math.sqrt(b + a * a) - a;

    if (xp > u1 && xp < u2) {
      return xp;
    } else if (xm > u1 && xm < u2) {
      return xm;
    } else {
      return Double.NaN;
    }
  }

  /**
   * If two components are highly overlapping, remove the one corresponding to a lower intensity.
   *
   * @param components the components
   */
  private void pruneOverlappingComponents(List<GMMComponent> components) {
    int i = 0;
    while (i < components.size() - 1) {
      if (overlapping(components.get(i), components.get(i + 1))) {
        if (components.get(i).weight * components.get(i).n > components.get(i + 1).weight * components.get(i + 1).n) {
          components.remove(i + 1);
        } else {
          components.remove(i);
        }
      } else {
        i = i + 1;
      }
    }
  }

  private boolean overlapping(GMMComponent m1, GMMComponent m2) {
    GMMComponent mm1 = new GMMComponent(m1.mean, m1.variance);
    mm1.weight = m1.weight;
    mm1.n = m1.n;
    GMMComponent mm2 = new GMMComponent(m2.mean, m2.variance);
    mm2.weight = m2.weight;
    mm2.n = m2.n;
    if (mm1.mean > mm2.mean) {
      GMMComponent tmp = mm1;
      mm1 = mm2;
      mm2 = tmp;
    }
    // Use absolute weights for calculating intersection.
    mm1.weight = mm1.weight * mm1.n;
    mm2.weight = mm2.weight * mm2.n;
    double x = intersection(mm1, mm2);
    if (Double.isNaN(x) || x < mm1.mean + thresh * Math.sqrt(mm1.variance) ||
        x > mm2.mean - thresh * Math.sqrt(mm2.variance)) {
      return true;
    }
    return false;
  }

  /**
   * Mark those components whose weight is below 5% of the maximum weight to consider them silence.
   *
   * @param components mixture component
   */
  private void markSmallComponents(List<GMMComponent> components) {
    double max = 0;
    for (GMMComponent m : components) {
      max = Math.max(max, m.weight * m.n);
    }
    for (GMMComponent m : components) {
      // Threshold silence as a peak of less than 25 db from the max
      if (m.weight * m.n < max * 0.01) {
        m.isSilence = true;
      }
    }
  }


  /**
   * Fits the mixture components to a contour using EM.
   *
   * @param components the mixture components.
   * @param c          the intensity contour
   */
  public void fitComponents(List<GMMComponent> components, Contour c) {
    // TODO: allow for a maximum number of iterations
    double old_ll;
    double ll = calcLikelihood(components, c);
    do {
      old_ll = ll;
      // calculate responsibilities
      double[][] resp = calcResponsibilities(components, c);

      // reestimate parameters
      updateComponents(components, c, resp);
      ll = calcLikelihood(components, c);
    } while (Math.abs(ll - old_ll) > 0.0001);
  }

  private void updateComponents(List<GMMComponent> components, Contour intensity, double[][] resp) {
    double total_n = 0;
    for (int i = 0; i < components.size(); ++i) {
      GMMComponent m = components.get(i);
      double sum = 0;
      double n_i = 0;
      for (int j = 0; j < intensity.size(); ++j) {
        sum += resp[i][j] * intensity.timeFromIndex(j);
        n_i += resp[i][j];
      }
      if (n_i > 0) {
        m.mean = sum / n_i;
      } else {
        m.mean = 0;
      }
      m.weight = n_i;
      m.n = n_i;
      total_n += n_i;
    }
    for (int i = 0; i < components.size(); ++i) {
      GMMComponent m = components.get(i);
      double ssqd = 0.;
      double n_i = 0.;
      for (int j = 0; j < intensity.size(); ++j) {
        double d = (intensity.timeFromIndex(j) - m.mean);
        ssqd += resp[i][j] * d * d;
        n_i += resp[i][j];
      }

      m.variance = ssqd / n_i;
    }

    for (GMMComponent m : components) {
      m.weight = m.weight / total_n;
    }
  }

  private double[][] calcResponsibilities(List<GMMComponent> components, Contour intensity) {
    double[][] tau = new double[components.size()][intensity.size()];

    // TODO: limit the scope of components to check for each j.
    // This takes N*K where N is the number of points, and K is the number of components.
    // However, most responsibilities approach zero.
    // Since the components are linearly ordered we can stop checking the components once one falls below an epsilon.
    // Also, we can start checking components at the component where the previous frame was first above some epsilon
    for (int j = 0; j < intensity.size(); ++j) {
      double total = 0.;
      for (int i = 0; i < components.size(); ++i) {
        GMMComponent m = components.get(i);
        tau[i][j] = m.calcLikelihood(intensity.timeFromIndex(j)) * m.weight;
        total += tau[i][j];
      }
      // sum to Weight normalization
      for (int i = 0; i < components.size(); ++i) {
        tau[i][j] /= total;
        tau[i][j] *= intensity.get(j);
      }
    }
    return tau;
  }

  /**
   * Calculates the overall log likelihood of the GMM fitting the intensity contour
   *
   * @param components the GMM Components
   * @param c          the intensity contour
   * @return the loglikelihood of the fit.
   */
  public double calcLikelihood(List<GMMComponent> components, Contour c) {
    double ll = 0.;
    double w_sum = 0.;
    for (Pair<Double, Double> p : c) {
      double l = 0.;

      for (GMMComponent m : components) {
        l += m.calcLikelihood(p.first) * m.weight;
      }
      w_sum += p.second;
      ll += p.second * Math.log(l);
    }

    return ll / w_sum;
  }

  /**
   * Initializes mixture components.
   * <p/>
   * One per 100ms, with a 100ms^2 variance.
   *
   * @param duration the duration of the wave file
   * @return initialized components
   */
  private List<GMMComponent> initializeComponents(double duration) {
    List<GMMComponent> components = new ArrayList<GMMComponent>();
    double step = 0.1;
    double ngauss = Math.floor(duration / step);
    for (double t = step; t < duration; t += step) {
      components.add(new GMMComponent(t, step / 2, 1 / ngauss));
    }
    return components;
  }
}
