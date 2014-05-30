/*  AuToBIParametersTest.java

    Copyright (c) 2011 Andrew Rosenberg

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
package edu.cuny.qc.speech.AuToBI.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Class for Aggregation.
 *
 * @see Aggregation
 */
public class AuToBIParametersTest {


  @Test
  public void testGetAndSetParameters() {
    AuToBIParameters params = new AuToBIParameters();

    params.setParameter("test_param", "one");

    try {
      assertEquals("one", params.getParameter("test_param"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testGetNonExistentParameters() {
    AuToBIParameters params = new AuToBIParameters();

    try {
      params.getParameter("test_param");
      fail();
    } catch (AuToBIException e) {
      // Expected.
    }
  }

  @Test
  public void testGetAndSetOptionalParameters() {
    AuToBIParameters params = new AuToBIParameters();

    params.setParameter("test_param", "one");

    assertEquals("one", params.getOptionalParameter("test_param"));

  }

  @Test
  public void testGetNonExistentOptionalParameters() {
    AuToBIParameters params = new AuToBIParameters();

    assertNull(params.getOptionalParameter("test_param"));
  }


  @Test
  public void testGetAndSetOptionalParametersWithDefault() {
    AuToBIParameters params = new AuToBIParameters();

    params.setParameter("test_param", "one");

    assertEquals("one", params.getOptionalParameter("test_param", "two"));

  }

  @Test
  public void testGetNonExistentOptionalParametersWithDefault() {
    AuToBIParameters params = new AuToBIParameters();

    assertEquals("two", params.getOptionalParameter("test_param", "two"));
  }

  @Test
  public void testBooleanParameterTrue() {
    AuToBIParameters params = new AuToBIParameters();
    params.setParameter("bool", "true");

    assertTrue(params.booleanParameter("bool", false));
  }

  @Test
  public void testBooleanParameterFalse() {
    AuToBIParameters params = new AuToBIParameters();
    params.setParameter("bool", "false");

    assertFalse(params.booleanParameter("bool", false));
  }

  @Test
  public void testBooleanParameterWeirdParameter() {
    AuToBIParameters params = new AuToBIParameters();
    params.setParameter("bool", "neither");

    assertFalse(params.booleanParameter("bool", false));
  }

  @Test
  public void testBooleanParameterDefaultFalse() {
    AuToBIParameters params = new AuToBIParameters();

    assertFalse(params.booleanParameter("bool", false));
  }

  @Test
  public void testBooleanParameterDefaultTrue() {
    AuToBIParameters params = new AuToBIParameters();

    assertTrue(params.booleanParameter("bool", true));
  }

  @Test
  public void testReadParametersParsesParameters() {

    String[] parameters = new String[]{"-test=go"};
    AuToBIParameters params = new AuToBIParameters();

    params.readParameters(parameters);

    try {
      assertEquals("go", params.getParameter("test"));
    } catch (AuToBIException e) {
      fail();
    }
  }

  @Test
  public void testReadParametersParsesBooleanParameters() {
    String[] parameters = new String[]{"-test"};
    AuToBIParameters params = new AuToBIParameters();

    params.readParameters(parameters);
    assertTrue(params.booleanParameter("test", false));
  }

  @Test
  public void testReadParametersIgnoresFieldsWOHyphen() {
    String[] parameters = new String[]{"test"};
    AuToBIParameters params = new AuToBIParameters();

    params.readParameters(parameters);
    assertFalse(params.hasParameter("test"));
  }
}



