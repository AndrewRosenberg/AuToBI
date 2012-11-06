package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Distribution;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import edu.cuny.qc.speech.AuToBI.util.AuToBIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/3/12 Time: 10:46 AM To change this template use File | Settings | File
 * Templates.
 */
public class ClassBasedWeightFunctionTrainer {

  private String class_attribute;
  private WeightType type;

  public enum WeightType {LINEAR, ENTROPY}

  public ClassBasedWeightFunctionTrainer(String class_attribute, WeightType type) {
    this.class_attribute = class_attribute;
    this.type = type;
  }

  public ClassBasedWeightFunction trainWeightFunction(List<Word> regions) {

    // Calculate the distribution of class values within the regions
    Distribution d = new Distribution();
    for (Region r : regions) {
      if (r.hasAttribute(class_attribute)) {
        d.add(r.getAttribute(class_attribute).toString());
      }
    }
    try {
      d.normalize();
    } catch (AuToBIException e) {
      AuToBIUtils.warn("Error in class weight calculation: " + e.getMessage());
    }

    // Calculate the class weight
    Map<String, Double> weight_fn = new HashMap<String, Double>();
    for (String s : d.keySet()) {
      double p = d.get(s);
      if (type == ClassBasedWeightFunctionTrainer.WeightType.LINEAR) {
        weight_fn.put(s, 1 / p);
      } else if (type == ClassBasedWeightFunctionTrainer.WeightType.ENTROPY) {
        weight_fn.put(s, -p * Math.log(p));
      }
    }

    return new ClassBasedWeightFunction(class_attribute, weight_fn);
  }
}
