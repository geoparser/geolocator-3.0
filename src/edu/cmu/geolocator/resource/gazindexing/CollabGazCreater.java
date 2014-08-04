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
package edu.cmu.geolocator.resource.gazindexing;

import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.GazInfoIndexerAllCountries;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.GazStringIndexerAllCountries;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.GazStringIndexerAltNames;

public class CollabGazCreater {

  public static void main(String argv[]) throws Exception{
    String geoNames=argv[0];
    GlobalParam.setGeoNames(geoNames);
    String gazIndex=argv[1];
    GlobalParam.setGazIndex(gazIndex);  
    
    GazInfoIndexerAllCountries.main(new String[]{"-write"});
    GazStringIndexerAllCountries.main(new String[]{"-write"});
    GazStringIndexerAltNames.main(new String[]{"-write"});
    
  }
}
