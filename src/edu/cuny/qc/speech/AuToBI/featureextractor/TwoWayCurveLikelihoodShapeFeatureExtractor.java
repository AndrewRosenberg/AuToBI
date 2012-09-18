package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.FeatureExtractor;
import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/24/12 Time: 11:19 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class TwoWayCurveLikelihoodShapeFeatureExtractor extends FeatureExtractor {
  private String f1;
  private String f2;

  public TwoWayCurveLikelihoodShapeFeatureExtractor(String f1, String f2) {
    this.f1 = f1;
    this.f2 = f2;

    this.required_features.add(f1 + "__risingCurveLikelihood");
    this.required_features.add(f1 + "__fallingCurveLikelihood");
    this.required_features.add(f1 + "__peakCurveLikelihood");
    this.required_features.add(f1 + "__valleyCurveLikelihood");

    this.required_features.add(f2 + "__risingCurveLikelihood");
    this.required_features.add(f2 + "__fallingCurveLikelihood");
    this.required_features.add(f2 + "__peakCurveLikelihood");
    this.required_features.add(f2 + "__valleyCurveLikelihood");

    this.extracted_features.add(f1 + f2 + "__rrCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__rfCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__rpCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__rvCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__frCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__ffCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__fpCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__fvCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__prCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__pfCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__ppCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__pvCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__vrCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__vfCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__vpCurveLikelihood");
    this.extracted_features.add(f1 + f2 + "__vvCurveLikelihood");

  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    for (Region r : (List<Region>) regions) {
      for (String shape1 : new String[]{"rising", "falling", "peak", "valley"}) {
        for (String shape2 : new String[]{"rising", "falling", "peak", "valley"}) {
          if (r.hasAttribute(f1 + "__" + shape1 + "CurveLikelihood") &&
              r.hasAttribute(f2 + "__" + shape2 + "CurveLikelihood")) {
            r.setAttribute(f1 + f2 + "__" + shape1.charAt(0) + shape2.charAt(0) + "CurveLikelihood",
                ((Double) r.getAttribute(f1 + "__" + shape1 + "CurveLikelihood")) *
                    ((Double) r.getAttribute(f2 + "__" + shape2 + "CurveLikelihood")));
          }
        }
      }
    }
  }
}
