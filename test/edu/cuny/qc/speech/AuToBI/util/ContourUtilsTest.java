package edu.cuny.qc.speech.AuToBI.util;

import edu.cuny.qc.speech.AuToBI.core.AuToBIException;
import edu.cuny.qc.speech.AuToBI.core.Contour;
import edu.cuny.qc.speech.AuToBI.core.Region;
import edu.cuny.qc.speech.AuToBI.core.SpeakerNormalizationParameter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA. User: andrew Date: 1/4/11 Time: 10:30 AM To change this template use File | Settings | File
 * Templates.
 */
public class ContourUtilsTest {


  @Test
  public void testGetSubContourSize() {
    Contour c = new Contour(0.1, 0.01, 10);

    try {
      Contour sub_c = ContourUtils.getSubContour(c, 0.12, 0.14);

      assertEquals(3, sub_c.size());
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSubContourBounds() {
    Contour c = new Contour(0.1, 0.01, 10);
    try {
      double lower_bound = 0.12;
      double upper_bound = 0.14;

      Contour sub_c = ContourUtils.getSubContour(c, lower_bound, upper_bound);

      assertTrue("contour bound, " + sub_c.getStart() + ", smaller than to lower_bound, " + lower_bound,
          sub_c.getStart() >= lower_bound);
      assertTrue(
          "contour bound, " + (((sub_c.size() - 1) * sub_c.getStep()) + sub_c.getStart()) +
              ", greater than upper_bound, " +
              upper_bound, ((sub_c.size() - 1) * sub_c.getStep()) + sub_c.getStart() <= upper_bound);


    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSubContourContents() {
    double[] v = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    Contour c = new Contour(0.1, 0.01, v);
    try {
      double lower_bound = 0.12;
      double upper_bound = 0.14;

      Contour sub_c = ContourUtils.getSubContour(c, lower_bound, upper_bound);

      assertEquals(3.0, sub_c.get(0), 0.0001);
      assertEquals(4.0, sub_c.get(1), 0.0001);
      assertEquals(5.0, sub_c.get(2), 0.0001);

    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetSubContourFailsOnBadBounds() {
    Contour c = new Contour(0.1, 0.01, 10);

    try {
      Contour sub_c = ContourUtils.getSubContour(c, 0.16, 0.14);

      fail();
    } catch (AuToBIException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testZScoreNormalizationPreservesEmptyValues() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 3, 4, 5});
    c.setEmpty(0);
    c.setEmpty(4);

    SpeakerNormalizationParameter norm_params = new SpeakerNormalizationParameter("");
    norm_params.insertPitch(1);
    norm_params.insertPitch(2);
    norm_params.insertPitch(3);

    Contour norm_c = ContourUtils.zScoreNormalizeContour(c, norm_params, "f0");
    assertTrue(norm_c.isEmpty(0));
    assertTrue(norm_c.isEmpty(4));
  }

  @Test
  public void testDeltaContourPreservesEmptyValues() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 3, 4, 5});
    c.setEmpty(0);
    c.setEmpty(3);

    Contour delta_c = ContourUtils.generateDeltaContour(c);
    assertTrue(delta_c.isEmpty(0));
    assertTrue(delta_c.isEmpty(2));
    assertTrue(delta_c.isEmpty(3));
  }

  @Test
  public void testDeltaContourSize() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 3, 4, 5});

    Contour delta_c = ContourUtils.generateDeltaContour(c);
    assertEquals(4, delta_c.size());
  }

  @Test
  public void testAssignValuesToSubRegionWorks() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 3, 4, 5});
    Region r = new Region(0.0, 0.501);
    r.setAttribute("feature", c);
    Region sub_r = new Region(0.1, 0.301);
    List<Region> regions = new ArrayList<Region>();
    regions.add(r);
    List<Region> sub_regions = new ArrayList<Region>();
    sub_regions.add(sub_r);
    try {
      ContourUtils.assignValuesToSubregions(sub_regions, regions, "feature");

      assertTrue(sub_r.hasAttribute("feature"));
      Contour sub_c = (Contour) sub_r.getAttribute("feature");
      assertEquals(3, sub_c.size());
      assertEquals(0.1, sub_c.getStart(), 0.001);
      assertEquals(0.1, sub_c.getStep(), 0.001);
    } catch (AuToBIException e) {
      e.printStackTrace();
    }

  }
}
