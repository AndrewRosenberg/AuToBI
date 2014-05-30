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
public class VoicingRatioFeatureExtractorTest {
  private VoicingRatioFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new VoicingRatioFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    c.setEmpty(3);
    c.setEmpty(4);
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("voicingRatio[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureWorks() {
    Region r = new Region(0, 0.9);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    c.setEmpty(3);
    c.setEmpty(4);
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      assertEquals(0.8, (Double) r.getAttribute("voicingRatio[contour]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureWorksOnSmallRegions() {
    Region r = new Region(0.09, 0.51);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    c.setEmpty(3);
    c.setEmpty(4);
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      assertEquals(0.6, (Double) r.getAttribute("voicingRatio[contour]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
