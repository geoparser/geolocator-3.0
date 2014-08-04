/*Copyright 2014, Language Technologies Institute, Carnegie Mellon
University

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
 @author Wei Zhang
*/
package edu.cmu.geolocator.nlp.ner.FeatureExtractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.geolocator.common.StringUtil;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.nlp.StanfordCoreTools.StanfordNLP;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.resource.dictionary.Dictionary;
import edu.cmu.geolocator.resource.trie.IndexSupportedTrie;
import edu.cmu.minorthird.classify.Feature;
import Wordnet.*;

public class FineFeatureGenerator extends FeatureGenerator{

  HashSet<String> preposition, countries;
  static HashMap<String,String> clusters ;
  Dictionary prepdict, countrydict;
  static StanfordNLP snlp;
  IndexSupportedTrie trie;
  static ArrayList<String> naturalFeaturesList ;
  static public ArrayList<String> unnamedLocationsList ;
  static ArrayList<String> personNamesList ;
  static ArrayList<String> sportsTeamsList ;
  static ArrayList<String> namedOrganizationsList ;
  static ArrayList<String> namedOrgIndicatorList ;
  static ArrayList<String> spatialVerbsList ;
  static ArrayList<String> spatialRelationsList ;
  static ArrayList<String> spatialPrepsList ;
  static ArrayList<String> streetsuffixList ;
  static ArrayList<String> newsPaperList ;
  static ArrayList<String> numbersList ;
  static HashSet<String> toponymsList ;
  
  public static void readAllLists() throws IOException{
    
    namedOrganizationsList = readListFile("LNamedOrganization");
    unnamedLocationsList = readListFile("LUnnamedLocation");
    namedOrgIndicatorList = readListFile("LNamedOrgIndicator");
    spatialVerbsList = readListFile("LSpatialVerbs");
    spatialRelationsList = readListFile("LSpatialRelations");
    personNamesList = readListFile("LPersonNames");
    spatialPrepsList = readListFile("LSpatialPreps");
    streetsuffixList = readListFile("LStreetSuffix");
    sportsTeamsList = readListFile("LSportsTeams");
    newsPaperList = readListFile("LNewsPapers");
    numbersList = readListFile("LNumbers");
    toponymsList = readSetFile("LAllCountries"); //LAllCountries");
    
    /*  
    System.out.println(LNaturalFeatures.get(2));
    System.out.println(LUnnamedLocations.get(2));
    System.out.println(LNamedOrganizations.get(2));
    */
  }
  public static boolean containsPartial(ArrayList<String> list, String word){
    for(String tmp : list)
      if(tmp.contains(word))
        return true; 
    return false;
  }
  
  public static ArrayList<String> readListFile(String FileName) throws IOException{
    
    ArrayList<String> list = new ArrayList<String>();
    String filename = "res/lists/"+FileName+".txt";
    System.err.println("Reading file:"+filename);
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = null;
    
    while ((line = reader.readLine()) != null) {
      //Lower casing before adding to list 
      list.add(line.trim().toLowerCase());
    }
    reader.close();
    return list;
    
  }
  
  public static void ReadBrownCluster(String filename) throws IOException{
  
  clusters = new HashMap<String,String>();
  String cluster ="",line="", word=""; int check=0;
  BufferedReader bw= new BufferedReader(new FileReader(filename));
  while((line= bw.readLine())!= null)
  {
    word=line.split("\t")[1];
    cluster = line.split("\t")[0];
    //System.out.println("brownbrownw"+word+cluster);
    clusters.put(word, cluster);
  }
  
  System.out.println("BC DONE");
  
  }
  public static HashSet<String> readSetFile(String FileName) throws IOException{
    
    HashSet<String> set = new HashSet<String>();
    String filename = "res/Lists/"+FileName+".txt";
    System.err.println("Reading file:"+filename);
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = null;
    
    while ((line = reader.readLine()) != null) {
      //Lower casing before adding to list 
      set.add(line.trim().toLowerCase());
    }
    reader.close();
    return set;
  }
  
  public FineFeatureGenerator() {
    super();
    // initialize dictionary to lookup.
    // "geoNames.com/allCountries.txt"

    snlp = new StanfordNLP();
    
    // Stanford - Lemmatizer, tokenizer, NER, POS
    
      if ( unnamedLocationsList ==null )
        try {
          readAllLists();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      
      if (clusters ==null )
        try {
          ReadBrownCluster("res/brownclusters/paths");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
  }

  static int statstreet = 0;
  static int statbuilding = 0;
  static int stattoponym = 0;
  static int statabbr = 0, statadj = 0;

  String tweet;

  
  
  public static ArrayList<String> FeaturelistGen (String sentence,FineFeatureGenerator fgen) throws IOException {

    ArrayList<String> FeatureList = new ArrayList<String>();
    String[]  simpleTokenizedData= null;
    String[] tokenTags = null;
    //String[] posTags=null,nerTags=null,lemma=null;
    HashMap tagdata = new HashMap <String,String>();  
    //int length=0;
    snlp.Tokenizer(sentence);
    simpleTokenizedData= snlp.StringTokenizer(sentence);
    StringBuffer bw = new StringBuffer();
    
    
    if(simpleTokenizedData!=null || simpleTokenizedData.length != 0)
    {
    
    ArrayList<String> newTokens = new ArrayList<String>();
    tokenTags =  TokentoBIOTag(simpleTokenizedData,newTokens);
    String [] tokenizedData = new String[newTokens.size()];
    tokenizedData = newTokens.toArray(tokenizedData);
    
    // Extract features
    
    //bw.write(data);
    List<Feature[]> tokenFeatures = fgen.extractFeature(tokenizedData);
    
    // Write feature + tag for each token 
    for (int j = 0; j < tokenFeatures.size(); j++) {
      bw=new StringBuffer();
      initialFeatureWriter();
      
      bw.append(tokenizedData[j]+ " ");
      //bw.write(tokenTags[j]);
      
      for (Feature f : tokenFeatures.get(j)) {
        append(f.toString());
        
        bw.append(f.toString()+" ");
      }
      bw.append(" ");
      // location class.
      String loctag = tokenTags[j];
      //append(loctag);
      bw.append(loctag + " ");
      //fwriter.write(emit());
      
      bw.append("\n");
      FeatureList.add(bw.toString());
    }
    
    
    //fwriter.write("\n");
    
    
    
    return FeatureList ;
    }
    
    else return null;
  }


  public static void main(String argv[]) throws IOException, InterruptedException {
    FineFeatureGenerator fgen = new FineFeatureGenerator();
    String sen = "cross the United States";
    ArrayList<String> FeatureList = new ArrayList<String>();
    
    FeatureList= FeaturelistGen(sen, fgen );
    for(String fn: FeatureList )
    {
      System.out.println(fn+ " ");
    }
    
  }
  @Override
  public List<Feature[]> extractFeature(Sentence tweetSentence) {
    try {
      return extractFeature(snlp.StringTokenizer(tweetSentence.getSentenceString()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  /**
   * MAIN FUNCTION FOR EXTRACTIN FEATURES
   * 
   * @param t_tweet
   * @param trie
   * @param postags
   * @return FEATURE LISTS
   * @throws IOException 
   */
  public List<Feature[]> extractFeature(String[] tokens) throws IOException {

    List<List<Feature>> instances = new ArrayList<List<Feature>>(tokens.length);
    List<Feature> f = new ArrayList<Feature>();
    String[] posTags=null,nerTags=null,lemma=null;
    
    int length= tokens.length;
    
    posTags = new String[length+1];
    nerTags = new String[length+1];
    lemma = new String[length+1];
    // Parse features
    Map<String,String> parentEdge = new HashMap<String,String>();
    Map<String,ArrayList<String>> childrenEdge = new HashMap<String,ArrayList<String>>();
    ArrayList<String> npChunks = null;
    
    snlp.DoAll(tokens,posTags,lemma,parentEdge,childrenEdge);
    npChunks = snlp.NPChunker(tokens, posTags);
    
    for (int i = 0; i < tokens.length; i++) {
      
    
      // clear feature list for this loop
      f = new ArrayList<Feature>();
  
      genLemmaFeatures(f, tokens,lemma,posTags,i); // lemma & lower case    
      
      genBrownClusterFeatures(f, tokens,i);
      
      // /////////////////////////////// MORPH FEATURES
      //genTokenFeatures(f, lemmadata, i);
      genCapFeatures(f, tokens, i);
      
      // ////////////////////////////// SEMANTIC FEATURES
      //genPosFeatures(f, posTags, i);// I think this function is useless
      
      // ////////////////////////////// SEMANTIC FEATURES
      genChunkFeatures(f, tokens, posTags, npChunks, i);
      
      /////////////////////////////////// List Features
      genLookupListFeatures(f, tokens, i);      
    
      genParseFeatures(f, tokens, parentEdge, childrenEdge,i);      
      
      genWordnetFeatures(f, tokens,i);

      instances.add(f);
      
    }

    // convert array to output format.
    ArrayList<Feature[]> newinstances = new ArrayList<Feature[]>();
    
    for (int i1 = 0; i1 < instances.size(); i1++) {
      newinstances.add(instances.get(i1).toArray(new Feature[] {}));
    }
    return newinstances;
  }

  private void genChunkFeatures(List<Feature> f, String[] tokens, String[] posTags, ArrayList<String> npChunks, int i) {
    
    // Last word OR last two words are in unnamed location list
    int featval=0,count=0;
    int wasInNPChunks=0;
    
    for (String np : npChunks){
      
      if (np.contains(tokens[i])){
        String[] chunkWords = np.split(" ");
        wasInNPChunks=1;
        //Check for last word feature
        String lastWord = np.substring(np.lastIndexOf(' ') + 1);
        if (unnamedLocationsList.contains(lastWord.toLowerCase()))
          featval=1;
        
        // Check for last 2 words
        String[] npSplit = np.split(" ");
        int length =npSplit.length;
        if (length >= 2){
          String last2Words = npSplit[length-2]+" "+npSplit[length-1];
          if (unnamedLocationsList.contains(last2Words.toLowerCase()))
            featval=1;
        }
        addFeature(f, "0_lword_Unloc_" + featval);
        
        // First Letter Capitalized in each word of the chunk
        featval=0;
        
        for (String cw : chunkWords)
        {
          if(MPHCAPbool(cw))
          {
            count+=1;
          }
        }
        
        if(count==chunkWords.length)
        {
          featval = 1;
        }
        
        addFeature(f, "0_lword_FirstCap_" + featval);
      
        // Last word OR last two words are in named organization indicator list 
        featval=0;
        
        if (namedOrgIndicatorList.contains(lastWord))
          featval=1;
      
        addFeature(f, "0_lword_NamedOrgIndicator_" + featval);
        
        // Last word OR last two words are in street list 
        
        featval=0;
        
        if (streetsuffixList.contains(lastWord))
          featval=1;
      
        addFeature(f, "0_lword_StreetSuffix_" + featval);
        
        
        // Word in the chunk is on the <natural features> list
        /*featval=0;
        
        for (String cw : chunkWords)
        {
          if(naturalFeaturesList.contains(cw))
          {
            featval=1;
          }
        }
        
        addFeature(f, "0_lword_natfeat_" + featval); */
        
        // Chunk is on the <toponym> list
        /*featval=0;
    
          if(toponymList.contains(np))
          {
            featval=1;
          }
        
        addFeature(f, "0_lword_natfeat_" + featval);
        */
        
        // 13.  If the chunk appears on <sports> or <newspaper> list or <TV station>
        featval=1;
        
        if(newsPaperList.contains(np) || sportsTeamsList.contains(np))  //|| tvStationList.contains(np)
        {
          featval=0;
        }
      
      addFeature(f, "0_lword_NonLoc_" + featval);
      
  
        
        //12.  If a word in the chunk is on <personal name>, and each word in chunk is upper case…might be preceded by <name title> and sometimes period
        featval=1; count =0;
        for (String cw : chunkWords)
        {
          if(personNamesList.contains(cw) && MPHCAPbool(cw))
          {
            count+=1;
          }
        }

        if(count==chunkWords.length)
        {
          featval = 0;
        }
        
        addFeature(f, "0_lword_Person_" + featval);
        
        
        //2.  Phrase might not start with, but include letters and numerals or word-number(s) [requires word list of numbers] 
        featval=0;
        for (String cw : chunkWords)
        {
          if(numbersList.contains(cw) || cw.matches(".*\\d.*"))
          {
            featval=1;
          }
        }
        addFeature(f, "0_lword_Numerals_" + featval);
        
        

        //Chunk matching <toponym> or <street> <location abbreviation> or <building/business> or <unnamed location> or <named natural feature> list word is preceded by <spatial verb> within 5 words of the phrase
        featval=0;
        
        //if(toponymsList.contains(np) || streetsuffixList.contains(np) ||  unnamedLocationsList.contains(np))
        
          if((spatialVerbsList.contains(i-1) && (i-1)>0 ) || (spatialVerbsList.contains(i-2) && (i-2)>0 ) || (spatialVerbsList.contains(i-3) && (i-3)>0 )||(spatialVerbsList.contains(i-4) && (i-4)>0 )||(spatialVerbsList.contains(i-5) && (i-5)>0 ))
          
          featval=1;
        
      
        addFeature(f, "0_lword_spatialverbs_" + featval);
      
        
        

        //Chunk matching  <toponym> or <street> <location abbreviation> or <building/business> or <unnamed location> or <named natural feature> list word or phrase is preceded within 3 by <spatial preposition indicator>       featval=0;
        featval=0;
        
          if((spatialPrepsList.contains(i-1) && (i-1)>0 ) || (spatialPrepsList.contains(i-2) && (i-2)>0 ) || (spatialPrepsList.contains(i-3) && (i-3)>0 ))
          
          featval=1;
        
      
        addFeature(f, "0_lword_spatialprep_" + featval);
      
      
        //Chunk matching <toponym> or <street> <location abbreviation> or <building/business> or <unnamed location> or <named natural feature> list is preceded within 5 words of <spatial relation> list word [such as north]        featval=0;
        featval=0;
        //if(toponymsList.contains(np) || streetsuffixList.contains(np) || unnamedLocationsList.contains(np))
        
          if((spatialRelationsList.contains(i-1) && (i-1)>0 ) || (spatialRelationsList.contains(i-2) && (i-2)>0 ) || (spatialRelationsList.contains(i-3) && (i-3)>0 )||(spatialRelationsList.contains(i-4) && (i-4)>0 )||(spatialRelationsList.contains(i-5) && (i-5)>0 ))
          
          featval=1;
        
      
        addFeature(f, "0_lword_spatialrelations_" + featval);
      
      
        break;
        
      }
      
    }
    
    if(wasInNPChunks==0)
    {
      addFeature(f, "0_lword_Unloc_" + 0);
      addFeature(f, "0_lword_FirstCap_" + 0);
      addFeature(f, "0_lword_NamedOrgIndicator_" + 0);
      addFeature(f, "0_lword_StreetSuffix_" + 0);
      addFeature(f, "0_lword_NonLoc_" + 0);
      addFeature(f, "0_lword_Person_" + 0);
      addFeature(f, "0_lword_Numerals_" + 0);
      addFeature(f, "0_lword_spatialverbs_" + featval);
      addFeature(f, "0_lword_spatialprep_" + featval);
      addFeature(f, "0_lword_spatialrelations_" + featval);
    }
  
    
  }
  
  /*
   *Parse feature. Label each token with its incoming edge 
   *
   */
  private static void genParseFeatures(List<Feature> f, String[] t_tweet,  Map<String,String> parentEdge,Map<String,ArrayList<String>> childrenEdge ,int i) {
    addFeature(f, "0_cont_Pedge_" + parentEdge.get(t_tweet[i]));
    // If incoming link is *subj* / *obj*
    if (parentEdge.get(t_tweet[i]) != null && parentEdge.get(t_tweet[i]).matches("(.*)subj(.*)|(.*)obj(.*)"))
      addFeature(f, "0_cont_subORobj_" + true);
    else
      addFeature(f, "0_cont_subORobj_" + false);
    // Any of the parent's link is subj/obj 
    // This needs to be coded in the stanford NLP since we dont have access to tree here
    // as of now.
    if ((parentEdge.get(t_tweet[i]) != null && parentEdge.get(t_tweet[i]).matches("(.*)prep(.*)")) || (i-1>0 && parentEdge.get(t_tweet[i-1]) != null && parentEdge.get(t_tweet[i-1]).matches("(.*)prep(.*)")) || (i-2>0 && parentEdge.get(t_tweet[i-2]) != null && parentEdge.get(t_tweet[i-2]).matches("(.*)prep(.*)")) || (i-3>0 && parentEdge.get(t_tweet[i-3]) != null && parentEdge.get(t_tweet[i-3]).matches("(.*)prep(.*)")))
      addFeature(f, "0_cont_prep_" + true);
    else
      addFeature(f, "0_cont_prep_" + false);
  
    if (childrenEdge.containsKey(t_tweet[i]))
      addFeature(f, "0_childrenPOS_" + Arrays.toString(childrenEdge.get(t_tweet[i]).toArray()).replace(" ", ""));
    else
      addFeature(f, "0_childrenPOS_" +"None");
    }
  
  
  /*
   * Wordnet features
   *
   */
  private static void genWordnetFeatures(List<Feature> f, String[] tokens,int i) {
    ArrayList<String> wordlist= new ArrayList<String>();
    Set<String> wordnet= new HashSet<String>();
    String res="false";
    wordlist.add("structure");
    wordlist.add("building");
    wordlist.add("room");
    wordlist.add("factory");
    wordlist.add("office");
    wordlist.add("institution");
    wordlist.add("location");
    wordlist.add("place");
    wordlist.add("position");
    wordlist.add("area");
    wordlist.add("region");

    wordnet = WordnetApi.WordnetFeature(tokens[i]);
    for(String w : wordlist)
    {
     if(wordnet.contains(w)) 
       res="true";
    
    }
    
    addFeature(f, "0_wordnet_" + res);
  
  }
  
  // //////////////////////////////////////////////
  /**
   * In the List OR NOT.
   * 
   * INPUT RAW TOKENS OUTPUT BINARY VALUE YES OR NO.
   * 
   * @param f
   * @param t_tweet
   * @param i
   */
  // prep-2.prep-1
  private static void genLookupListFeatures(List<Feature> f, String[] t_data, int i ) {

    //System.out.println(t_data[i]);
      addFeature(f, "Presence_LUnnamedLocation_" + unnamedLocationsList.contains(TOKLW(t_data[i])));
      addFeature(f, "Presence_LPersonNames_" + personNamesList.contains(TOKLW(t_data[i])));
      addFeature(f, "Presence_LNamedOrganization_" + namedOrganizationsList.contains(TOKLW(t_data[i])));
      addFeature(f, "Presence_LToponym_" + toponymsList.contains(TOKLW(t_data[i])));

      // Partial presence ( a part of the location word contains token

      addFeature(f, "Presence_LUnnamedLocationPartial_" + containsPartial(unnamedLocationsList,TOKLW(t_data[i])));
      addFeature(f, "Presence_LPersonNamesPartial_" + containsPartial(personNamesList,TOKLW(t_data[i])));
      addFeature(f, "Presence_LNamedOrganizationPartial_" + containsPartial(namedOrganizationsList,TOKLW(t_data[i])));
      
      //System.out.println("Presence_LUnnamedLocation_" + unnamedLocationsList.contains(TOKLW(t_data[i])));
  }
  
  
  private static void genBrownClusterFeatures(List<Feature> f, String[] t_data, int i ) throws IOException {

      if(clusters.containsKey(TOKLW(t_data[i])) && clusters.get(TOKLW(t_data[i])) != null)
      {
        //System.out.println(clusters.get(TOKLW(t_data[i])));
        addFeature(f, "BrownCluster_" + clusters.get(TOKLW(t_data[i])));
      }
      
      else
      addFeature(f, "BrownCluster_-1");
      
  }
  
  // lemma, lower, POS feature
  private static void genLemmaFeatures(List<Feature> f, String[] t_data, String[] lemma , String[] POS,int i ) {
    
    //System.out.println(t_data[i]);
      addFeature(f,"lemma_"+lemma[i]);
      /*
      if((i-1)>0 && (i-2)>0 && (i-3)>0)
      addFeature(f,"POS_"+POS[i-1]+POS[i-2]+ POS[i-3]);
      */
      addFeature(f,"lower_"+TOKLW(t_data[i]));
      
    
      //System.out.println("Presence_LUnnamedLocation_" + unnamedLocationsList.contains(TOKLW(t_data[i])));
  }

  /**
   * COUNTRY GAZ EXISTENCE
   * 
   * @param f
   * @param f_country
   * @param i
   */
  

  /**
   * POINT POS FOR EACH SURROUNDING WORD POS SEQUENCE
   * 
   * @param f
   * @param f_pos
   * @param i
   */
  // pos.seq-3-1.seq+1+3
  private static void genPosFeatures(List<Feature> f, String[] f_pos, int i) {
    int t_length = f_pos.length;
    // f5 PART OF SPEECH

    // CURRENT WORD
    addFeature(f, "0.pos." + f_pos[i]);

    String posleft = "", posright = "";
    if (i - 4 >= 0) {
      addFeature(f, "-4.pos." + f_pos[i - 4]);
       posleft += f_pos[i - 4];
    }
    else
      addFeature(f, "-4.pos." + "false");
    if (i - 3 >= 0) {
      addFeature(f, "-3.pos." + f_pos[i - 3]);
       posleft += f_pos[i - 3];
    }
    else
      addFeature(f, "-3.pos." + "false");
  
    if (i - 2 >= 0) {
       addFeature(f, "-2.pos." + f_pos[i - 2]);
      posleft += f_pos[i - 2];
    }
    else
      addFeature(f, "-2.pos." + "false");
    if (i - 1 >= 0) {
      addFeature(f, "-1.pos." + f_pos[i - 1]);
      posleft += f_pos[i - 1];
    }
    else
      addFeature(f, "-1.pos." + "false");
    if (i + 1 <= t_length - 1) {
      addFeature(f, "+1.pos." + f_pos[i + 1]);
      posright += f_pos[i + 1];
    }
    else
      addFeature(f, "+1.pos." + "false");
    if (i + 2 <= t_length - 1) {
       addFeature(f, "+2.pos." + f_pos[i + 2]);
      posright += f_pos[i + 2];
    }
    else
      addFeature(f, "+2.pos." + "false");
    if (i + 3 <= t_length - 1) {
       addFeature(f, "+3.pos." + f_pos[i + 3]);
       posright += f_pos[i + 3];
    }
    else
      addFeature(f, "+3.pos." + "false");
    if (i + 4 <= t_length - 1) {
       addFeature(f, "+4.pos." + f_pos[i + 4]);
       posright += f_pos[i + 4];
    }
    else
      addFeature(f, "+4.pos." + "false");
    
    addFeature(f, "-pos_seq_" + posleft);
    addFeature(f, "+pos_seq_" + posright);

  }

  /**
   * CAPITALIZATION SEQUENCE POINT CAPs OF SURROUNDING WORDS CAP SEQUENCEs
   * 
   * @param f
   * @param t_tweet
   * @param i
   */
  // cap.seq-3-1.seq+1+3
  private static void genCapFeatures(List<Feature> f, String[] t_tweet, int i) {
    int t_length = t_tweet.length;

    // CURRENT WORD
    addFeature(f, "0_mph_cap_" + MPHCAP(t_tweet[i]));

    String left = "", right = "";
    if (i - 4 >= 0) {
      // addFeature(f, "-4_mph_cap_" + MPHCAP(t_tweet[i - 4]));
      // left += MPHCAP(t_tweet[i - 4]);
    }
    if (i - 3 >= 0) {
      addFeature(f, "-3_mph_cap_" + MPHCAP(t_tweet[i - 3]));
      // left += MPHCAP(t_tweet[i - 3]);
    }
    else
      addFeature(f, "-3_mph_cap_" + "false");
    if (i - 2 >= 0) {
      addFeature(f, "-2_mph_cap_" + MPHCAP(t_tweet[i - 2]));
      left += MPHCAP(t_tweet[i - 2]);
    }
    else
      addFeature(f, "-2_mph_cap_" + "false");
    if (i - 1 >= 0) {
      addFeature(f, "-1_mph_cap_" + MPHCAP(t_tweet[i - 1]));
      left += MPHCAP(t_tweet[i - 1]) + "::";
    }
    else
      addFeature(f, "-1_mph_cap_" + "false");
    if (i + 1 <= t_length - 1) {
      addFeature(f, "+1_mph_cap_" + MPHCAP(t_tweet[i + 1]));
      right += MPHCAP(t_tweet[i + 1]);
    }
    else
      addFeature(f, "+1_mph_cap_" + "false");
    if (i + 2 <= t_length - 1) {
      addFeature(f, "+2_mph_cap_" + MPHCAP(t_tweet[i + 2]));
      right += MPHCAP(t_tweet[i + 2]);
    }
    else
      addFeature(f, "+2_mph_cap_" + "false");
    if (i + 3 <= t_length - 1) {
      addFeature(f, "+3_mph_cap_" + MPHCAP(t_tweet[i + 3]));
      // right += MPHCAP(t_tweet[i + 3]);
    }
    else
      addFeature(f, "+3_mph_cap_" + "false");
    if (i + 4 <= t_length - 1) {
      // addFeature(f, "+4_mph_cap_" + MPHCAP(t_tweet[i + 4]));
      // right += MPHCAP(t_tweet[i + 4]);
    }
    
      
    addFeature(f, "-_mph_cap_seq_" + left);
    addFeature(f, "+_mph_cap_seq_" + right);
    addFeature(f, "-+_mph_cap_seq_" + left + right);

  }

  /**
   * CONTEXT WORD (LEMMA) EXISTENCE The bag of words feature, and position
   * appearance feature together. 1. Each lemma is added in bag of context
   * words 2. Each position has an presence feature for determining the
   * existence of the window position.
   * 
   * @param f
   *              : Feature list
   * @param lemmat_tweet
   *              : lemmas of the tweet,
   * @param i
   *              : position of the current word
   */

  /**
   * CAPITALIZATION
   * 
   * @param string
   * @return boolean
   */
  private static String MPHCAP(String string) {

    boolean a = Character.isUpperCase(string.charAt(0));
    return Boolean.toString(a);
  }
  
  private static Boolean MPHCAPbool(String string) {

    boolean a = Character.isUpperCase(string.charAt(0));
    return a;
  }

  /**
   * CONVERT TO LOWER TYPE Input the lemma, 1. Run tokentype() to convert to
   * token 2. lowercase and deaccent the lemma.
   * 
   * @param lemmastring
   * @return
   */
  private static String TOKLW(String lemmastring) {

    lemmastring = StringUtil.getDeAccentLoweredString(tokentype(lemmastring));
    return lemmastring;
  }





  // ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // TOOLS
  // //////////////////////////////////
  /**
   * JUDGE EMPTY OF AN ARRAY.
   * 
   * @param array
   * @return
   */
  static boolean EmptyArray(String[] array) {
    if (array.length < 2)
      if (array[0].equals(""))
        return true;
    return false;
  }

  // ////////////////////////////////////////////////////////////////////////////////
  // HELPER FOR FEATURE VECTOR
  // /////////////////////////////////////////
  static StringBuilder sb = new StringBuilder();

  /**
   * helper for building feature vector. sb stores the features on a line,
   * and this func is used to initialize the sb, aka, clear the builder.
   */
  private static void initialFeatureWriter() {
    sb = new StringBuilder();
  }

  private static void append(String featurestring) {

    if (sb.length() > 0)
      sb.append("\t");
    sb.append(featurestring);
  }

  static String emit() {
    return sb.append("\n").toString();
  }

  private static void addFeature(List<Feature> features, String string) {

    features.add(new Feature(string));
  }

  // ////////////////////////////////////////////////////////////////////////////////////
  // GETTER AND SETTERS /////

  public HashSet<String> getPreposition() {
    return preposition;
  }

  public void setPreposition(HashSet<String> preposition) {
    this.preposition = preposition;
  }

  public HashSet<String> getCountries() {
    return countries;
  }

  public void setCountries(HashSet<String> countries) {
    this.countries = countries;
  }

  public IndexSupportedTrie getTrie() {
    return trie;
  }

  public void setTrie(IndexSupportedTrie trie) {
    this.trie = trie;
  }



public static String ParseFineLine(String line, HashMap<String, String> tagdata )
{
  String data = line.replaceAll("\\<.*?>","");
  //System.out.println(data);
  String reg= "<.*?>(.*?)</.*?>";
  
  Pattern p = Pattern.compile(reg);
  Matcher m = p.matcher(line);
    while(m.find())
    {
      String tag = m.group(0).split(">")[0].replace("<","");
    
        String[] s1 = m.group(1).split(" ");
       //System.out.println(tag + ' '+s1[0]);
        
      int i=0;
        for (String w : s1){
          if (w.equals("Telefonica"))
            System.err.println(tag+","+m.group(1));
          if (i==0)
            tagdata.put(w,"B-"+tag);
          else
            tagdata.put(w,"I-"+tag);
          i++;
        }
       // System.out.println(tagdata.get(s1[0]));
    }
    return data;
}
  
//
//public static String[] DataTokenizer(String data)
//{
//  String[] TokenizedData = data.split(" ");
//  
//    return TokenizedData;
//
//} 

/*
 * Assumes tokens contains the tags. 
 * eg: ["I","am","in","<Toponym>","New","York","</Toponym>","."];
 */
public static String[] TokentoTag(String [] Tokens,ArrayList<String> newTokens){
  
  String[] Tags = new String [Tokens.length] ;
  String startReg= "<.*?>"; 
  String endReg = "</.*?>";
  String curr_tag = "O";
  //ArrayList<String> newTokens = new ArrayList<String>();
  int i=0;
  for (String w: Tokens){
    
    if (w.matches(startReg) && !w.matches(endReg)){
      curr_tag=w.replace("<","").replace(">", "");
      continue;
    }
    if (w.matches(endReg)){
      curr_tag="O";
      continue;
      
    }
    if(w.equals("<") || w.equals(">") || w.equals("\\") || w.equals("<\\") ){
      System.err.println(w);
    }
    newTokens.add(w);
    Tags[i] = curr_tag;
    i++;
    
  }
  assert (newTokens.size()==Tokens.length);
  return Tags;
}

public static String[] TokentoTag(String[] Tokens, HashMap Tagdata)
{
  String[] Tags = new String [Tokens.length] ;
  
  Integer i =0;
  for (String token : Tokens)
    {
      
      if (Tagdata.get(token)== null)
        Tags[i]= "O";
      else
        Tags[i]= (String) Tagdata.get(token);
      
      i++;
    }
    
  
    return Tags;

} 

public static String[] TokentoBIOTag(String [] Tokens,ArrayList<String> newTokens){
  
  String[] Tags = new String [Tokens.length] ;
  String startReg= "<.*?>"; 
  String endReg = "</.*?>";
  String curr_tag = "O";
  int start=0;
  //ArrayList<String> newTokens = new ArrayList<String>();
  int i=0;
  for (String w: Tokens){
    
    if (w.matches(startReg) && !w.matches(endReg)){
      curr_tag=w.replace("<","").replace(">", "");
      continue;
    }
    if (w.matches(endReg)){
      curr_tag="O";
      start=0; 
      continue;
      
    }
    if(w.equals("<") || w.equals(">") || w.equals("\\") || w.equals("<\\") ){
      System.err.println(w);
    }
    newTokens.add(w);
    
    if(curr_tag.equals("O"))
      start=0;
    else{
      if (start==0)
        curr_tag = "B-"+curr_tag ;
      else if (start==1)
        curr_tag = curr_tag.replace("B-", "I-") ;
      start++;
    }
    
    Tags[i] = curr_tag;
    i++;
    
  }
  assert (newTokens.size()==Tokens.length);
  return Tags;
}



/**
 * CONVERT TO TYPE Naively decide the tweet token type, url, or hashtag,
 * or metion, or number. Or it's not any of them, just return it's
 * original string.
 * 
 * @param token
 * @return
 */
public static String tokentype(String token) {
  // lower cased word.
  String ltoken = StringUtil.getDeAccentLoweredString(token.trim());

  if (ltoken.startsWith("http:") || ltoken.startsWith("www:")) {
    ltoken = "[http]";
  } else if (ltoken.startsWith("@") || ltoken.startsWith("#")) {
    if (ltoken.length() > 1) {
      ltoken = ltoken.substring(1);
    }
  }
  try {
    Double.parseDouble(ltoken);
    ltoken = "[num]";
  } catch (NumberFormatException e) {
  }

  return ltoken;
}
} 




