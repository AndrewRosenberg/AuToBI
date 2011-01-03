package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import edu.cuny.qc.speech.AuToBI.core.TimeValuePair;

import java.util.HashMap;
import java.util.List;

/**
 * Constructs SpeakerNormalizationParameters based on values stored in all input regions.
 * <p/>
 * This is an alternative approach to storing speaker normalization parameters calculated from external data.
 */
public class AllRegionsSNPFeatureExtractor extends FeatureExtractor {
  private String dest_feature; // The feature to store the SNP object on
  private String speaker_id_feature; // The feature containing speaker identity information

  /**
   * Constructs a new AllRegionsSNPFeatureExtractor.
   *
   * @param dest_feature       The feature to store the SpeakerNormalizationParameter objects
   * @param speaker_id_feature The feature containing the speaker identifier.
   */
  public AllRegionsSNPFeatureExtractor(String dest_feature, String speaker_id_feature) {
    this.dest_feature = dest_feature;
    this.speaker_id_feature = speaker_id_feature;

    this.required_features.add("f0");
    this.required_features.add("I");
    this.required_features.add(speaker_id_feature);
    this.extracted_features.add(dest_feature);
  }


  /**
   * Constructs Speaker Normalization Parameters for each speaker represented by the regions and stores the resulting
   * object in the regions attributes.
   *
   * @param regions the regions to extract features from
   * @throws FeatureExtractorException this should never happen.
   */
  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    HashMap<String, SpeakerNormalizationParameter> snp_map = new HashMap<String, SpeakerNormalizationParameter>();

    for (Region r : (List<Region>) regions) {
      String speaker_id = r.getAttribute(speaker_id_feature).toString();
      if (!snp_map.containsKey(speaker_id)) {
        snp_map.put(speaker_id, new SpeakerNormalizationParameter(speaker_id));
      }
      SpeakerNormalizationParameter snp = snp_map.get(speaker_id);

      if (r.hasAttribute("f0")) {
        snp.insertPitch((List<TimeValuePair>) r.getAttribute("f0"));
      }
      if (r.hasAttribute("I")) {
        snp.insertIntensity((List<TimeValuePair>) r.getAttribute("I"));
      }
    }

    for (Region r : (List<Region>) regions) {
      String speaker_id = r.getAttribute(speaker_id_feature).toString();
      if (!snp_map.containsKey(speaker_id)) {
        snp_map.put(speaker_id, new SpeakerNormalizationParameter(speaker_id));
      }
      SpeakerNormalizationParameter snp = snp_map.get(speaker_id);

      r.setAttribute(dest_feature, snp);
    }
  }
}
