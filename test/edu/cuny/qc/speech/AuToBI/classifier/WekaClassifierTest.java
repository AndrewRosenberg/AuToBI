package edu.cuny.qc.speech.AuToBI.classifier;

import edu.cuny.qc.speech.AuToBI.AuToBI;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import org.junit.Test;
import weka.classifiers.trees.J48;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for WekaClassifier.
 *
 * @see WekaClassifier
 */
public class WekaClassifierTest {

  @Test
  public void testTrainConstructsFeaturesIfNotSet() {
    FeatureSet fs = new FeatureSet();
    fs.insertRequiredFeature("feature");
    fs.setClassAttribute("class");

    Word w = new Word(0.0, 0.0, "test");
    w.setAttribute("feature", 1.0);
    w.setAttribute("class", "ONE");

    Word w1 = new Word(0.0, 0.0, "test");
    w1.setAttribute("feature", 1.0);
    w1.setAttribute("class", "TWO");

    fs.insertDataPoint(w);
    fs.insertDataPoint(w1);

    WekaClassifier c = new WekaClassifier(new J48());
    try {
      c.train(fs);
      assertTrue(true);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
