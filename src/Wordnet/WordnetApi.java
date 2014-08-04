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
package Wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
/*
import edu.cmu.lti.deiis.hw5.constants.StopWords;
import edu.cmu.lti.deiis.hw5.constants.WordNetConstants;
import edu.cmu.lti.qalab.utils.Utils;*/
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;

public class WordnetApi {
	private static Map<String, Set<String>> cache = null;
	//private static DistributionalSimilarity ds = null;
	private static Set<String> wordNetStopWords = null;
	
	public class WordNetConstants {

		public static final String DIR_ATT="wordnet.database.dir";

		public static final String DIR_PATH="wordnet";

		public static final double WORDNET_SYNONYM_WEIGHT=1;

		public static final int WORDNET_HYPERNYM_DEPTH=3;

		}

	public static String stopLine = "entity,abstraction,concept,idea,abstract entity,act,creation,activity,deed,unit,action,whole,thing,system,physical entity,material,set,collection,group,object,physical object,event";
	static {

		cache = new HashMap<String, Set<String>>();
	System.setProperty(WordNetConstants.DIR_ATT, WordNetConstants.DIR_PATH);

	}
	
	public static Set<String> WordnetFeature(String word) {
		
		boolean result;
		Set<String> list= new HashSet<String>();
		Set<String> cachedWords = cache.get(word);
		NounSynset nounSynset;
		NounSynset[] hyponyms;
		NounSynset[] hypernyms;
		WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
        for (int i = 0; i < synsets.length; i++) {
			nounSynset = (NounSynset) (synsets[i]);
			hyponyms = nounSynset.getHyponyms();
			hypernyms = nounSynset.getHypernyms();
			
			for (int j = 0; j < hyponyms.length; j++) {
				NounSynset hpn = hyponyms[j];
				String[] wordforms=hpn.getWordForms();
				
				for(int f=0;f<wordforms.length;f++)
					{list.add(wordforms[f].toLowerCase());
					Collections.addAll(list, hpn.getDefinition().toLowerCase().replace("(","").replace(")","").replace(";","").split(" "));
				 //System.out.println(hpn.getDefinition());
					}
			
			}

			for (int j = 0; j < hypernyms.length; j++) {
				NounSynset hpn = hypernyms[j];
				String[] wordforms=hpn.getWordForms();
				for(int f=0;f<wordforms.length;f++)
				{	list.add(wordforms[f].toLowerCase());
				Collections.addAll(list, hpn.getDefinition().toLowerCase().split(" "));
				// System.out.println(hpn.getDefinition());
				}
			}
      
			
	    }

        return list;
	}

	public static  Set<String> checkSet( Set<String> set){
		if (set!=null)
			return set;
		else
			return new  HashSet<String>();
	}
	

	
	public static void main(String args[]) {
		
		Set<String> res=WordnetFeature("Change");
		
		//Set<String> hyponymList = getWordHyponyms("apartment", null);
		// Set<String> hyponymList=getVerbHyponyms("transform",null);
		// Set<String> hyponymList=getAdjectiveHyponyms("beautiful",null);
		// Set<String> hyponymList =getAdverbHyponyms("swiftly", null);
		System.out.println("--------------------hyponymList-------------");

		for ( String r : res)
		{
			System.out.println(r);
		}
		}
	
}