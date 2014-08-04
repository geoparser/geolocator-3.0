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
package edu.cmu.geolocator.nlp.pos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

/*
 * The POSTagger from Juan. It's the one that we are using right now.
 * The tag set is the universal tag set that is created by google( one of the author is from LTI).
 */
public class ESOpenNLPPOSTagger {

	static final POSModel model= new POSModelLoader().load(new File("res/es/opennlp-es-pos-maxent-pos-es-1.model"));
	static POSTaggerME tagger= new POSTaggerME(model);;
	
	
	//  method for tagging a line. The line should be tokenized in advance.
	
	public static List<String> tag( String[] tokenizedline){
		
        return Arrays.asList(tagger.tag(tokenizedline));

	}
	
	public static void main(String argv[]) throws IOException{
		
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
	    perfMon.start();
	    String line;
	    String input="A-3 carril lento cerrado (cono) (verde) #valencia #valencia #a3 #buÐol #trafico http://bit.ly/chaKTz";
			    // ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));
	  //  while ((line = lineStream.read()) != null) {
	
	    	//use modified twokenizer for spanish
	    	//tags and tokenized items are seperate, to ensure conviniency.
	    	List<String> tokenizedline	=  EuroLangTwokenizer.tokenize(input);
	    	List<String> tags 	= ESOpenNLPPOSTagger.tag(tokenizedline.toArray(new String[]{}));
	
	    	for(int i=0;i<tags.size();i++)
	    		System.out.println(tokenizedline.get(i)+" "+tags.get(i));
	    	
	    	
	//    	POSSample sample = new POSSample(a, tags);
	 //       System.out.println(sample.toString());

	        perfMon.incrementCounter();
	  //  }
	    perfMon.stopAndPrintFinalResult();
	}
	
}
