package edu.cmu.geolocator.nlp;

import java.io.IOException;

import edu.cmu.geolocator.model.Tweet;

public interface MisspellParser {

	String parse(String s) throws IOException;
}
