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
    r.setAttribute("contour__risingCurve", rising);
    r.setAttribute("contour__fallingCurve", falling);
    r.setAttribute("contour__peakCurve", peak);
    r.setAttribute("contour__valleyCurve", valley);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(regions.get(0).hasAttribute("contour__risingCurveLikelihood"));
      assertTrue(regions.get(0).hasAttribute("contour__fallingCurveLikelihood"));
      assertTrue(regions.get(0).hasAttribute("contour__peakCurveLikelihood"));
      assertTrue(regions.get(0).hasAttribute("contour__valleyCurveLikelihood"));
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
      r.setAttribute("contour__risingCurve", rising);
      r.setAttribute("contour__fallingCurve", falling);
      r.setAttribute("contour__peakCurve", peak);
      r.setAttribute("contour__valleyCurve", valley);
      regions.add(r);
      try {
        fe.extractFeatures(regions);
        assertEquals(0.0, (Double) regions.get(0).getAttribute("contour__risingCurveLikelihood"), 0.0001);
        assertEquals(0.3333, (Double) regions.get(0).getAttribute("contour__fallingCurveLikelihood"), 0.0001);
        assertEquals(0.5, (Double) regions.get(0).getAttribute("contour__peakCurveLikelihood"), 0.0001);
        assertEquals(0.1666, (Double) regions.get(0).getAttribute("contour__valleyCurveLikelihood"), 0.0001);
      } catch (FeatureExtractorException e) {
        fail();
      }
    }

}
