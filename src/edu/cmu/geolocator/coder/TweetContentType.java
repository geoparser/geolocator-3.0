package edu.cmu.geolocator.coder;

public class TweetContentType {
  
  public static enum CONTENTTYPE {ISL,BAY};

  public static CONTENTTYPE type2Feature(String text){
    text = text.toLowerCase();
    if (text.contains("beach"))
      return CONTENTTYPE.ISL;
    if(text.contains("bay"))
      return CONTENTTYPE.BAY;
    return null;
  }
}
