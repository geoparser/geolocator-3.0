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
