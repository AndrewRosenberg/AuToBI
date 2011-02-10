package edu.cuny.qc.speech.AuToBI.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test class for FeatureSet.
 *
 * @see FeatureSet
 */
public class FeatureSetTest {

  @Test
  public void testFeatureConstructionWorksWithNullClassAttribute() {
    FeatureSet fs = new FeatureSet();
    fs.getDataPoints().add(new Word(0,1, "one"));
    fs.getDataPoints().add(new Word(1,2, "two"));
    fs.setClassAttribute(null);

    fs.constructFeatures();
  }

}
