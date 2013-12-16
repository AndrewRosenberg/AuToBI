package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.List;

/**
 * Voiced to unvoiced ratio
 */
@SuppressWarnings("unchecked")
public class VoicingRatioFeatureExtractor extends FeatureExtractor {

  private String pitch_feature;  // the feature containing the pitch feature to determine voicing

  public VoicingRatioFeatureExtractor(String pitch_feature) {
    this.pitch_feature = pitch_feature;

    this.required_features.add(pitch_feature);
    this.extracted_features.add(pitch_feature + "__voicingRatio");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {

      Contour pitch;
      try {
        pitch = ContourUtils.getSubContour((Contour) r.getAttribute(pitch_feature), r.getStart(), r.getEnd());
      } catch (AuToBIException e) {
        throw new FeatureExtractorException(e.getMessage());
      }
      if (pitch != null) {
        r.setAttribute(pitch_feature + "__voicingRatio", pitch.contentSize() * 1.0 / pitch.size());
      } else {
        r.setAttribute(pitch_feature + "__voicingRatio", 0.0);
      }
    }
  }
}
