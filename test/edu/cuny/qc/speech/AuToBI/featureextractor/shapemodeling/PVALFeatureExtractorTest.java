package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import edu.cuny.qc.speech.AuToBI.featureextractor.GParam;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/14/12 Time: 5:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class PVALFeatureExtractorTest {
  private PVALFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new PVALFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1., 2.});
    GParam lowgp = new GParam(1.0, 1.0);
    GParam highgp = new GParam(10.0, 1.0);
    CurveShape peak = new CurveShape(8, true);
    CurveShape valley = new CurveShape(1, false);
    r.setAttribute("contour", c);
    r.setAttribute("lowGP[contour]", lowgp);
    r.setAttribute("highGP[contour]", highgp);
    r.setAttribute("peakCurve[contour]", peak);
    r.setAttribute("peakLL[contour]", 0.6);
    r.setAttribute("valleyCurve[contour]", valley);
    r.setAttribute("valleyLL[contour]", 0.4);

    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(r.hasAttribute("PVAmp[contour]"));
      assertTrue(r.hasAttribute("PVLocation[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureWorks() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1., 2.});
    GParam lowgp = new GParam(1.0, 1.0);
    GParam highgp = new GParam(10.0, 1.0);
    CurveShape peak = new CurveShape(8, true);
    CurveShape valley = new CurveShape(1, false);
    r.setAttribute("contour", c);
    r.setAttribute("lowGP[contour]", lowgp);
    r.setAttribute("highGP[contour]", highgp);
    r.setAttribute("peakCurve[contour]", peak);
    r.setAttribute("peakLL[contour]", 0.6);
    r.setAttribute("valleyCurve[contour]", valley);
    r.setAttribute("valleyLL[contour]", 0.4);

    regions.add(r);
    try {
      fe.extractFeatures(regions);

      assertEquals(0.8, (Double) r.getAttribute("PVLocation[contour]"), 0.0001);
      assertEquals(11.0, (Double) r.getAttribute("PVAmp[contour]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
