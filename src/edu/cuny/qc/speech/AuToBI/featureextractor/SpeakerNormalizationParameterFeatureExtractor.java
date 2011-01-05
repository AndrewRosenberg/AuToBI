package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.HashMap;
import java.util.List;

/**
 * Generates speaker normalization parameters based on a speaker id feature.
 */
public class SpeakerNormalizationParameterFeatureExtractor extends FeatureExtractor {
  private String speaker_id_feature;
  private String destination_feature;

  public SpeakerNormalizationParameterFeatureExtractor(String speaker_id_feature, String destination_feature) {
    this.speaker_id_feature = speaker_id_feature;
    this.destination_feature = destination_feature;

    required_features.add("f0");
    required_features.add("I");
    extracted_features.add(destination_feature);
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<String, SpeakerNormalizationParameter> params = new HashMap<String, SpeakerNormalizationParameter>();

    for (Region r : (List<Region>) regions) {
      SpeakerNormalizationParameter norm_params;
      if (!params.containsKey(r.getAttribute(speaker_id_feature))) {
        params.put((String) r.getAttribute(speaker_id_feature),
            new SpeakerNormalizationParameter(r.getAttribute(speaker_id_feature).toString()));
      }
      norm_params = params.get(r.getAttribute(speaker_id_feature));
      if (r.hasAttribute("f0")) {
        norm_params.insertPitch((Contour) r.getAttribute("f0"));
      }
      if (r.hasAttribute("I")) {
        norm_params.insertIntensity((Contour) r.getAttribute("I"));
      }
    }

    for (Region r : (List<Region>) regions) {
      r.setAttribute(destination_feature, params.get(r.getAttribute(speaker_id_feature)));
    }
  }
}
