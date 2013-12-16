package edu.cuny.qc.speech.AuToBI.core;

/**
 * PitchContour operates like a standard Contour object for the purposes of storing f0 values, but includes another
 * aligned stream of information to access the voicing confidence from the pitch tracking algorithm.
 *
 * @see edu.cuny.qc.speech.AuToBI.PitchExtractor
 */
public class PitchContour extends Contour {
  protected double[] strengths;  //

  /**
   * Constructs a new pitch contour at starting time x0, frame delta, dx containing, values
   *
   * @param x0     the starting time
   * @param dx     the delta time
   * @param values the initial values
   */
  public PitchContour(double x0, double dx, double[] values) {
    super(x0, dx, values);
    this.strengths = new double[values.length];
  }

  /**
   * Constructs a new pitch contour at starting time x0, frame delta, dx and n points.
   *
   * @param x0 the starting time
   * @param dx the delta time
   * @param n  the number of frames
   */
  public PitchContour(double x0, double dx, int n) {
    super(x0, dx, n);
    this.strengths = new double[n];
  }

  /**
   * Sets the strengths of a frame, i.
   *
   * @param i the frame
   * @param s the strengths value.
   */
  public void setStrength(int i, double s) {
    this.strengths[i] = s;
  }

  /**
   * Sets the strength for a given time.
   * <p/>
   * This will overwrite the value currently associated with that time.
   * <p/>
   * Be careful, two distinct times can be mapped to the same index in the contour.  This can lead to accidentally
   * overwriting a contour value if the time steps are not consistent.
   *
   * @param time  the time
   * @param value the value
   */
  public void setStrength(double time, double value) {
    setStrength(indexFromTime(time), value);
  }

  /**
   * Gets the strengths associated with a time.
   *
   * @param time the time
   * @return the value stored at that time.
   */
  public double getStrength(double time) {
    return getStrength(indexFromTime(time));
  }

  /**
   * Gets the value associated with an index.
   * <p/>
   * Returns NaN if the index has no associated value
   *
   * @param index the index
   * @return the value stored at the index
   */
  public double getStrength(int index) {
    if (index < 0 || index >= n) {
      return Double.NaN;
    }
    return strengths[index];
  }
}
