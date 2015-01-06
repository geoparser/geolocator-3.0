package edu.cmu.geolocator;

import java.util.Arrays;
import java.util.HashSet;

public class GlobalParam {

  public static String gazIndex = "GazIndex";

  public static String geoNames = "GeoNames";


  public static String getGazIndex() {
    return gazIndex;
  }

  public static void setGazIndex(String gazIndex) {
    GlobalParam.gazIndex = gazIndex;
    return;
  }

  public static String getGeoNames() {
    return geoNames;
  }

  public static void setGeoNames(String geoNames) {
    GlobalParam.geoNames = geoNames;
    return;
  }

}
