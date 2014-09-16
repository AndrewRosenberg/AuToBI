/*  Pair.java

    Copyright (c) 2009-2014 Andrew Rosenberg

  This file is part of the AuToBI prosodic analysis package.

  AuToBI is free software: you can redistribute it and/or modify
  it under the terms of the Apache License (see boilerplate below)

 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You should have received a copy of the Apache 2.0 License along with AuToBI.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 */
package edu.cuny.qc.speech.AuToBI.core;

/**
 * Pair<T1,T2> is a lightweight generic to represent two objects as a single Pair.
 *
 * @param <T1> the first object
 * @param <T2> the second object
 */
public class Pair<T1, T2> {
  public T1 first;   // the first object
  public T2 second;  // the second object

  /**
   * Constructs a new pair with values, v1 and v2.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  public Pair(T1 v1, T2 v2) {
    first = v1;
    second = v2;
  }

  /**
   * Constructs a new empty pair.
   */
  public Pair() {
    first = null;
    second = null;
  }
}
