package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/14/12 Time: 5:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class HighLowComponentFeatureExtractorTest {
  private HighLowComponentFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new HighLowComponentFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(regions.get(0).hasAttribute("lowGP[contour]"));
      assertTrue(regions.get(0).hasAttribute("highGP[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureWorks() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      GParam low =
          (GParam) regions.get(0).getAttribute("lowGP[contour]");
      GParam high =
          (GParam) regions.get(0).getAttribute("highGP[contour]");
      assertEquals(1, low.mean, 0.0001);
      assertEquals(0.6324555320336759, low.stdev, 0.0001);
      assertEquals(10.6, high.mean, 0.0001);
      assertEquals(0.8, high.stdev, 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
