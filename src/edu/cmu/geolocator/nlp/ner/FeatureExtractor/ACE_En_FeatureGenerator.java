/**
 * 
 * Copyright (c) 2012 - 2014 Carnegie Mellon University
 * 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 * 
 * @author Wei Zhang,  Language Technology Institute, School of Computer Science, Carnegie-Mellon University.
 * email: wei.zhang@cs.cmu.edu
 *
 * 
 */
package edu.cmu.geolocator.nlp.ner.FeatureExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.cmu.geolocator.common.StringUtil;
import edu.cmu.geolocator.model.Document;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.geolocator.resource.dictionary.Dictionary;
import edu.cmu.geolocator.resource.dictionary.Dictionary.DicType;
import edu.cmu.geolocator.resource.gazindexing.Index;
import edu.cmu.minorthird.classify.Feature;

public class ACE_En_FeatureGenerator {

	public ACE_En_FeatureGenerator() {
	};

	HashSet<String> preposition;

	@SuppressWarnings("unchecked")
	public ACE_En_FeatureGenerator(String resourcepath) {

		try {
			Dictionary prepdict = Dictionary.getSetFromListFile(resourcepath + "en/prepositions.txt", true, true);
			preposition = (HashSet<String>) prepdict.getDic(DicType.SET);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static void main(String argv[]) throws IOException, InterruptedException {

		Document d = null;

		ACE_En_FeatureGenerator fgen = new ACE_En_FeatureGenerator("res/");

		for (Sentence sent : d.getP().get(0).getSentences()) {
			List<ArrayList<Feature>> tweetfeatures = fgen.extractFeature(sent);
		}

	}

	/**
	 * MAIN FUNCTION FOR EXTRACTIN FEATURES
	 * 
	 * @param t_tweet
	 * @param trie
	 * @param postags
	 * @return FEATURE LISTS
	 */

	public ArrayList<ArrayList<Feature>> extractFeature(Sentence sent) {

		int len = sent.tokenLength();

		ArrayList<ArrayList<Feature>> instances = new ArrayList<ArrayList<Feature>>(len);
		ArrayList<Feature> f = new ArrayList<Feature>();

		// normalize tweet norm_tweet
		for (int i = 0; i < len; i++)
			sent.getTokens()[i].setNorm(StringUtil.getDeAccentLoweredString(tokentype(sent.getTokens()[i].getToken())));

		// String[] f_pos = postags.toArray(new String[] {});

		// f_gaz originally. filled in inGaz Field in token. check norm_tweet
		// field.
		// boolean[] f_gaz =
		// gazTag(tweetSentence, this.index);

		// use norm_tweet field to tag countries. don't remove f_country,
		// because it's not a type in
		// token.
		// boolean[] f_country = countryTag(tweetSentence);

		for (int i = 0; i < len; i++) {
			// clear feature list for this loop
			f = new ArrayList<Feature>();
			// /////////////////////////////// MORPH FEATURES
			// use lemma_tweet to get token features.
			genTokenFeatures(f, sent, i);
			// ////////////////////////////// SEMANTIC FEATURES
			genPosFeatures(f, sent, i);
			// ////////////////////////////////// GAZ AND DICT LOOK UP
			// genGazFeatures(f, sent, i);
			// f7: STREET SUFFIX
			// f8 PREPOSITION

			// genCountryFeatures(f, f_country, i);

			// f10 directions

			// FEATURES are not stored in tweetsentence in advance. Those are
			// generated in those features.
			// use t_tweet to get cap.
			genCapFeatures(f, sent, i);

			// use t_tweet to generate preposition tags.
			genPrepFeatures(f, sent, i, preposition);

//			genSuffixFeatures(f, sent, i);
			// f9: COUNTRY
			// f11: DISTANCE
			// f12: STOPWORDS
			// f13: BUILDING

			instances.add(f);
		}

		return instances;

	}


	// //////////////////////////////////////////////////////////////////////////////////
	// FEATURE EXTRACTORS
	// //////////////////////////////////////////////
	/**
	 * PREPOSITION OR NOT.
	 * 
	 * INPUT RAW TOKENS OUTPUT BINARY VALUE YES OR NO.
	 * 
	 * @param f
	 * @param t_tweet
	 * @param i
	 */
	// prep-2.prep-1
	private static void genPrepFeatures(List<Feature> f, Sentence sent, int i, HashSet<String> preposition) {
		// String[] t_tweet;

		if (i - 3 >= 0)
			addFeature(f, "-3_cont_prep_" + preposition.contains(TOKLW(sent.getTokens()[i - 3].getToken())));
		if (i - 2 >= 0)
			addFeature(f, "-2_cont_prep_" + preposition.contains(TOKLW(sent.getTokens()[i - 2].getToken())));
		if (i - 1 >= 0)
			addFeature(f, "-1_cont_prep_" + preposition.contains(TOKLW(sent.getTokens()[i - 1].getToken())));
	}

	/**
	 * COUNTRY GAZ EXISTENCE
	 * 
	 * @param f
	 * @param f_country
	 * @param i
	 */
	// country.-1.+1.seq-1+1
	private static void genCountryFeatures(List<Feature> f, boolean[] f_country, int i) {
		addFeature(f, "0_cont_country_" + f_country[i]);
		String countryseq = "";
		if (i - 1 >= 0) {
			addFeature(f, "-1_cont_country_" + f_country[i - 1]);
			countryseq += f_country[i - 1] + "::";
		}
		if (i + 1 <= f_country.length - 1) {
			addFeature(f, "+1_cont_country_" + f_country[i + 1]);
			countryseq += f_country[i + 1];
		}
		addFeature(f, "-+_cont_country_seq_" + countryseq);

	}

	/**
	 * GAZ EXISTENCE
	 * 
	 * @param f
	 * @param f_gaz
	 * @param i
	 */
	// gaz.-1.+1.seq-1+1
	private static void genGazFeatures(List<Feature> f, Sentence sent, int i) {
		// boolean[] f_gaz;
		// CURRENT WORD
		addFeature(f, "0_cont_gaz_" + sent.getTokens()[i].isInLocationGazetteer());

		String gazseq = "";
		if (i - 1 >= 0) {
			addFeature(f, "-1_cont_gaz_" + sent.getTokens()[i - 1].isInLocationGazetteer());
			gazseq += sent.getTokens()[i - 1].isInLocationGazetteer() + "::";
		}
		if (i + 1 <= sent.tokenLength() - 1) {
			addFeature(f, "+1_cont_gaz_" + sent.getTokens()[i + 1].isInLocationGazetteer());
			gazseq += sent.getTokens()[i + 1].isInLocationGazetteer();
		}
		addFeature(f, "-+_cont_gaz_seq_" + gazseq);
	}

	/**
	 * POINT POS FOR EACH SURROUNDING WORD POS SEQUENCE
	 * 
	 * @param f
	 * @param f_pos
	 * @param i
	 */
	// pos.seq-3-1.seq+1+3
	private static void genPosFeatures(List<Feature> f, Sentence twSent, int i) {
		// String[] f_pos;
		int t_length = twSent.tokenLength();
		// f5 PART OF SPEECH

		// CURRENT WORD
		addFeature(f, "0_pos_" + twSent.getTokens()[i].getPOS());

		String posleft = "", posright = "";
		if (i - 4 >= 0) {
			// addFeature(f, "-4.pos." + f_pos[i - 4]);
			// posleft += f_pos[i - 4];
		}
		if (i - 3 >= 0) {
			// addFeature(f, "-3.pos." + f_pos[i - 3]);
			// posleft += f_pos[i - 3];
		}
		if (i - 2 >= 0) {
			// addFeature(f, "-2_pos_" + f_pos[i - 2]);
			posleft += twSent.getTokens()[i - 2].getPOS();
		}
		if (i - 1 >= 0) {
			addFeature(f, "-1_pos_" + twSent.getTokens()[i - 1].getPOS());
			posleft += twSent.getTokens()[i - 1].getPOS();
		}
		if (i + 1 <= t_length - 1) {
			addFeature(f, "+1_pos_" + twSent.getTokens()[i + 1].getPOS());
			posright += twSent.getTokens()[i + 1].getPOS();
		}
		if (i + 2 <= t_length - 1) {
			// addFeature(f, "+2_pos_" + f_pos[i + 2]);
			posright += twSent.getTokens()[i + 2].getPOS();
		}
		if (i + 3 <= t_length - 1) {
			// addFeature(f, "+3.pos." + f_pos[i + 3]);
			// posright += f_pos[i + 3];
		}
		if (i + 4 <= t_length - 1) {
			// addFeature(f, "+4.pos." + f_pos[i + 4]);
			// posright += f_pos[i + 4];
		}
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
	private static void genCapFeatures(List<Feature> f, Sentence sent, int i) {
		// String[] t_tweet;
		int t_length = sent.tokenLength();

		// CURRENT WORD
		addFeature(f, "0_mph_cap_" + MPHCAP(sent.getTokens()[i].getToken()));

		String left = "", right = "";
		if (i - 4 >= 0) {
			// addFeature(f, "-4_mph_cap_" + MPHCAP(t_tweet[i - 4]));
			// left += MPHCAP(t_tweet[i - 4]);
		}
		if (i - 3 >= 0) {
			addFeature(f, "-3_mph_cap_" + MPHCAP(sent.getTokens()[i - 3].getToken()));
			// left += MPHCAP(t_tweet[i - 3]);
		}
		if (i - 2 >= 0) {
			addFeature(f, "-2_mph_cap_" + MPHCAP(sent.getTokens()[i - 2].getToken()));
			left += MPHCAP(sent.getTokens()[i - 2].getToken());
		}
		if (i - 1 >= 0) {
			addFeature(f, "-1_mph_cap_" + MPHCAP(sent.getTokens()[i - 1].getToken()));
			left += MPHCAP(sent.getTokens()[i - 1].getToken()) + "::";
		}
		if (i + 1 <= t_length - 1) {
			addFeature(f, "+1_mph_cap_" + MPHCAP(sent.getTokens()[i + 1].getToken()));
			right += MPHCAP(sent.getTokens()[i + 1].getToken());
		}
		if (i + 2 <= t_length - 1) {
			addFeature(f, "+2_mph_cap_" + MPHCAP(sent.getTokens()[i + 2].getToken()));
			right += MPHCAP(sent.getTokens()[i + 2].getToken());
		}
		if (i + 3 <= t_length - 1) {
			addFeature(f, "+3_mph_cap_" + MPHCAP(sent.getTokens()[i + 3].getToken()));
			// right += MPHCAP(t_tweet[i + 3]);
		}
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
	 *            : Feature list
	 * @param lemmat_tweet
	 *            : lemmas of the tweet,
	 * @param i
	 *            : position of the current word
	 */
	// tok.-1.+1.pres-4+4.
	private static void genTokenFeatures(List<Feature> f, Sentence sent, int i) {
		// String[] lemmat_tweet;
		// CURRENT TOKEN
		addFeature(f, "0_tok_lw_" + TOKLW(sent.getTokens()[i].getLemma()));
		if (i - 4 >= 0) {
			// addFeature(f, "-_tok_lw_" + TOKLW(lemmat_tweet[i - 4]));
			addFeature(f, "-4_tok_present_1");
		} else {
			addFeature(f, "-4_tok_present_0");
		}
		if (i - 3 >= 0) {
			addFeature(f, "-_tok_lw_" + TOKLW(sent.getTokens()[i - 3].getLemma()));
			addFeature(f, "-3_tok_present_1");
		} else {
			addFeature(f, "-3_tok_present_0");
		}
		// this feature has changed into bag of window words feature,
		// which is less specific than just the position.
		if (i - 2 >= 0) {
			addFeature(f, "-2_tok_lw_" + TOKLW(sent.getTokens()[i - 2].getLemma()));
			addFeature(f, "-2_tok_present_1");
		} else {
			addFeature(f, "-2_tok_present_0");
		}
		if (i - 1 >= 0) {
			addFeature(f, "-1_tok_lw_" + TOKLW(sent.getTokens()[i - 1].getLemma()));
			addFeature(f, "-1_tok_present_1");
		} else {
			addFeature(f, "-1_tok_present_0");
		}
		if (i + 1 <= sent.tokenLength() - 1) {
			addFeature(f, "+1_tok_lw_" + TOKLW(sent.getTokens()[i + 1].getLemma()));
			addFeature(f, "+1_tok_present_1");
		} else {
			addFeature(f, "+1_tok_present_0");
		}
		if (i + 2 <= sent.tokenLength() - 1) {
			addFeature(f, "+2_tok_lw_" + TOKLW(sent.getTokens()[i + 2].getLemma()));
			addFeature(f, "+2_tok_present_1");
		} else {
			addFeature(f, "+2_tok_present_0");
		}
		if (i + 3 <= sent.tokenLength() - 1) {
			addFeature(f, "+_tok_lw_" + TOKLW(sent.getTokens()[i + 3].getLemma()));
			addFeature(f, "+3_tok_present_1");
		} else {
			addFeature(f, "+3_tok_present_0");
		}
		if (i + 4 <= sent.tokenLength() - 1) {
			// addFeature(f, "+_tok_lw_" +
			// TOKLW(sent.getTokens()[i+4].getLemma()));
			addFeature(f, "+4_tok_present_1");
		} else {
			addFeature(f, "+4_tok_present_0");
		}
	}

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

	/**
	 * CONVERT TO TYPE Naively decide the tweet token type, url, or hashtag, or
	 * metion, or number. Or it's not any of them, just return it's original
	 * string.
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

	// ////////////////////////////////////////////////////////////////////////////////////
	// GAZ FEATURE HELPER
	// //////////////////////////////////////////////////////////
	/**
	 * GAZ TAGGING BASED ON GREEDY SEARCH. FIND THE LONGEST MATCH STARTING FROM
	 * THE CURRENT WORD
	 * 
	 * @param t_tweet
	 * @param trie
	 * @return
	 */
	private static Sentence gazTag(Sentence twSent, Index index) {
		int len = twSent.tokenLength();
		boolean[] gaztag = new boolean[twSent.tokenLength()];
		int i = 0;
		while (i < len) {
			String history = "";
			for (int j = i; j < len; j++) {
				history += twSent.getTokens()[j].getNorm();
				if (history.trim().length() == 0)
					continue;
				if (index.inIndex(history)) {
					for (int k = i; k < j + 1; k++)
						gaztag[k] = true;
					// gaztag[j]=true;
				}
			}
			i++;
		}

		for (i = 0; i < gaztag.length; i++) {
			twSent.getTokens()[i].setInLocationGazetteer(gaztag[i]);
		}
		return twSent;
	}

	/**
	 * COUNTRY TAGGING BASED ON GREEDY SEARCH FIND THE LONGEST MATCH STARTING
	 * FROM THE CURRENT WORD
	 * 
	 * @param t_tweet
	 * @return
	 */
	private static boolean[] countryTag(Sentence sent) {
		// String[] t_tweet;
		boolean[] countrytag = new boolean[sent.tokenLength()];
		int i = 0;
		while (i < sent.tokenLength()) {
			String history = "";
			for (int j = i; j < sent.tokenLength(); j++) {
				history += " " + StringUtil.getDeAccentLoweredString(sent.getTokens()[j].getNorm());
				// System.out.println(history);
				// System.out.println(ParserUtils.isCountry(history.trim()));
				if (ParserUtils.isCountry(history.trim())) {
					for (int k = i; k < j + 1; k++)
						countrytag[k] = true;
				}
			}
			i++;
		}
		return countrytag;
	}

	/**
	 * HELPER FOR SAFELY TAGGING THE STRINGS
	 * 
	 * @param t_tweet
	 * @param t_street
	 * @param t_building
	 * @param t_toponym
	 * @param t_abbr
	 * @param tk
	 * @return
	 */
	private static HashMap<Integer, String> safeTag(String[] t_tweet, String[] t_street, String[] t_building, String[] t_toponym,
			String[] t_abbr) {
		HashMap<Integer, String> tagresults = new HashMap<Integer, String>();
		if (!EmptyArray(t_toponym)) {
			fillinTag(t_tweet, t_toponym, tagresults, "TP");
		}
		if (!EmptyArray(t_street)) {
			fillinTag(t_tweet, t_street, tagresults, "ST");
		}
		if (!EmptyArray(t_building)) {
			fillinTag(t_tweet, t_building, tagresults, "BD");
		}
		if (!EmptyArray(t_abbr)) {
			fillinTag(t_tweet, t_abbr, tagresults, "AB");
		}
		return tagresults;
	}

	/**
	 * AUXILARY FUNCTION FOR SAFETAG. FILL THE TAG IN t_location INTO THE
	 * tagresults hashmap.
	 * 
	 * @param t_tweet
	 * @param t_street
	 * @param tagresults
	 * @param tk
	 */
	private static void fillinTag(String[] t_tweet, String[] t_location, HashMap<Integer, String> tagresults, String TAG) {

		for (String location : t_location) {

			List<String> loctokens = EuroLangTwokenizer.tokenize(location);
			// if (TAG.equals("AB")) {
			// System.out.println("the original tweet tokenized is : " +
			// Arrays.asList(t_tweet).toString());
			// System.out.println("the location tokenized is :" +
			// loctokens.toString());
			// }
			for (String token : loctokens) {
				boolean have = false;
				String ntoken = StringUtil.getDeAccentLoweredString(token);
				for (int i = 0; i < t_tweet.length; i++) {
					if (StringUtil.getDeAccentLoweredString(
							(t_tweet[i].startsWith("#") && t_tweet[i].length() > 1) ? t_tweet[i].substring(1) : t_tweet[i])
							.equals(ntoken)) {
						tagresults.put(i, TAG);
						have = true;
					}
				}
				if (have == false)
					System.out.println("Don't have the tag: " + token);
			}
		}
		// if(TAG.equals("AB"))
		// System.out.println(tagresults);
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

}
