package edu.cuny.qc.speech.AuToBI.featureextractor.shapemodeling;

import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.featureextractor.FeatureExtractorException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/14/12 Time: 5:16 PM To change this template use File | Settings |
 * File Templates.
 */
public class CurveShapeFeatureExtractorTest {
  private CurveShapeFeatureExtractor fe;
  private List<Region> regions;

  @Before
  public void setUp() {
    regions = new ArrayList<Region>();
    fe = new CurveShapeFeatureExtractor("contour");
  }

  @Test
  public void testExtractFeaturesExtractsFeatures() {
    Region r = new Region(0, 1);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);
      assertTrue(regions.get(0).hasAttribute("risingCurve[contour]"));
      assertTrue(regions.get(0).hasAttribute("fallingCurve[contour]"));
      assertTrue(regions.get(0).hasAttribute("peakCurve[contour]"));
      assertTrue(regions.get(0).hasAttribute("valleyCurve[contour]"));
    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureRisingContourWorks() {
    Region r = new Region(0, .999);
    Contour c = new Contour(0.0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      CurveShape rising = (CurveShape) regions.get(0).getAttribute("risingCurve[contour]");
      assertEquals(10, rising.peak);
      assertArrayEquals(new double[]{0., 1., 1., 2., 9., 9., 9., 9., 9., 9.}, rising.smoothed_curve, 0.001);

    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureFallingContourWorks() {
    Region r = new Region(0, .999);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      CurveShape falling =
          (CurveShape) regions.get(0).getAttribute("fallingCurve[contour]");
      assertEquals(-1, falling.peak);
      assertArrayEquals(new double[]{6.3333, 6.3333, 6.3333, 6.3333, 6.3333, 6.3333, 6.3333, 6.3333, 6.3333, 1.0},
          falling.smoothed_curve, 0.001);

    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeaturePeakContourWorks() {
    Region r = new Region(0, .999);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      CurveShape peak =
          (CurveShape) regions.get(0).getAttribute("peakCurve[contour]");
      assertEquals(8, peak.peak);
      assertArrayEquals(new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.},
          peak.smoothed_curve, 0.001);

    } catch (FeatureExtractorException e) {
      fail();
    }
  }

  @Test
  public void testExtractFeatureValleyContourWorks() {
    Region r = new Region(0, .999);
    Contour c = new Contour(0, 0.1, new double[]{0., 1., 1., 2., 10., 10, 10., 11., 12., 1.});
    r.setAttribute("contour", c);
    regions.add(r);
    try {
      fe.extractFeatures(regions);

      CurveShape valley =
          (CurveShape) regions.get(0).getAttribute("valleyCurve[contour]");
      assertEquals(1, valley.peak);
      assertArrayEquals(new double[]{0.666, 1.333, 1.333, 2., 9., 9., 9., 9., 9., 9.},
          valley.smoothed_curve, 0.001);

    } catch (FeatureExtractorException e) {
      fail();
    }
  }
}
