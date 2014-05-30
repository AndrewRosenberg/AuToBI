/*  ContourUtilsTest.java

    Copyright 2011 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
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
  public void testGetNullSubContour() {
    Contour c = null;

    try {
      Contour sub_c = ContourUtils.getSubContour(c, 0.12, 0.14);

      fail();
    } catch (AuToBIException e) {
      // Should throw an exception
    }
  }

  @Test
  public void testGetSubEmptyContour() {
    Contour c = new Contour(0.1, 0.01, 1);

    try {
      Contour sub_c = ContourUtils.getSubContour(c, 0.12, 0.14);

      assertEquals(0, sub_c.contentSize());
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
    assertEquals(5, delta_c.size());
  }

  @Test
  public void testDeltaContourWithEmptyContour() {

    Contour c = new Contour(0.0, 0.1, 0);

    Contour delta_c = ContourUtils.generateDeltaContour(c);
    assertEquals(0, delta_c.size());
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
      fail();
    }
  }

  @Test
  public void testAssignValuesToSubRegionWithNoFeature() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 3, 4, 5});
    Region r = new Region(0.0, 0.501);

    //r.setAttribute("feature", c);
    Region sub_r = new Region(0.1, 0.301);
    List<Region> regions = new ArrayList<Region>();
    regions.add(r);
    List<Region> sub_regions = new ArrayList<Region>();
    sub_regions.add(sub_r);
    try {
      ContourUtils.assignValuesToSubregions(sub_regions, regions, "feature");

      assertFalse(sub_r.hasAttribute("feature"));
    } catch (AuToBIException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testGetIndexOfMaximumWorks() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 4, 5});

    int i = ContourUtils.getIndexOfMaximum(c);
    assertEquals(2, i);
  }

  @Test
  public void testGetIndexOfMaximumWorksWithMultipleMaxima() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 4, 6});

    int i = ContourUtils.getIndexOfMaximum(c);
    assertEquals(2, i);
  }

  @Test
  public void testGetIndexOfFollowingMinimum() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 4, 6});

    int i = ContourUtils.getIndexOfFollowingMinimum(c, 2, 0.01);
    assertEquals(3, i);
  }

  @Test
  public void testGetIndexOfFollowingWhenOneDoesntExist() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 7, 8});

    int i = ContourUtils.getIndexOfFollowingMinimum(c, 2, 0.01);
    assertEquals(2, i);
  }

  @Test
  public void testGetIndexOfFollowingWithoutRise() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});

    int i = ContourUtils.getIndexOfFollowingMinimum(c, 2, 0.01);
    assertEquals(4, i);
  }


  @Test
  public void testGetIndexOfPrecedingMinimum() {
    Contour c = new Contour(0.0, 0.1, new double[]{5, 2, 6, 4, 6});

    int i = ContourUtils.getIndexOfPrecedingMinimum(c, 2, 0.01);
    assertEquals(1, i);
  }

  @Test
  public void testGetIndexOfPrecedingWhenOneDoesntExist() {
    Contour c = new Contour(0.0, 0.1, new double[]{7, 6.4, 6, 7, 8});

    int i = ContourUtils.getIndexOfPrecedingMinimum(c, 2, 0.01);
    assertEquals(2, i);
  }

  @Test
  public void testGetIndexOfPrecedingWithoutRise() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});

    int i = ContourUtils.getIndexOfPrecedingMinimum(c, 2, 0.01);
    assertEquals(0, i);
  }

  @Test
  public void testAssignValuesToRegions() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});
    List<Region> regions = new ArrayList<Region>();
    regions.add(new Region(0.0, 0.2));
    regions.add(new Region(0.2, 0.3));

    try {
      ContourUtils.assignValuesToRegions(regions, c, "feature");
      assertTrue(regions.get(0).hasAttribute("feature"));
      assertTrue(regions.get(1).hasAttribute("feature"));
    } catch (AuToBIException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testAssignValuesToOverlappingRegions() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});
    List<Region> regions = new ArrayList<Region>();
    regions.add(new Region(0.0, 0.2));
    regions.add(new Region(0.0, 0.3));

    try {
      ContourUtils.assignValuesToRegions(regions, c, "feature");
      assertTrue(regions.get(0).hasAttribute("feature"));
      assertTrue(regions.get(1).hasAttribute("feature"));
    } catch (AuToBIException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testAssignValuesToRegionsFailsOnNull() {

    Contour c = null;
    List<Region> regions = new ArrayList<Region>();
    regions.add(new Region(0.0, 0.2));
    regions.add(new Region(0.2, 0.3));

    try {
      ContourUtils.assignValuesToRegions(regions, c, "feature");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testAssignValuesToOrderedRegions() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});
    List<Region> regions = new ArrayList<Region>();
    regions.add(new Region(0.0, 0.22));
    regions.add(new Region(0.22, 0.33));

    ContourUtils.assignValuesToOrderedRegions(regions, c, "feature");
    assertTrue(regions.get(0).hasAttribute("feature"));
    assertTrue(regions.get(1).hasAttribute("feature"));

  }

  @Test
  public void testAssignValuesToOrderedRegionsWithExtraRegions() {

    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});
    List<Region> regions = new ArrayList<Region>();
    regions.add(new Region(0.0, 0.22));
    regions.add(new Region(0.22, 0.33));
    regions.add(new Region(5.00, 6.00));

    ContourUtils.assignValuesToOrderedRegions(regions, c, "feature");
    assertTrue(regions.get(0).hasAttribute("feature"));
    assertTrue(regions.get(1).hasAttribute("feature"));
    assertTrue(regions.get(2).hasAttribute("feature"));

  }

  @Test
  public void testAssignValuesToOrderedRegionsWithNoRegions() {
    Contour c = new Contour(0.0, 0.1, new double[]{1, 2, 6, 5, 3});
    List<Region> regions = new ArrayList<Region>();

    ContourUtils.assignValuesToOrderedRegions(regions, c, "feature");
  }

  @Test
  public void testInterpolateWorks() {
    Contour c = new Contour(0.0, 0.1, 5);
    Contour intensity = new Contour(0.0, 0.1, new double[]{1, 1, 1, 1, 1});
    c.set(0, 1);
    c.set(4, 5);

    Contour ic = ContourUtils.interpolate(c, intensity, 0);
    assertEquals(2.0, ic.get(1), 0.00001);
    assertEquals(3.0, ic.get(2), 0.00001);
    assertEquals(4.0, ic.get(3), 0.00001);
  }

  @Test
  public void testInterpolateOmitsRightEdge() {
    Contour c = new Contour(0.0, 0.1, 7);
    Contour intensity = new Contour(0.0, 0.1, new double[]{1, 1, 1, 1, 1, 1, 1});
    c.set(0, 1);
    c.set(4, 5);

    Contour ic = ContourUtils.interpolate(c, intensity, 0);
    assertTrue(ic.isEmpty(5));
    assertTrue(ic.isEmpty(6));
  }

  @Test
  public void testInterpolateOmitsLeftEdge() {
    Contour c = new Contour(0.0, 0.1, 7);
    Contour intensity = new Contour(0.0, 0.1, new double[]{1, 1, 1, 1, 1, 1, 1});
    c.set(2, 1);
    c.set(6, 5);

    Contour ic = ContourUtils.interpolate(c, intensity, 0);
    assertTrue(ic.isEmpty(0));
    assertTrue(ic.isEmpty(1));
  }

  @Test
  public void testInterpolateOmitsAcrossAGap() {
    Contour c = new Contour(0.0, 0.1, 7);
    Contour intensity = new Contour(0.0, 0.1, new double[]{1, 1, 1, 0, 0, 1, 1});
    c.set(0, 1);
    c.set(1, 1);
    c.set(2, 1);

    c.set(5, 5);
    c.set(6, 5);

    Contour ic = ContourUtils.interpolate(c, intensity, 0.5);
    assertTrue(ic.isEmpty(3));
    assertTrue(ic.isEmpty(4));
  }
}
