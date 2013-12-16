package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.GParam;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Calculates Peak/Valley Amplitude and location features.
 */
@SuppressWarnings("unchecked")
public class PVALFeatureExtractor extends FeatureExtractor {

  private String feature;   // name of the feature

  public PVALFeatureExtractor(String feature) {
    this.feature = feature;

    this.required_features.add(feature);
    this.required_features.add(feature + "__lowGP");
    this.required_features.add(feature + "__highGP");
    this.required_features.add(feature + "__peakCurve");
    this.required_features.add(feature + "__peakCurveLikelihood");
    this.required_features.add(feature + "__valleyCurve");
    this.required_features.add(feature + "__valleyCurveLikelihood");

    this.extracted_features.add(feature + "__PVAmp");
    this.extracted_features.add(feature + "__PVLocation");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature + "__peakCurveLikelihood") && r.hasAttribute(feature + "__valleyCurveLikelihood")) {
        double p_peak = (Double) r.getAttribute(feature + "__peakCurveLikelihood");
        double p_valley = (Double) r.getAttribute(feature + "__valleyCurveLikelihood");
        CurveShape curve;
        if (p_peak >= p_valley) {
          curve = (CurveShape) r.getAttribute(feature + "__peakCurve");
        } else {
          curve = (CurveShape) r.getAttribute(feature + "__valleyCurve");
        }

        Contour c;
        try {
          c = ContourUtils.getSubContour((Contour) r.getAttribute(feature), r.getStart(), r.getEnd());
        } catch (AuToBIException e) {
          throw new FeatureExtractorException(e.getMessage());
        }
        r.setAttribute(feature + "__PVLocation", 1 - (r.getEnd() - c.timeFromIndex(curve.peak)) / r.getDuration());
        GParam gp;
        if (p_peak >= p_valley) {
          gp = (GParam) r.getAttribute(feature + "__lowGP");
        } else {
          gp = (GParam) r.getAttribute(feature + "__highGP");
        }
        r.setAttribute(feature + "__PVAmp", Math.abs(c.get(curve.peak) - gp.mean));
      }
    }
  }
}
