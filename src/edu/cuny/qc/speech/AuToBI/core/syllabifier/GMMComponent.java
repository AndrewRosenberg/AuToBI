package edu.cuny.qc.speech.AuToBI.core.syllabifier;

/**
 * A simple class to hold mean and variance information for Gaussian Mixture Model components.
 */
class GMMComponent {
  double mean = 0;
  double variance = 0;
  boolean isSilence;
  double weight;  // mixture coefficient
  double n; // total (weighted) responsibility assigned to this component

  public GMMComponent(double mean, double variance) {
    this.mean = mean;
    this.variance = variance;
    this.isSilence = false;
    this.weight = 1.;
    this.n = 0.;
  }

  public GMMComponent(double mean, double variance, double weight) {
    this.mean = mean;
    this.variance = variance;
    this.isSilence = false;
    this.weight = weight;
    this.n = 0.;
  }

  /**
   * Calculates the gaussian likelihood of the value
   *
   * @param value the value
   * @return the gaussian likelihood of the value.
   */
  public Double calcLikelihood(Double value) {
    double stdev = Math.sqrt(variance);
    double pdf = 1 / (stdev * Math.sqrt(2 * Math.PI));
    pdf *= Math.pow(Math.E, (-(value - mean) * (value - mean)) / (2 * stdev * stdev));
    return pdf;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }
}