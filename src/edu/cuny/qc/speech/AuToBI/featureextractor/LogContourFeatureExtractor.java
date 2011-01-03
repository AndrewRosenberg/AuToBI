package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.TimeValuePair;
import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * LogContourFeatureExtractor constructs a new timevaluepair contour applying a log transformation to each point.
 */
public class LogContourFeatureExtractor extends FeatureExtractor {
  private String src;  // The source feature
  private String tgt;  // The target feature

  // Constructs a new Feature Extractor
  public LogContourFeatureExtractor(String source_feature, String target_feature) {
    super();

    this.src = source_feature;
    this.tgt = target_feature;
    this.required_features.add(source_feature);
    this.extracted_features.add(target_feature);
  }

  @Override
  /**
   * Constructs a new List<TimeValuePair> object containing log transformed values based on a source contour.
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {

      if (r.hasAttribute(src)) {
        List<TimeValuePair> src_contour = (List<TimeValuePair>) r.getAttribute(src);
        List<TimeValuePair> tgt_contour = new ArrayList<TimeValuePair>();
        for (TimeValuePair tvp : src_contour) {
          tgt_contour.add(new TimeValuePair(tvp.getTime(), Math.log(tvp.getValue())));
        }
        r.setAttribute(tgt, tgt_contour);
      }
    }
  }
}
