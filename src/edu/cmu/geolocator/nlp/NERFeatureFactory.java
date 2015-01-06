package edu.cmu.geolocator.nlp;

import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FeatureGenerator;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FineFeatureGenerator;
import edu.cmu.geolocator.resource.ResourceFactory;

public class NERFeatureFactory {

  private static FeatureGenerator enFeatureGenerator;
  private static FineFeatureGenerator fineenFeatureGenerator;

  private static FeatureGenerator esFeatureGenerator;

  public static FeatureGenerator getInstance(String langCode) throws Exception{
    if (langCode.equalsIgnoreCase("en")){
      if (enFeatureGenerator==null)
        enFeatureGenerator =  new FeatureGenerator("en", ResourceFactory.getClbIndex(), "res/");
      return enFeatureGenerator;
    }
    if (langCode.equalsIgnoreCase("fine-en")){
      if (fineenFeatureGenerator==null)
        fineenFeatureGenerator =  new FineFeatureGenerator();
      return fineenFeatureGenerator;
    }
    if(langCode.equalsIgnoreCase("es")){
      if(esFeatureGenerator==null)
        esFeatureGenerator= new FeatureGenerator("es", ResourceFactory.getClbIndex(), "res/");
      return esFeatureGenerator;
    }
    if(langCode.equalsIgnoreCase("ace")){
        if(esFeatureGenerator==null)
          esFeatureGenerator= new FeatureGenerator("ace", ResourceFactory.getClbIndex(), "res/");
        return esFeatureGenerator;
      }
    
    
    throw new Exception("Language not compatible with NER FeatureGenerator.");
  }
}
