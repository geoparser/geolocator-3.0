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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


/**
 * The Stanford tagger is 60 ms/30toks. 35ms/17toks, -> 2ms/1tok.
 */
public class ENStanfodPOSTagger {
	public MaxentTagger tagger;
	public ENStanfodPOSTagger(String f) {
		tagger = new MaxentTagger(f);
	}

	public MaxentTagger getTagger(){
		return tagger;
	}
	public List<String> tag(List<String> tokens) {
		StringBuilder tokenstring = new StringBuilder();
		for (String tok : tokens) {
			tokenstring.append(tok).append(" ");
		}
		String tagged = tagger.tagString(tokenstring.toString());
		// System.out.println(tagged);
		String[] toktags = tagged.split("[ ]");
		List<String> tags = new ArrayList<String>(toktags.length);
		for (String toktag : toktags) {
			tags.add(toktag.split("[_]")[1]);
		}
		return tags;

	}

	public static void main(String avp[]) throws IOException {
		ENStanfodPOSTagger entagger = new ENStanfodPOSTagger("res/en/wsj-0-18-bidirectional-distsim.tagger");
		
		String sample = "40.#twitterafterdark she works at the ford plant! http://t.co/ruaip2aa #pussy #booty|org";
		List<String> tagged = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
		String inputStr = br.readLine();
		while (inputStr != "") {
			long start = System.currentTimeMillis();
//			for (int i = 0; i < 1000; i++) 
			{
				List<String> tokens = EuroLangTwokenizer.tokenize(inputStr);
				 System.out.println(tokens.toString());
				// The tagged string
				tagged = entagger.tag(tokens);
			}
			long end = System.currentTimeMillis();
			System.out.println((end - start) + tagged.toString());
			inputStr = br.readLine();
		}
	}
}
