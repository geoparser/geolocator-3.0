/**
 * 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 * 
 * @author Wei Zhang,  Language Technology Institute, School of Computer Science, Carnegie-Mellon University.
 * email: wei.zhang@cs.cmu.edu
 * 
 */

package edu.cmu.geolocator.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry; 

public class CollectionSorting {

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static ArrayList<Entry<String, Float>> rankArray(ArrayList<Entry<String, Float>> as) {
    // sort by frequency
    Collections.sort(as, new Comparator() {
      public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        Float first = (Float) e1.getValue();
        Float second = (Float) e2.getValue();
        return second.compareTo(first);
      }
    });

    return as;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T extends Comparable<T>> ArrayList<Entry<String, T>> rankIntArray(ArrayList<Entry<String, T>> as) {
    // sort by frequency
    Collections.sort(as, new Comparator() {
      public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        T first = (T) e1.getValue();
        T second = (T) e2.getValue();
        return second.compareTo(first);
      }
    });
    return as;
  }

  public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
    ArrayList mapKeys = new ArrayList(passedMap.keySet());
    ArrayList mapValues = new ArrayList(passedMap.values());
    Collections.sort(mapValues);
    Collections.sort(mapKeys);

    LinkedHashMap sortedMap = new LinkedHashMap();

    Iterator valueIt = mapValues.iterator();
    while (valueIt.hasNext()) {
      Object val = valueIt.next();
      Iterator keyIt = mapKeys.iterator();

      while (keyIt.hasNext()) {
        Object key = keyIt.next();
        String comp1 = passedMap.get(key).toString();
        String comp2 = val.toString();

        if (comp1.equals(comp2)) {
          passedMap.remove(key);
          mapKeys.remove(key);
          sortedMap.put((String) key, val);
          break;
        }

      }

    }
    return sortedMap;
  }
}
