package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
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
        Contour src_contour = (Contour) r.getAttribute(src);
        Contour tgt_contour = new Contour(src_contour.getStart(), src_contour.getStep(), src_contour.size());
        for (int i = 0; i < src_contour.size(); ++i) {
          if (!src_contour.isEmpty(i)) {
            tgt_contour.set(i, Math.log(src_contour.get(i)));
          }
        }
        r.setAttribute(tgt, tgt_contour);
      }
    }
  }
}
