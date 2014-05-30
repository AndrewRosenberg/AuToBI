package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/14/12 Time: 5:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class CurveShapeLikelihoodFeatureExtractorTest {
  private CurveShapeLikelihoodFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new CurveShapeLikelihoodFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    CurveShape rising = new CurveShape(-1, true);
    rising.rmse = 10.0;
    CurveShape falling = new CurveShape(-1, true);
    falling.rmse = 5.0;
    CurveShape peak = new CurveShape(-1, true);
    peak.rmse = 0.0;
    CurveShape valley = new CurveShape(-1, true);
    valley.rmse = 6.0;
    r.setAttribute("risingCurve[contour]", rising);
    r.setAttribute("fallingCurve[contour]", falling);
    r.setAttribute("peakCurve[contour]", peak);
    r.setAttribute("valleyCurve[contour]", valley);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(regions.get(0).hasAttribute("risingLL[contour]"));
      assertTrue(regions.get(0).hasAttribute("fallingLL[contour]"));
      assertTrue(regions.get(0).hasAttribute("peakLL[contour]"));
      assertTrue(regions.get(0).hasAttribute("valleyLL[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturesWorks() {
    Region r = new Region(0, 1);
    CurveShape rising = new CurveShape(-1, true);
    rising.rmse = 3.0;
    CurveShape falling = new CurveShape(-1, true);
    falling.rmse = 1.0;
    CurveShape peak = new CurveShape(-1, true);
    peak.rmse = 0.0;
    CurveShape valley = new CurveShape(-1, true);
    valley.rmse = 2.0;
    r.setAttribute("risingCurve[contour]", rising);
    r.setAttribute("fallingCurve[contour]", falling);
    r.setAttribute("peakCurve[contour]", peak);
    r.setAttribute("valleyCurve[contour]", valley);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertEquals(0.0, (Double) regions.get(0).getAttribute("risingLL[contour]"), 0.0001);
      assertEquals(0.3333, (Double) regions.get(0).getAttribute("fallingLL[contour]"), 0.0001);
      assertEquals(0.5, (Double) regions.get(0).getAttribute("peakLL[contour]"), 0.0001);
      assertEquals(0.1666, (Double) regions.get(0).getAttribute("valleyLL[contour]"), 0.0001);
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

}
