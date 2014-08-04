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
package edu.cmu.geolocator.nlp.StanfordCoreTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
public class StanfordNLP {
  
  StanfordCoreNLP pipeline;
  StanfordCoreNLP pipelineTags;
  Annotation document;

  public StanfordNLP()
  {
    Properties props = new Properties();
        props.put("annotators", "tokenize,ssplit,parse,pos,lemma");
        pipeline = new StanfordCoreNLP(props);
        props.clear();
        props.put("tokenize.whitespace", "true");
        props.put("annotators", "tokenize,ssplit,parse,pos,lemma");
        pipelineTags = new  StanfordCoreNLP(props);
        document = null;
       
  }
  public void DoAll(String[] tokenizedData, String[] POSTags, String[] LEMMA,Map<String,String> parentEdge,
      Map<String,ArrayList<String>> childrenEdge)
  {
    StringBuilder builder = new StringBuilder();
    for(String s : tokenizedData) {
        builder.append(s);
        builder.append(" ");
    }
    DoAll(builder.toString(), tokenizedData, POSTags, LEMMA, parentEdge,childrenEdge);
    
  }
   
  public void DoAll(String data, String[] TokenizedData, String[] POSTags, String[] LEMMA, Map<String,String> parentEdge,
      Map<String,ArrayList<String>> childrenEdge)
  {
    //if(document == null)
    {
      document = new Annotation(data);
        pipelineTags.annotate(document);
    }
      int i=0;
    List<CoreLabel> tokens = document.get(TokensAnnotation.class);
  
    for (CoreLabel token : tokens) {
      
            String wPOS = token.get(PartOfSpeechAnnotation.class);
            String wNER = token.get(NamedEntityTagAnnotation.class);
            String wLEMMA = token.get(LemmaAnnotation.class)  ;
            TokenizedData[i]= token.toString();
            POSTags[i]= wPOS;
            LEMMA[i]= wLEMMA;
        i++;
    }
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    if(sentences.size()>0){
    SemanticGraph tree = sentences.get(0).get(BasicDependenciesAnnotation.class);
    //System.out.println(data+"\n"+tree.toString()+"\n");
    createEdgeMap(tree,parentEdge,childrenEdge);
    }
        
  }
  // token -> edge name mapping. Each node has a single incoming edge
    public void createEdgeMap(SemanticGraph tree,Map<String,String> edgeMap, Map<String,ArrayList<String>> childMap){
      Set<SemanticGraphEdge> tmp = tree.getEdgeSet();
      // = new HashMap<String,String>();
      for(SemanticGraphEdge edge : tmp ){
        //System.out.println(edge.toString()+"("+edge.getSource().toString()+","+edge.getTarget().word()+")");
        edgeMap.put(edge.getTarget().word(), edge.toString());
        if(!childMap.containsKey(edge.getSource().word()))
          childMap.put(edge.getSource().word(), new ArrayList<String>());
        childMap.get(edge.getSource().word()).add(edge.getTarget().tag());
      
        
      }
      //System.out.println();

    }
  public String[] POSTagger(String data)
  {
    
    String[] POSTags = null;
    int i=0;
    List<CoreLabel> tokens =  document.get(TokensAnnotation.class);
    for (CoreLabel token : tokens) {
            String wPOS = token.get(PartOfSpeechAnnotation.class);
            POSTags[i]= wPOS;
            i++;
    }
           
    return POSTags;
        
  }
  
  public String[] NERTagger(String data)
  {
    String[] NERTags = null;
    int i=0;
    List<CoreLabel> tokens = Tokenizer( data);
    for (CoreLabel token : tokens) {
            String word = token.get(NamedEntityTagAnnotation.class);
            NERTags[i]= word;
            i++;
    }
           
    return NERTags;
        
  }
  
  
  
  public List<CoreLabel> Tokenizer(String data)
  {
    document = new Annotation(data);
      pipeline.annotate(document);
     List<CoreLabel> tokens = document.get(TokensAnnotation.class);
    
     return tokens;
  }
  
  public String[] StringTokenizer(String data)
  {
    document = new Annotation(data);
      pipeline.annotate(document);
    String[] Tokens=null;
     List<CoreLabel> tokens = document.get(TokensAnnotation.class);
     Tokens= new String[tokens.size()];
     int i =0;
     for(CoreLabel t : tokens)
     {
       String tokenString = t.toString();
       Tokens[i] = tokenString;
       i++;
     }
     return Tokens;
  }
  
  public String[] Lemmatizer(String data)
  {
    String[] Lemma = new String[data.length()];
    
    int i=0;
    List<CoreLabel> tokens = Tokenizer( data);
    for (CoreLabel token : tokens) {
            String word = token.get(LemmaAnnotation.class);
            Lemma[i]= word;
            i++;
    }
         
    return Lemma;
  }
  
  
  
  public ArrayList<String> NPChunker(String[] Tokens, String[] POSTags) throws IOException
  {
    ArrayList<String> NP= new ArrayList<String>();
    int length = Tokens.length;
    int start =0;
    String N="";
    for (int i=0;i<length;i++)
    {
      
      if (POSTags[i].contains("JJ"))
      { 
        start=1;
        N = N+" "+Tokens[i];
        //NP.add(N);
      }
      
      else if(POSTags[i].contains("NN")&& start == 1)
      {
        N = N + " "+Tokens[i];
      
        //NP.add(N);
      }
      
      else if(POSTags[i].contains("NN")&& start == 0)
      {
        start =1;
      
        N = N + " "+Tokens[i];
      }
      else 
      {
        if(start==1)
          NP.add(N.trim());
        start=0;
        N= "";
      }
    }
    
    if(!N.trim().equals(""))
      NP.add(N.trim());

//    BufferedWriter r = new BufferedWriter(new FileWriter("res/lists/Chunks.txt",true));
//    r.write(NP.toString());
//    r.close();
    return NP;
  }
  
  
  public static void main(String[] args) throws IOException {

    runSimple();
    
  }
  public static void runSimple() throws IOException{
     String data = "Pythons slumber on the gnarled roots of errie mangrove forests";
    
    StanfordNLP snlp = new StanfordNLP();
    int nSize=16;
    String[] r1 =new String[nSize], r2 = new String[nSize], r3 = new String[nSize], r4 = new String[nSize];
    Map<String,String> parentEdge = new HashMap<String,String>();
    Map<String,ArrayList<String>> childrenEdge = new HashMap<String,ArrayList<String>>();
    ArrayList<String> NP = null;
  
    snlp.DoAll(data, r1, r2, r3, parentEdge,childrenEdge);
    for(String word : childrenEdge.keySet())
      System.out.println(word+"-is parent of-"+childrenEdge.get(word).toString());
    for (String r : r3) {
      if (r==null)
        break;
      System.out.println(r + " ");
    }

    NP = snlp.NPChunker(r1, r2);
    for (String np : NP) {
      System.out.println(np);
    
    }
    

  }
  public static void runFromFile() throws IOException{
    // String data = "Pythons slumber on the gnarled roots of errie mangrove forests";
    
    StanfordNLP snlp = new StanfordNLP();
    int nSize=16;
    String[] r1 =new String[nSize], r2 = new String[nSize], r3 = new String[nSize], r4 = new String[nSize];
    Map<String,String> parentEdge = new HashMap<String,String>();
    Map<String,ArrayList<String>> childrenEdge = new HashMap<String,ArrayList<String>>();
    ArrayList<String> NP = null;
    String filename = "E:/CMU/DataMining/Data/Runs/ErrorAnalysis.txt";
    System.err.println("Reading file:"+filename);
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    BufferedWriter writer = new BufferedWriter(new FileWriter("E:/CMU/DataMining/tokenized.txt"));
    String data = null;
    
    while ((data = reader.readLine()) != null) {
      //System.out.println(data);
      int len= data.length();
      r1= new String[len];
      r2= new String[len];
      r3= new String[len];
      r4= new String[len];
      snlp.DoAll(data, r1, r2, r3,parentEdge,childrenEdge);
      for (String r : r1) {
        if(r==null)
          break;
        //System.out.println(r);
        writer.write(r+" ");
      }
      writer.write("\n");
    }
    reader.close();
    writer.close();
    /*
    snlp.DoAll(data, r1, r2, r3, r4, parentEdge);

    for (String r : r3) {
      System.out.println(r + " ");
    }

    NP = snlp.NPChunker(r1, r2);
    for (String np : NP) {
      System.out.println(np);
    
    }
    */

  }
}


