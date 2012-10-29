package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class CurveShapeLikelihoodFeatureExtractor extends FeatureExtractor {

  private String feature;

  public CurveShapeLikelihoodFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature + "__risingCurve");
    this.required_features.add(feature + "__fallingCurve");
    this.required_features.add(feature + "__peakCurve");
    this.required_features.add(feature + "__valleyCurve");

    this.extracted_features.add(feature + "__risingCurveLikelihood");
    this.extracted_features.add(feature + "__fallingCurveLikelihood");
    this.extracted_features.add(feature + "__peakCurveLikelihood");
    this.extracted_features.add(feature + "__valleyCurveLikelihood");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature + "__risingCurve") && r.hasAttribute(feature + "__fallingCurve") &&
          r.hasAttribute(feature + "__peakCurve") && r.hasAttribute(feature + "__valleyCurve")) {
        CurveShape rising =
            (CurveShape) r.getAttribute(feature + "__risingCurve");
        CurveShape falling =
            (CurveShape) r.getAttribute(feature + "__fallingCurve");
        CurveShape peak =
            (CurveShape) r.getAttribute(feature + "__peakCurve");
        CurveShape valley =
            (CurveShape) r.getAttribute(feature + "__valleyCurve");

        double maxrmse = Math.max(rising.rmse, Math.max(falling.rmse, Math.max(peak.rmse, valley.rmse)));
        Distribution d = new Distribution();
        d.add("rising", maxrmse - rising.rmse);
        d.add("falling", maxrmse - falling.rmse);
        d.add("peak", maxrmse - peak.rmse);
        d.add("valley", maxrmse - valley.rmse);
        try {
          d.normalize();
        } catch (AuToBIException e) {
          // This exception is thrown when the distribution is even.  Since this happens so often, we'll do nothing here.
          // TODO: consider a different calculation of the likelihood
        }

        r.setAttribute(feature + "__risingCurveLikelihood", d.get("rising"));
        r.setAttribute(feature + "__fallingCurveLikelihood", d.get("falling"));
        r.setAttribute(feature + "__peakCurveLikelihood", d.get("peak"));
        r.setAttribute(feature + "__valleyCurveLikelihood", d.get("valley"));
      }
    }
  }
}
