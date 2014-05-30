package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SNPAssignmentFeatureExtractor.
 *
 * @see SNPAssignmentFeatureExtractor
 */
public class SNPAssignmentFeatureExtractorTest {

  @Test
  public void assignsDefaultFeatureOnNullSpeakerID() {
    try {
      String test_file = System.getenv("AUTOBI_TEST_DIR") + "/h1.spkrnorm";
      ArrayList<String> files = new ArrayList<String>();
      files.add(test_file);
      SNPAssignmentFeatureExtractor fe = new SNPAssignmentFeatureExtractor(null, files);

      ArrayList<Region> regions = new ArrayList<Region>();
      regions.add(new Region(0.0, 0.1));
      regions.add(new Region(0.1, 0.2));
      regions.add(new Region(0.2, 0.3));

      fe.extractFeatures(regions);
      SpeakerNormalizationParameter snp =
          (SpeakerNormalizationParameter) regions.get(0).getAttribute("spkrNormParamsFromFile");
      assertTrue(snp.equals(regions.get(1).getAttribute("spkrNormParamsFromFile")));
      assertTrue(snp.equals(regions.get(2).getAttribute("spkrNormParamsFromFile")));
    } catch (AuToBIException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void assignsFeatures() {
    try {
      ArrayList<String> files = new ArrayList<String>();
      files.add(System.getenv("AUTOBI_TEST_DIR") + "/h1.spkrnorm");
      files.add(System.getenv("AUTOBI_TEST_DIR") + "/h2.spkrnorm");
      SNPAssignmentFeatureExtractor fe = new SNPAssignmentFeatureExtractor("speaker_id", files);

      ArrayList<Region> regions = new ArrayList<Region>();
      regions.add(new Region(0.0, 0.1));
      regions.add(new Region(0.1, 0.2));
      regions.add(new Region(0.2, 0.3));

      regions.get(0).setAttribute("speaker_id", "h1");
      regions.get(1).setAttribute("speaker_id", "h1");
      regions.get(2).setAttribute("speaker_id", "h2");

      fe.extractFeatures(regions);
      SpeakerNormalizationParameter snp =
          (SpeakerNormalizationParameter) regions.get(0).getAttribute("spkrNormParamsFromFile");
      assertTrue(snp.equals(regions.get(1).getAttribute("spkrNormParamsFromFile")));
      assertTrue(!snp.equals(regions.get(2).getAttribute("spkrNormParamsFromFile")));
    } catch (AuToBIException e) {
      fail();
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void throwsAnExceptionWithMultipleFilesAndNullSpeakerID() {
    try {
      ArrayList<String> files = new ArrayList<String>();
      files.add("/Users/andrew/code/AuToBI/release/test_data/h1.spkrnorm");
      files.add("/Users/andrew/code/AuToBI/release/test_data/h2.spkrnorm");
      SNPAssignmentFeatureExtractor fe = new SNPAssignmentFeatureExtractor(null, files);
      fail();
    } catch (AuToBIException e) {
      assertTrue(true);
    }
  }
}
