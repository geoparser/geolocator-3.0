package edu.cmu.geolocator.nlp;

import edu.cmu.geolocator.nlp.lemma.AnnaLemmatizer;
import edu.cmu.geolocator.nlp.lemma.UWMorphaStemmer;
import edu.cmu.geolocator.nlp.pos.ENTweetPOSTagger;
import edu.cmu.geolocator.nlp.pos.ESAnnaPOSTagger;

public class NLPFactory {
  /**
   * @return the enPosTagger
   */
  public static POSTagger getEnPosTagger() {
    return ENTweetPOSTagger.getInstance();
  }

  public static Lemmatizer getEnUWStemmer() {
    return UWMorphaStemmer.getInstance();
  }

  public static POSTagger getEsPosTagger() {
    return ESAnnaPOSTagger.getInstance();
  }

  public static Lemmatizer getEsAnnaLemmatizer() {
    try {
      return AnnaLemmatizer.getInstance("es");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  public static Lemmatizer getEnAnnaLemmatizer() {
    try {
      return AnnaLemmatizer.getInstance("en");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
