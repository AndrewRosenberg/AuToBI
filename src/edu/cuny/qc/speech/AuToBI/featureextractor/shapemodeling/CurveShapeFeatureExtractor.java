package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class CurveShapeFeatureExtractor extends FeatureExtractor {

  private String feature;

  public CurveShapeFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature);
    this.extracted_features.add(feature + "__risingCurve");
    this.extracted_features.add(feature + "__fallingCurve");
    this.extracted_features.add(feature + "__peakCurve");
    this.extracted_features.add(feature + "__valleyCurve");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature)) {
        Contour super_c = (Contour) r.getAttribute(feature);
        Contour c;
        try {
          c = ContourUtils.getSubContour(super_c, r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }

        CurveShape falling = smooth(-1, true, c);
        CurveShape rising = smooth(c.size(), true, c);
        CurveShape best_peak = null;
        double min_peak_rmse = Double.MAX_VALUE;

        // Sample the curve at at most 20 points to calculate peak and valley likelihoods.
        // On long regions calculating at every point gets *very* slow.
        int step = Math.max(1, c.size() / 20);
        for (int i = 1; i < c.size() - 1; i += step) {
          CurveShape peak = smooth(i, true, c);
          if (peak.rmse < min_peak_rmse) {
            best_peak = peak;
            min_peak_rmse = peak.rmse;
          }
        }
        CurveShape best_valley = null;
        double min_valley_rmse = Double.MAX_VALUE;
        for (int i = 1; i < c.size() - 1; i += step) {
          CurveShape valley = smooth(i, false, c);
          if (valley.rmse < min_valley_rmse) {
            best_valley = valley;
            min_valley_rmse = valley.rmse;
          }
        }
        r.setAttribute(feature + "__risingCurve", rising);
        r.setAttribute(feature + "__fallingCurve", falling);
        r.setAttribute(feature + "__peakCurve", best_peak);
        r.setAttribute(feature + "__valleyCurve", best_valley);
      }
    }
  }

  private CurveShape smooth(int p, boolean isPeak, Contour c) {
    CurveShape curve = new CurveShape(p, isPeak);

    if (p > 0 && p < c.size() && c.isEmpty(p)) {
      curve.rmse = Double.MAX_VALUE;
      return curve;
    }
    // reflect values around the peak
    double[] v = new double[c.size()];
    for (int i = 0; i < c.size(); ++i) {
      if (!c.isEmpty(i)) {
        if (i >= p) {
          if (p < 0) {
            v[i] = -c.get(i);
          } else {
            v[i] = 2 * c.get(p) - c.get(i);
          }
        } else {
          v[i] = c.get(i);
        }
        if (!isPeak) {
          v[i] = -v[i];
        }
      } else {
        v[i] = Double.NaN;
      }
    }

    ArrayList<Block> pava_blocks = new ArrayList<Block>();
    // initialize blocks
    for (int i = 0; i < c.size(); ++i) {
      if (!Double.isNaN(v[i])) {
        pava_blocks.add(new Block(i, i, v[i]));
      }
    }

    // Merge blocks using PAVA
    boolean done = false;
    boolean increasing = true;
    int idx = 1;
    int start_merge = -1, end_merge = -1;
    while (!done) {
      while (idx < pava_blocks.size()) {
        // monotonically increasing.
        if (pava_blocks.get(idx - 1).x < pava_blocks.get(idx).x) {
          if (!increasing) {
            // merge
            break;
          }
        } else if (pava_blocks.get(idx - 1).x > pava_blocks.get(idx).x) {
          if (increasing) {
            start_merge = idx - 1;
          }
          end_merge = idx;
          increasing = false;
        }
        idx++;
      }
      done = true;
      if (start_merge != end_merge) {
        // merge blocks
        double value = 0.0;
        for (int i = start_merge; i <= end_merge; ++i) {
          value += pava_blocks.get(i).x * (pava_blocks.get(i).high_idx - pava_blocks.get(i).low_idx + 1);
        }
        value /= (pava_blocks.get(end_merge).high_idx - pava_blocks.get(start_merge).low_idx + 1);
        Block new_block = new Block(pava_blocks.get(start_merge).low_idx, pava_blocks.get(end_merge).high_idx, value);

        // remove the old blocks
        while (start_merge <= end_merge) {
          pava_blocks.remove(start_merge);
          end_merge--;
        }
        // replace with the new, merged block
        pava_blocks.add(start_merge, new_block);
        done = false;
        // Reset
        idx = 1;
        start_merge = -1;
        end_merge = -1;
        increasing = true;
      }
    }
    // Generate a smooth contour from block representation
    double[] smoothed = new double[c.size()];
    for (Block b : pava_blocks) {
      for (int i = b.low_idx; i <= b.high_idx; ++i) {
        if (!c.isEmpty(i)) {
          if (i >= p) {
            if (p < 0) {
              smoothed[i] = -b.x;
            } else {
              if (!isPeak) {
                smoothed[i] = 2 * -c.get(p) - b.x;
              } else {
                smoothed[i] = 2 * c.get(p) - b.x;
              }
            }
          } else {
            smoothed[i] = b.x;
          }
        }
        if (!isPeak) {
          smoothed[i] = -smoothed[i];
        }
      }
    }

    // Calculate RMSE
    double rmse = 0.0;
    for (int i = 0; i < c.size(); ++i) {
      if (!c.isEmpty(i)) {
        rmse += (c.get(i) - smoothed[i]) * (c.get(i) - smoothed[i]);
      }
    }
    rmse /= c.contentSize();
    rmse = Math.sqrt(rmse);
    curve.rmse = rmse;
    curve.smoothed_curve = smoothed;
    return curve;
  }

  private class Block {
    int low_idx;   // the lowest corresponding index
    int high_idx;  // the highest corresponding index
    double x;      // the current value of the block

    public Block(int low, int high, double x) {
      this.low_idx = low;
      this.high_idx = high;
      this.x = x;
    }
  }
}
