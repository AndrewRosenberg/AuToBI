package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/17/12 Time: 2:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class SurroundingContextFeatureExtractor extends FeatureExtractor {

  private String feature;  // feature to copy

  public SurroundingContextFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add(feature);
    this.extracted_features.add(feature + "__prev");
    this.extracted_features.add(feature + "__next");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (int i = 0; i < regions.size(); ++i) {
      Region r = (Region) regions.get(i);
      if (i != 0) {
        if (((Region) regions.get(i - 1)).hasAttribute(feature)) {
          r.setAttribute(feature + "__prev", ((Region) regions.get(i - 1)).getAttribute(feature));
        }
      }
      if (i != regions.size() - 1) {
        if (((Region) regions.get(i + 1)).hasAttribute(feature)) {
          r.setAttribute(feature + "__next", ((Region) regions.get(i + 1)).getAttribute(feature));
        }
      }
    }
  }
}
