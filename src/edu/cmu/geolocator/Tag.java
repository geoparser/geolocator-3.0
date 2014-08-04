
/*Copyright 2014, Language Technologies Institute, Carnegie Mellon
University

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
*/
package edu.cmu.geolocator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public class Tag {
  public static final String[] tagstrings = { 
    "TP", "tp", "B-Toponym", "I-Toponym", 
    "BD", "bd", "B-NamedBusinessOrBuilding", "I-NamedBusinessOrBuilding", "NamedBusinessOrBuilding", 
    "ST","st", "B-Street", "I-Street",
    "AB", "ab", 
    "I-Person", "B-Person", "UnnamedLocation", "B-UnnamedLocation", "I-UnnamedLocation" 
    };

  /**
   * filter type input should be tp, st, bd, ab, and o.
   * 
   * @param filtertypes
   * @return
   */
  public static HashSet<String> getFilter(HashMap<String, Boolean> filtertypes){
    HashSet<String> filterSet = new HashSet<String>();
    for (Entry<String, Boolean> ft : filtertypes.entrySet()){
      if (ft.getKey().equalsIgnoreCase("tp") && ft.getValue()==true){
        filterSet.add(tagstrings[0]);filterSet.add(tagstrings[1]);
        filterSet.add(tagstrings[2]);filterSet.add(tagstrings[3]);
      }
      if (ft.getKey().equalsIgnoreCase("bd")&& ft.getValue()==true){
        filterSet.add(tagstrings[4]);filterSet.add(tagstrings[5]);
        filterSet.add(tagstrings[6]);filterSet.add(tagstrings[7]);filterSet.add(tagstrings[8]);
      }

      if (ft.getKey().equalsIgnoreCase("st")&& ft.getValue()==true){
        filterSet.add(tagstrings[9]);filterSet.add(tagstrings[10]);
        filterSet.add(tagstrings[11]);filterSet.add(tagstrings[12]);
      }

      if (ft.getKey().equalsIgnoreCase("ab")&& ft.getValue()==true){
        filterSet.add(tagstrings[13]);filterSet.add(tagstrings[14]);
      }
    }
    return filterSet;
  }
}
