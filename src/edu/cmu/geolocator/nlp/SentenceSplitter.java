package edu.cmu.geolocator.nlp;

import java.util.List;

public interface SentenceSplitter {

  List<String> split(String s);
  
}
