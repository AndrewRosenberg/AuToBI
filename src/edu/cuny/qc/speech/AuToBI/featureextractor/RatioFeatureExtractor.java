package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/24/12 Time: 11:13 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class RatioFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "ratio";
  private String f1;
  private String f2;

  public RatioFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add(f1);
    this.required_features.add(f2);
    this.extracted_features.add(moniker + "[" + f1 + "," + f2 + "]");
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(f1) && r.hasAttribute(f2)) {
        r.setAttribute(moniker + "[" + f1 + "," + f2, ((Double) r.getAttribute(f1)) / ((Double) r.getAttribute(f2)));
      }
    }
  }
}
