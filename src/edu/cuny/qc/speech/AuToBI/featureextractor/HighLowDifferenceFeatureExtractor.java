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
  public static final String moniker = "highLowDiff";
  private String feature; // the name of the feature name

  public HighLowDifferenceFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add("highGP[" + feature + "]");
    this.required_features.add("lowGP[" + feature + "]");

    this.extracted_features.add("highLowDiff[" + feature + "]");
  }


  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute("lowGP[" + feature + "]") && r.hasAttribute("highGP[" + feature + "]")) {
        GParam lowgp = (GParam) r.getAttribute("lowGP[" + feature + "]");
        GParam highgp = (GParam) r.getAttribute("highGP[" + feature + "]");
        r.setAttribute("highLowDiff[" + feature + "]", highgp.mean - lowgp.mean);
      }
    }
  }
}
