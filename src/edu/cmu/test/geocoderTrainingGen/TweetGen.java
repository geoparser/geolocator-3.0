package edu.cmu.test.geocoderTrainingGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.io.GetReader;
import edu.cmu.geolocator.io.GetWriter;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.parser.ParserFactory;

public class TweetGen {

  public static String norm(String text) {
    return text.replace("\n", " ").replace("\r", "").replace("\t", " ");
  }

  public static void main(String argv[]) throws FileNotFoundException, UnsupportedEncodingException {
    GlobalParam.setGazIndex("GazIndex");
    GlobalParam.setGeoNames("GeoNames");
    
    String path = "/Users/Indri/Documents/Research_data/Disambiguation/additionalData/";
    BufferedWriter bw = null;
    try {
      bw = GetWriter
              .getFileWriter(path+"all.txt");
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    File folder = new File(
            path+"coord/");

    BufferedReader br = null;
    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.getName().equals(".DS_Store"))
        continue;
      try {
        br = GetReader.getUTF8FileReader(path+"coord/"+fileEntry.getName());
      } catch (FileNotFoundException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      } catch (UnsupportedEncodingException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      }
      
      String line = null;
      Status tweet = null;
      String text, uloc = "", udsc = "", tzone = "", pname = "", pcnty = "", ptype = "";
      double lat, lon;
      StringBuilder parsedlocs;
      List<LocEntityAnnotation> locations,gazentries;
      GeoLocation[][] pbox = null;
      String bbox = null;
      try {
        while ((line = br.readLine()) != null) {

          text = "";
          uloc = "";
          udsc = "";
          tzone = "";
          pname = "";
          pcnty = "";
          ptype = "";
          bbox = "";
          lat = 0;
          lon = 0;
          parsedlocs = new StringBuilder();
          locations = null;
          pbox = null;

          if (line.trim().length() == 0)
            continue;
          try {
            tweet = DataObjectFactory.createStatus(line);
            // System.out.println(tweet.getText().trim());
          } catch (TwitterException e) {
            // TODO Auto-generated catch block
            System.err.println("Not parserable");
            e.printStackTrace();
            continue;
          }
          text = tweet.getText();
          text = TweetGen.norm(text);
          if (text.trim().length() < 2)
            continue;
          if (tweet.getGeoLocation() != null) {
            lat = tweet.getGeoLocation().getLatitude();
            lon = tweet.getGeoLocation().getLongitude();
          }
          if (tweet.getUser() != null) {
            uloc = tweet.getUser().getLocation() != null ? tweet.getUser().getLocation() : "";
            udsc = tweet.getUser().getDescription() != null ? tweet.getUser().getDescription() : "";
            tzone = tweet.getUser().getTimeZone() != null ? tweet.getUser().getTimeZone() : "";
            uloc = TweetGen.norm(uloc);
            udsc = TweetGen.norm(udsc);
            tzone = TweetGen.norm(tzone);
          }
          if (tweet.getPlace() != null) {
            pname = tweet.getPlace().getFullName() != null ? tweet.getPlace().getFullName() : "";
            pcnty = tweet.getPlace().getCountry() != null ? tweet.getPlace().getCountry() : "";
            ptype = tweet.getPlace().getPlaceType() != null ? tweet.getPlace().getPlaceType() : "";
            pname = TweetGen.norm(pname);
            pcnty = TweetGen.norm(pcnty);
            ptype = TweetGen.norm(ptype);
            pbox = tweet.getPlace().getBoundingBoxCoordinates() != null ? tweet.getPlace()
                    .getBoundingBoxCoordinates() : null;
            if (pbox != null) {
              bbox += "[" + pbox[0][0].getLatitude() + " " + pbox[0][0].getLongitude() + "] ["
                      + pbox[0][1].getLatitude() + " " + pbox[0][1].getLongitude() + "] ["
                      + pbox[0][2].getLatitude() + " " + pbox[0][2].getLongitude() + "] ["
                      + pbox[0][3].getLatitude() + " " + pbox[0][3].getLongitude() + "]";
              bbox = "\"type\"\":\"\"Polygon\"\" \"\"coordinates\"\":[[" + bbox + "]]\"\"\"";
              System.out.println(bbox);
            }
          }
          Tweet t = new Tweet(tweet);
//          locations = ParserFactory.getEnNERParser().parse(t);
          gazentries = ParserFactory.getEnToponymParser().parse(t);
          if (gazentries.size()==0)
            continue;
          System.out.println("Tweet is : " + tweet.getText());
          System.out.println("Locations Recognized are: " + gazentries.size());
          parsedlocs = new StringBuilder();
          for (LocEntityAnnotation loc : gazentries) {
            if(gazentries.contains(loc))
              parsedlocs.append("tp{").append(loc.getTokenString()).append("[ , ]").append("}tp");
          }
          if (parsedlocs.length()==0)
            continue;
          bw.write(text + "\t" + parsedlocs.toString() + "\t" + lat + "\t" + lon + "\t" + uloc
                  + "\t" + tzone + "\t" + udsc + "\tnull\t" + pname + "\t" + pcnty + "\t" + ptype
                  + "\t" + bbox + "\n");
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        try {
          br.close();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
      try {
        br.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    try {
      bw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
