package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.Word;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for ContourFeatureExtractor
 *
 * @see ContourFeatureExtractor
 */
public class ContourFeatureExtractorTest {

  @Test
  public void testExtractFeaturesWorksWithNullFeature() {
    try {
      List<Region> regions = new ArrayList<Region>();
      regions.add(new Word(0, 1, "test"));

      ContourFeatureExtractor cfe = new ContourFeatureExtractor("test_attribute");
      cfe.extractFeatures(regions);
    } catch (NullPointerException e) {
      fail();
    } catch (FeatureExtractorException e) {
      assertTrue(true);
    }
  }
}
