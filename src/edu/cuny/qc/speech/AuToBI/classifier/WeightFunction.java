package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.core.Region;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 3/3/12 Time: 10:44 AM To change this template use File | Settings | File
 * Templates.
 */
public abstract class WeightFunction {
  /**
   * Calculates the weight of a data point
   * @param r a data point to weight
   * @return the weight of the data point.
   */
  public abstract double weight(Region r);
}
