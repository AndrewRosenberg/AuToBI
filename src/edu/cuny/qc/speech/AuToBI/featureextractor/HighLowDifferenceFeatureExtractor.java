package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class HighLowDifferenceFeatureExtractor extends FeatureExtractor {
  private String feature; // the name of the feature name

  public HighLowDifferenceFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add(feature + "__highGP");
    this.required_features.add(feature + "__lowGP");

    this.extracted_features.add(feature + "__highLowDiff");
  }


  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      GParam lowgp = (GParam) r.getAttribute(feature + "__lowGP");
      GParam highgp = (GParam) r.getAttribute(feature + "__highGP");
      r.setAttribute(feature + "__highLowDiff", highgp.mean - lowgp.mean);
    }
  }
}
