package edu.cmu.geolocator.nlp;

import edu.cmu.geolocator.model.*;
public interface POSTagger {

	Sentence tag(Sentence sent);
}
