package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Pair;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Calculates the RMSE difference between two contours
 */
@SuppressWarnings("unchecked")
public class ContourDifferenceFeatureExtractor extends FeatureExtractor {
  private String f1;
  private String f2;

  public ContourDifferenceFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add(f1);
    this.required_features.add(f2);
    this.extracted_features.add(f1 + "_" + f2 + "__rmse");
    this.extracted_features.add(f1 + "_" + f2 + "__meanError");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(f1) && r.hasAttribute(f2)) {
        r.setAttribute(f1 + "_" + f2 + "__rmse",
            contourRMSE((Contour) r.getAttribute(f1), (Contour) r.getAttribute(f2)));
        r.setAttribute(f1 + "_" + f2 + "__meanError", contourError((Contour) r.getAttribute(f1),
            (Contour) r.getAttribute(f2)));
      }
    }
  }

  private Double contourError(Contour c1, Contour c2) {
    double error = 0.0;
    for (Pair<Double, Double> p : c1) {
      error += (p.second - c2.get(p.first));
    }
    error /= c1.contentSize();
    return error;
  }

  private Double contourRMSE(Contour c1, Contour c2) {
    double rmse = 0.0;
    for (Pair<Double, Double> p : c1) {
      rmse += (p.second - c2.get(p.first)) * (p.second - c2.get(p.first));
    }
    rmse /= c1.contentSize();
    rmse = Math.sqrt(rmse);
    return rmse;
  }
}
