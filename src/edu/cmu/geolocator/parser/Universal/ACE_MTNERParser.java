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
package edu.cmu.geolocator.parser.Universal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.geolocator.io.PipeLineAnnotate;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.ACE_En_FeatureGenerator;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.minorthird.classify.ClassLabel;
import edu.cmu.minorthird.classify.Example;
import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.classify.MutableInstance;
import edu.cmu.minorthird.classify.sequential.SequenceClassifier;
import edu.cmu.minorthird.util.IOUtil;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class ACE_MTNERParser {

	SequenceClassifier model;

	ACE_En_FeatureGenerator fg;

	/**
	 * Default protected constructor
	 * 
	 * @throws IOException
	 * 
	 */

	private static ACE_MTNERParser fineenglishparser;

	public static ACE_MTNERParser getInstance() {

		if (fineenglishparser == null) {
			try {
				fineenglishparser = new ACE_MTNERParser("ACE-CRF.model120", new ACE_En_FeatureGenerator("res/"));
			} catch (Exception e) {
				System.err.println("Spanish NER Model File not found");
				e.printStackTrace();
			}
		}
		return fineenglishparser;
	}

	ACE_MTNERParser(String modelname, ACE_En_FeatureGenerator featureg) {
		try {
			model = (SequenceClassifier) IOUtil.loadSerialized(new java.io.File(modelname));
			this.fg = featureg;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Identifying location names by NER
	Example[] examples;

	Sentence tweetSentence;

	/**
	 * extract the entities, and put them into the tweet. return them also.
	 */
	public List<LocEntityAnnotation> parse(Tweet tweet) {
		tweetSentence = tweet.getSentence();
		// ///////////////////
		PipeLineAnnotate pla = new PipeLineAnnotate(tweetSentence.getSentenceString());
		List<CoreMap> NLPsents = pla.getSentences();

		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for (CoreMap NLPsentence : NLPsents) {

			Sentence mysentence = new Sentence(NLPsentence.get(TextAnnotation.class));

			List<CoreLabel> tokens = NLPsentence.get(TokensAnnotation.class);

			int j = 0;
			Token[] tokensArray = new Token[tokens.size()];
			for (CoreLabel token : tokens) {
				int pos = token.beginPosition();
				int end = token.endPosition();
				String word = token.get(TextAnnotation.class);
				Token myToken = new Token(word, j + "", j);
				myToken.setLemma(token.lemma());
				myToken.setStart(pos);
				myToken.setEnd(end);
				tokensArray[j] = myToken;
				j++;

			}
			mysentence.setTokens(tokensArray);
			mysentence.setStart(tokensArray[0].getStart());
			mysentence.setEnd(tokensArray[tokensArray.length - 1].getEnd());
			sentences.add(mysentence);

		}

		// ////////////////////
		
		List<LocEntityAnnotation> locs = new ArrayList<LocEntityAnnotation>();

		int sentid = 0;
		for (Sentence sent : sentences) {
			Example[] exp = new Example[sent.getTokens().length];
			List<ArrayList<Feature>> tweetfeatures = fg.extractFeature(sent);
			for (int tokid = 0; tokid < sent.getTokens().length; tokid++) {
				ClassLabel lab = new ClassLabel(sent.getTokens()[tokid].getNE() == null ? "" : sent.getTokens()[tokid].getNE());
				MutableInstance inst = new MutableInstance("ACE-NER", sentid + "-" + tokid);
				for (int j = 0; j < tweetfeatures.get(0).size(); j++) {
					inst.addBinary(tweetfeatures.get(tokid).get(j));
				}
				exp[tokid] = new Example(inst, lab);
			}
			ClassLabel[] resultlabels = model.classification(exp);
			for (int tokid = 0; tokid < sent.getTokens().length; tokid++) {
				sent.getTokens()[tokid].setNEprediction(resultlabels[tokid].bestClassName());
				//System.out.println(resultlabels[tokid].bestClassName() + " "+sent.getTokens()[tokid].getToken());
			}
			
			/**
			 * rewrite the loc-entity generation, to support positions.
			 */
			int startpos = -1, endpos = -1;
			String current = "O", previous = "O";
			for (int k = 0; k < resultlabels.length; k++) {
				if (k > 0)
					previous = current;
				current = resultlabels[k].bestClassName();
				if (current.equals("O"))
					if (previous.equals("O"))
						continue;
					else {
						endpos = k - 1;
						// System.out.println(startpos + " " + endpos + " " +
						// previous);
						Token[] t = new Token[endpos - startpos + 1];
						for (int i = startpos; i <= endpos; i++) {
							t[i - startpos] = sent.getTokens()[i].setNE(previous);
						}
						LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, previous, t);

						// set the probability of the NE type
						// This may be changed later.
						le.setNETypeProb(0.95);

						locs.add(le);
					}
				else if (previous.equals("O"))
					startpos = k;
				else
					endpos = k;

			}
			
			
			sentid++;
		}
		// ///////////////////
		return locs;
	}

	public static void main(String argv[]) throws IOException {

		String sss = "I live in Pittsburgh. I am going to new york.";
		Tweet t = new Tweet(sss);
		BufferedReader s = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
		System.out.println(">");
		while (true) {
			String ss = s.readLine();
			if (ss.length() == 0)
				continue;
			t.setSentence(ss);
			double stime = System.currentTimeMillis();
			List<LocEntityAnnotation> matches = ParserFactory.getACENERParser().parse(t);
			if (matches == null)
				System.out.println("No results. ");
			double etime = System.currentTimeMillis();
			System.out.println(matches);
			System.out.println(etime - stime + "\n>");
		}
	}

}
