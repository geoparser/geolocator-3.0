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
 @author Wei Zhang
*/
package edu.cmu.geolocator.resource;

import edu.cmu.geolocator.resource.Map.AdmCode2GazCandidateMap;
import edu.cmu.geolocator.resource.Map.CCodeAdj2CTRYtype;
import edu.cmu.geolocator.resource.Map.FeatureCode2Map;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.CollaborativeIndex;

public class ResourceFactory {

  private static CollaborativeIndex collaborativeIndex = CollaborativeIndex.getInstance();

  private static AdmCode2GazCandidateMap adminCode2GazCandidateMap = AdmCode2GazCandidateMap
          .getInstance();

  private static CCodeAdj2CTRYtype countryCode2CountryMap = CCodeAdj2CTRYtype
          .getInstance();

  private static FeatureCode2Map featurecode2map = FeatureCode2Map.getInstance();
  /**
   * @return the collaborativeIndex
   */
  public static CollaborativeIndex getClbIndex() {
    return collaborativeIndex;
  }

  /**
   * return the GazEntryAndInfo type given the admin code.
   * @return the adminCode2GazCandidateMap
   */
  public static AdmCode2GazCandidateMap getAdminCode2GazCandidateMap() {
    return adminCode2GazCandidateMap;
  }

  /**
   * return the Country type given the country code.
   * @return the countryCode2CountryMap
   * 
   */
  public static CCodeAdj2CTRYtype getCountryCode2CountryMap() {
    return countryCode2CountryMap;
  }

  public static FeatureCode2Map getFeatureCode2Map() {
    // TODO Auto-generated method stub
    return featurecode2map;
  }
}
