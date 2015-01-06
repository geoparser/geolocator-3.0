package edu.cmu.geolocator.nlp;

import edu.cmu.geolocator.model.Sentence;

public interface Lemmatizer {
	Sentence lemmatize(Sentence sent);
}
