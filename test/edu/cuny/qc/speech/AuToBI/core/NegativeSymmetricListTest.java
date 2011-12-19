/*  NegativeSymmetricListTest.java

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA. User: andrew Date: Dec 11, 2010 Time: 6:37:35 PM To change this template use File |
 * Settings | File Templates.
 */
public class NegativeSymmetricListTest {

  @Test
  public void testGetAndSetZero() {
    NegativeSymmetricList list = new NegativeSymmetricList();
    for (int i = 0; i < 6; ++i)
      list.add(6.0);
    list.set(0, 5.0);
    assertEquals(5.0, list.get(0), 0.0001);
  }

  @Test
  public void testGetNegativeAndSetNegative() {
    NegativeSymmetricList list = new NegativeSymmetricList();
    for (int i = 0; i < 6; ++i)
      list.add(6.0);

    list.set(-4, 5.0);
    assertEquals(5.0, list.get(-4), 0.0001);
  }

  @Test
  public void testGetPositiveAndSetNegative() {
    NegativeSymmetricList list = new NegativeSymmetricList();
    for (int i = 0; i < 6; ++i)
      list.add(6.0);

    list.set(-4, 5.0);
    assertEquals(5.0, list.get(4), 0.0001);
  }

  @Test
  public void testGetNegativeAndSetPositive() {
    NegativeSymmetricList list = new NegativeSymmetricList();
    for (int i = 0; i < 6; ++i)
      list.add(6.0);

    list.set(4, 5.0);
    assertEquals(5.0, list.get(-4), 0.0001);
  }

  @Test
  public void testGetPositiveAndSetPositive() {
    NegativeSymmetricList list = new NegativeSymmetricList();
    for (int i = 0; i < 6; ++i)
      list.add(6.0);

    list.set(4, 5.0);
    assertEquals(5.0, list.get(4), 0.0001);
  }

}

