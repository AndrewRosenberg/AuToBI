package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Region;

import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/3/12 Time: 10:45 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassBasedWeightFunction extends WeightFunction {
  private String attribute;
  private Map<String,Double> fn;

  public ClassBasedWeightFunction(String attribute, Map<String, Double> weight_fn) {
    this.attribute = attribute;
    this.fn = weight_fn;
  }

  public double weight(Region r) {
    if (r.hasAttribute(attribute) && fn.containsKey(r.getAttribute(attribute).toString())) {
      return fn.get(r.getAttribute(attribute).toString());
    }
    return 0;
  }
}
