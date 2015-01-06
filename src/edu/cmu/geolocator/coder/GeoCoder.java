package edu.cmu.geolocator.coder;

import java.util.List;

import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.Tweet;

public interface GeoCoder {

  List<CandidateAndFeature> resolve(Tweet example, String mode,String filter) throws Exception;

}
