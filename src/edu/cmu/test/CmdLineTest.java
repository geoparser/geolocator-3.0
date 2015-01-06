package edu.cmu.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.coder.CoderFactory;
import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.LocGroupFeatures;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.parser.ParserFactory;

public class CmdLineTest {

  public static void main(String argv[]) throws Exception {
    GlobalParam.setGazIndex("C:\\chenxu\\geolocator-3.0\\GazIndex");
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(isr);
    String s = null;
    System.out.println(">");

    while ((s = br.readLine()) != null) {
      System.out.println(">");

      if (s.length() < 1) {
        System.out.println(">");
        continue;
      }
      BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.US);
      boundary.setText(s);
      int start = boundary.first();
      for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {

        Tweet tweet = new Tweet(s.substring(start,end));
        System.out.println("geoparsing...");
        
        List<LocEntityAnnotation> topos = ParserFactory.getACENERParser().parse(tweet);        // List<LocEntityAnnotation> topos = ParserFactory.getEnToponymParser().parse(tweet);
        tweet.setToponyms(topos);

        System.out.println("geocoding...");
        List<CandidateAndFeature> resolved = CoderFactory.getENAggGeoCoder().resolve(tweet,
                LocGroupFeatures.DEBUGMODE, LocGroupFeatures.FILTERLESS1000POP);

        if (topos == null || topos.size()==0) {
          System.err.println("No resolved toponyms");
          continue;
        }

        for (LocEntityAnnotation topo : topos) {
          System.out.println(topo.getTokenString() + " " + topo.getNEType() + " "+topo.getToksStart()+" "+topo.getToksEnd()
              +" "    + topo.getNETypeProb());
        }
        if (resolved == null) {
          System.err.println("No resolved coordinates");
          continue;
        }

        for (CandidateAndFeature code : resolved) {
          System.out.println(code.getAsciiName() + " " + code.getCountryCode() + " "
                  + code.getLatitude() + "" + code.getLongitude() + "[Prob]:" + code.getProb());
        }
        System.out.println(">");
      
        System.out.println(s.substring(start, end));
      }

      Tweet tweet = new Tweet(s);
      System.out.println("geoparsing...");
      List<LocEntityAnnotation> topos = ParserFactory.getEnAggrParser().parse(tweet);
      // List<LocEntityAnnotation> topos = ParserFactory.getEnToponymParser().parse(tweet);
      tweet.setToponyms(topos);

      System.out.println("geocoding...");
      List<CandidateAndFeature> resolved = CoderFactory.getENAggGeoCoder().resolve(tweet,
              LocGroupFeatures.DEBUGMODE, LocGroupFeatures.FILTERLESS1000POP);

      if (topos == null || topos.size()==0) {
        System.err.println("No resolved toponyms");
        continue;
      }

      for (LocEntityAnnotation topo : topos) {
        System.out.println(topo.getTokenString() + " " + topo.getNEType() + " "+topo.getToksStart()+" "+topo.getToksEnd()
            +" "    + topo.getNETypeProb()+ topo.getToksStart()+topo.getToksEnd());
      }
      if (resolved == null) {
        System.err.println("No resolved coordinates");
        continue;
      }

      for (CandidateAndFeature code : resolved) {
        System.out.println(code.getAsciiName() + " " + code.getCountryCode() + " "
                + code.getLatitude() + "" + code.getLongitude() + "[Prob]:" + code.getProb());
      }
      System.out.println(">");
    }
  }

}
