package edu.cuny.qc.speech.AuToBI;

import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import org.junit.Test;

import java.net.Authenticator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for AuToBI.
 */
public class AuToBITest {

  @Test
  public void testInit() {
    // Init sets up the log4j logger, and passes command line to AuToBIParameters for parsing.
    // See AuToBIParametersTest for testing of parameter parsing.
    assertTrue(true);
  }

  @Test
  public void testInitializeReferenceCounting() {

    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getRequiredFeatures().add("feature2");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe2.getExtractedFeatures().add("feature2");

    AuToBI autobi = new AuToBI();

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testInitializeReferenceCountingTwoFeaturesExtractedByOneExtractor() {

    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.getRequiredFeatures().add("feature2");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getExtractedFeatures().add("feature2");
    fe.getRequiredFeatures().add("feature3");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe2.getExtractedFeatures().add("feature3");

    AuToBI autobi = new AuToBI();

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
      assertEquals(2, autobi.getReferenceCount("feature3"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testInitializeReferenceCountingWorksWithNullFeatureExtractors() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    fs.setClassAttribute("feature1");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getRequiredFeatures().add("feature2");

    AuToBI autobi = new AuToBI();

    autobi.registerFeatureExtractor(fe);
    autobi.registerNullFeatureExtractor("feature2");

    try {
      autobi.initializeReferenceCounting(fs);

      assertEquals(1, autobi.getReferenceCount("feature1"));
      assertEquals(1, autobi.getReferenceCount("feature2"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testFeatureGarbageCollectionDeletesZeroCountFeatures() {
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0.0, 0.1, "test_point");
    fs.insertDataPoint(w);

    AuToBI autobi = new AuToBI();
    try {
      autobi.initializeReferenceCounting(fs);
    } catch (AuToBIException e) {
      fail();
    }
    autobi.incrementReferenceCount("test_feature");
    w.setAttribute("test_feature", true);
    autobi.decrementReferenceCount("test_feature");

    autobi.featureGarbageCollection(fs);
    assertTrue(!w.hasAttribute("test_feature"));
  }

  @Test
  public void testFeatureExtractionRemovesUnusedFeatures() {
    // Set up Test Feature Extraction Configuration
    FeatureSet fs = new FeatureSet();
    Word w = new Word(0.0, 0.1, "test_point");
    fs.insertDataPoint(w);

    fs.setClassAttribute("feature1");
    fs.getRequiredFeatures().add("feature2");
    fs.constructFeatures();

    FeatureExtractor fe = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("feature1", true);
          r.setAttribute("feature2", true);
        }
      }
    };
    fe.getExtractedFeatures().add("feature1");
    fe.getExtractedFeatures().add("feature2");
    fe.getRequiredFeatures().add("feature3");

    FeatureExtractor fe2 = new FeatureExtractor() {
      @Override
      public void extractFeatures(List regions) throws FeatureExtractorException {
        for (Region r : (List<Region>) regions) {
          r.setAttribute("feature3", true);
        }
      }
    };
    fe2.getExtractedFeatures().add("feature3");

    AuToBI autobi = new AuToBI();

    autobi.registerFeatureExtractor(fe);
    autobi.registerFeatureExtractor(fe2);

    try {
      autobi.initializeReferenceCounting(fs);

      autobi.extractFeatures(fs);
      assertEquals(2, w.getAttributes().size());
    } catch (AuToBIException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }


}
