package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/14/12 Time: 5:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class AUContourFeatureExtractorTest {
  private AUContourFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new AUContourFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(regions.get(0).hasAttribute("area[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureRisingContourWorks() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      assertEquals(58.0, (Double) regions.get(0).getAttribute("area[contour]"), 0.0001);

    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
