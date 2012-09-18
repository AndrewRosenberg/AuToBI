package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

/**
* Created with IntelliJ IDEA. User: andrew Date: 7/16/12 Time: 11:08 AM To change this template use File | Settings |
* File Templates.
*/
public class CurveShape {
  double[] smoothed_curve;
  int peak;
  boolean isPeak;
  double rmse;

  public CurveShape(int p, boolean isPeak) {
    this.peak = p;
    this.isPeak = isPeak;
  }
}
