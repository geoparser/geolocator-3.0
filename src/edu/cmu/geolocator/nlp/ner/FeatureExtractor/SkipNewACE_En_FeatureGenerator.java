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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import Wordnet.WordnetApi;
import edu.cmu.geolocator.common.StringUtil;
import edu.cmu.geolocator.model.Document;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.nlp.StanfordCoreTools.StanfordNLP;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.geolocator.resource.dictionary.Dictionary;
import edu.cmu.geolocator.resource.dictionary.Dictionary.DicType;
import edu.cmu.geolocator.resource.gazindexing.Index;
import edu.cmu.minorthird.classify.Feature;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.process.WordShapeClassifier;

public class SkipNewACE_En_FeatureGenerator {

	private static final int WORDSHAPECHRIS1 = 1;
	static ArrayList<String> naturalFeaturesList;
	static ArrayList<String> unnamedLocationsList;
	static ArrayList<String> personNamesList;
	static ArrayList<String> sportsTeamsList;
	static ArrayList<String> namedOrganizationsList;
	static ArrayList<String> namedOrgIndicatorList;
	static ArrayList<String> spatialVerbsList;
	static ArrayList<String> spatialRelationsList;
	static ArrayList<String> spatialPrepsList;
	static ArrayList<String> streetsuffixList;
	static ArrayList<String> newsPaperList;
	static ArrayList<String> numbersList;
	static HashSet<String> toponymsList;
	static StanfordNLP snlp;
	private static String sen;
	static HashMap<String, String> clusters;

	public SkipNewACE_En_FeatureGenerator(String resourcepath) {

		try {
			Dictionary prepdict = Dictionary.getSetFromListFile(resourcepath
					+ "en/prepositions.txt", true, true);
			preposition = (HashSet<String>) prepdict.getDic(DicType.SET);
		} catch (IOException e) {

			e.printStackTrace();
		}

		try {
			readAllLists();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (clusters == null)
			try {
				ReadBrownCluster("res/brownclusters/brown-rcv1.clean.tokenized-CoNLL03.txt-c3200-freq1.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		snlp = new StanfordNLP();

	}

	public static void readAllLists() throws IOException {

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
		naturalFeaturesList = readListFile("LNaturalFeatures");
		toponymsList = readSetFile("LAllCountries"); // LAllCountries");

		/*
		 * System.out.println(LNaturalFeatures.get(2));
		 * System.out.println(LUnnamedLocations.get(2));
		 * System.out.println(LNamedOrganizations.get(2));
		 */
	}

	public static ArrayList<String> readListFile(String FileName)
			throws IOException {

		ArrayList<String> list = new ArrayList<String>();
		String filename = "res/lists/" + FileName + ".txt";
		System.err.println("Reading file:" + filename);
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;

		while ((line = reader.readLine()) != null) {
			// Lower casing before adding to list
			list.add(line.trim().toLowerCase());
		}
		reader.close();
		return list;

	}

	public static HashSet<String> readSetFile(String FileName)
			throws IOException {

		HashSet<String> set = new HashSet<String>();
		String filename = "res/Lists/" + FileName + ".txt";
		System.err.println("Reading file:" + filename);
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;

		while ((line = reader.readLine()) != null) {
			// Lower casing before adding to list
			set.add(line.trim().toLowerCase());
		}
		reader.close();
		return set;
	}

	public static void ReadBrownCluster(String filename) throws IOException {

		clusters = new HashMap<String, String>();
		String cluster = "", line = "", word = "";
		int check = 0;
		BufferedReader bw = new BufferedReader(new FileReader(filename));
		while ((line = bw.readLine()) != null) {
			word = line.split("\t")[1];
			cluster = line.split("\t")[0];
			// System.out.println("brownbrownw"+word+cluster);
			clusters.put(word, cluster);
		}

		System.out.println("BC DONE");

	}

	HashSet<String> preposition;

	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws IOException,
			InterruptedException {

		Document d = null;
		try {
			CRFClassifier.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SkipNewACE_En_FeatureGenerator fgen = new SkipNewACE_En_FeatureGenerator(
				"res/");
		Sentence sent = new Sentence("I live in Pittsburgh");
		List<ArrayList<Feature>> tweetfeatures = fgen.extractFeature(sent);
		System.out.println(tweetfeatures);
		/*
		 * for (Sentence sent : d.getP().get(0).getSentences()) {
		 * List<ArrayList<Feature>> tweetfeatures = fgen.extractFeature(sent); }
		 */

	}

	/**
	 * MAIN FUNCTION FOR EXTRACTIN FEATURES
	 * 
	 * @param t_tweet
	 * @param trie
	 * @param postags
	 * @return
	 * @return FEATURE LISTS
	 * @throws IOException
	 */

	public ArrayList<ArrayList<Feature>> extractFeature(Sentence sent)
			throws IOException {

		int len = sent.tokenLength();

		StringBuffer featureText = new StringBuffer();

		ArrayList<ArrayList<Feature>> instances = new ArrayList<ArrayList<Feature>>(
				len);
		ArrayList<Feature> f = new ArrayList<Feature>();

		// normalize tweet norm_tweet
		for (int i = 0; i < len; i++)
			sent.getTokens()[i].setNorm(StringUtil
					.getDeAccentLoweredString(tokentype(sent.getTokens()[i]
							.getToken())));

		// String[] f_pos = postags.toArray(new String[] {});

		// f_gaz originally. filled in inGaz Field in token. check norm_tweet
		// field.
		// boolean[] f_gaz =
		// gazTag(tweetSentence, this.index);

		// use norm_tweet field to tag countries. don't remove f_country,
		// because it's not a type in
		// token.
		// boolean[] f_country = countryTag(tweetSentence);
		Token[] originalTokens = sent.getTokens();
		String[] tokens = new String[originalTokens.length];
		String[] posTags = new String[tokens.length];

		for (int j = 0; j < originalTokens.length; j++) {
			tokens[j] = originalTokens[j].getToken();
			posTags[j] = originalTokens[j].getPOS();
		}
		sen = sent.getSentenceString();
		// Last word OR last two words are in unnamed location list

		String[] nerTags = new String[tokens.length];
		String[] lemma = new String[tokens.length];
		// Parse features
		Map<String, String> parentEdge = new HashMap<String, String>();
		Map<String, ArrayList<String>> childrenEdge = new HashMap<String, ArrayList<String>>();
		ArrayList<String> npChunks = null;
		// snlp.DoAll(tokens, posTags, lemma, parentEdge, childrenEdge);
		try {
			npChunks = snlp.NPChunker(tokens, posTags);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < len; i++) {
			// clear feature list for this loop
			f = new ArrayList<Feature>();
			// /////////////////////////////// MORPH FEATURES
			// use lemma_tweet to get token features.
			genTokenFeatures(f, sent, i);
			// ////////////////////////////// SEMANTIC FEATURES
			genPosFeatures(f, posTags, sent, i);

			genBrownClusterFeatures(f, originalTokens, i);

			genTagFeatures(f, originalTokens, i);

			//genWordShapeFeatures(f, originalTokens, i);
			// ////////////////////////////////// GAZ AND DICT LOOK UP
			//genGazFeatures(f, sent, i);
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

			//genShapeFeatures(f, sent, i);

			// genChunkFeatures(f, sent, tokens, npChunks, i);
			//genLookupListFeatures(f, sent, i);

			addFeature(f, "W=" + sent.getTokens()[i].getToken());

			// genWordnetFeatures(f, sent, i);
			// genSuffixFeatures(f, sent, i);
			// f9: COUNTRY
			// f11: DISTANCE
			// f12: STOPWORDS
			// f13: BUILDING

			instances.add(f);

			String temple = f.toString().replace("[", "");
			temple = temple.replace("]", "\n");
			temple = temple.replace(",", "");
			featureText.append(temple);
			System.out.println(temple);

		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(
					"c:/featuretrain.txt"), true);
			byte[] contentInBytes = featureText.toString().getBytes();
			fileOutputStream.write(contentInBytes);
			fileOutputStream.write('\r');
			fileOutputStream.flush();
			fileOutputStream.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private static void genPrepFeatures(List<Feature> f, Sentence sent, int i,
			HashSet<String> preposition) {
		// String[] t_tweet;

		if (i - 3 >= 0)
			addFeature(
					f,
					"-3_cont_prep_"
							+ preposition.contains(TOKLW(sent.getTokens()[i - 3]
									.getToken().trim())) + "@-3");
		if (i - 2 >= 0)
			addFeature(
					f,
					"-2_cont_prep_"
							+ preposition.contains(TOKLW(sent.getTokens()[i - 2]
									.getToken().trim())) + "@-2");
		if (i - 1 >= 0)
			addFeature(
					f,
					"-1_cont_prep_"
							+ preposition.contains(TOKLW(sent.getTokens()[i - 1]
									.getToken().trim())) + "@-1");

	}

	private static void genShapeFeatures(List<Feature> f, Sentence sent, int i) {
		// String[] t_tweet;

	}

	/**
	 * COUNTRY GAZ EXISTENCE
	 * 
	 * @param f
	 * @param f_country
	 * @param i
	 */
	// country.-1.+1.seq-1+1
	private static void genCountryFeatures(List<Feature> f,
			boolean[] f_country, int i) {
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
		addFeature(f,
				"0_cont_gaz_" + sent.getTokens()[i].isInLocationGazetteer());

		String gazseq = "";
		if (i - 1 >= 0) {
			addFeature(
					f,
					"-1_cont_gaz_"
							+ sent.getTokens()[i - 1].isInLocationGazetteer()
							+ "@-1");
			gazseq += sent.getTokens()[i - 1].isInLocationGazetteer() + "::";
		}
		if (i + 1 <= sent.tokenLength() - 1) {
			addFeature(
					f,
					"+1_cont_gaz_"
							+ sent.getTokens()[i + 1].isInLocationGazetteer()
							+ "@1");
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
	private static void genPosFeatures(List<Feature> f, String[] posTags,
			Sentence twSent, int i) {
		// String[] f_pos;
		int t_length = twSent.tokenLength();
		// f5 PART OF SPEECH

		// CURRENT WORD
		addFeature(f, "0_pos_" + posTags[i]);

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
			addFeature(f, "-1_pos_" + posTags[i - 1] + "@-1");
			posleft += posTags[i - 1];
		}
		if (i + 1 <= t_length - 1) {
			addFeature(f, "+1_pos_" + posTags[i + 1] + "@1");
			posright += posTags[i + 1];
		}
		if (i + 2 <= t_length - 1) {
			// addFeature(f, "+2_pos_" + posTags[i+2]);
			posright += posTags[i + 2];
		}
		if (i + 3 <= t_length - 1) {
			// addFeature(f, "+3.pos." + f_pos[i + 3]);
			// posright += f_pos[i + 3];
		}
		if (i + 4 <= t_length - 1) {
			// addFeature(f, "+4.pos." + f_pos[i + 4]);
			// posright += f_pos[i + 4];
		}
		// addFeature(f, "-pos_seq_" + posleft+"@-1");
		// addFeature(f, "+pos_seq_" + posright+"@1");

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

		if (i - 3 >= 0) {
			addFeature(f,
					"-3_mph_cap_" + MPHCAP(sent.getTokens()[i - 3].getToken())
							+ "@-3");
			// left += MPHCAP(t_tweet[i - 3]);
		}

		if (i - 2 >= 0) {
			addFeature(f,
					"-2_mph_cap_" + MPHCAP(sent.getTokens()[i - 2].getToken())
							+ "@-2");
			left += MPHCAP(sent.getTokens()[i - 2].getToken());
		}
		if (i - 1 >= 0) {
			addFeature(f,
					"-1_mph_cap_" + MPHCAP(sent.getTokens()[i - 1].getToken())
							+ "@-1");
			left += MPHCAP(sent.getTokens()[i - 1].getToken()) + "::";
		}
		if (i + 1 <= t_length - 1) {
			addFeature(f,
					"+1_mph_cap_" + MPHCAP(sent.getTokens()[i + 1].getToken())
							+ "@1");
			right += MPHCAP(sent.getTokens()[i + 1].getToken());
		}
		if (i + 2 <= t_length - 1) {
			addFeature(f,
					"+2_mph_cap_" + MPHCAP(sent.getTokens()[i + 2].getToken())
							+ "@2");
			right += MPHCAP(sent.getTokens()[i + 2].getToken());
		}
		if (i + 3 <= t_length - 1) {
			addFeature(f,
					"+3_mph_cap_" + MPHCAP(sent.getTokens()[i + 3].getToken())
							+ "@3");
			// right += MPHCAP(t_tweet[i + 3]);
		}

		// addFeature(f, "-_mph_cap_seq_" + left);
		// addFeature(f, "+_mph_cap_seq_" + right);
		// addFeature(f, "-+_mph_cap_seq_" + left + right);

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
		addFeature(f, sent.getTokens()[i].getNE() + " ---- " + "0_tok_lw_"
				+ TOKLW(sent.getTokens()[i].getLemma().trim()));
		if (i - 4 >= 0) {
			addFeature(f, "-4_tok_lw_"
					+ TOKLW(sent.getTokens()[i - 4].getLemma().trim()) + "@-4");
			addFeature(f, "-4_tok_present_1" + "@-4");
		} else {
			addFeature(f, "-4_tok_lw_" + null + "@-4");
			addFeature(f, "-4_tok_present_0" + "@-4");
		}
		if (i - 3 >= 0) {
			addFeature(f, "-3_tok_lw_"
					+ TOKLW(sent.getTokens()[i - 3].getLemma().trim()) + "@-3");
			addFeature(f, "-3_tok_present_1" + "@-3");
		} else {
			addFeature(f, "-3_tok_lw_" + null + "@-3");
			addFeature(f, "-3_tok_present_0" + "@-3");
		}
		// this feature has changed into bag of window words feature,
		// which is less specific than just the position.
		if (i - 2 >= 0) {
			addFeature(f, "-2_tok_lw_"
					+ TOKLW(sent.getTokens()[i - 2].getLemma().trim()) + "@-2");
			addFeature(f, "-2_tok_present_1" + "@-2");
		} else {
			addFeature(f, "-2_tok_lw_" + null + "@-2");
			addFeature(f, "-2_tok_present_0" + "@-2");
		}
		if (i - 1 >= 0) {
			addFeature(f, "-1_tok_lw_"
					+ TOKLW(sent.getTokens()[i - 1].getLemma().trim()) + "@-1");
			addFeature(f, "-1_tok_present_1" + "@-1");
		} else {
			addFeature(f, "-1_tok_lw_" + null + "@-1");
			addFeature(f, "-1_tok_present_0" + "@-1");
		}
		if (i + 1 <= sent.tokenLength() - 1) {
			addFeature(f, "+1_tok_lw_"
					+ TOKLW(sent.getTokens()[i + 1].getLemma().trim()) + "@1");
			addFeature(f, "+1_tok_present_1" + "@1");
		} else {
			addFeature(f, "+1_tok_lw_" + null + "@1");
			addFeature(f, "+1_tok_present_0" + "@1");
		}
		if (i + 2 <= sent.tokenLength() - 1) {
			addFeature(f, "+2_tok_lw_"
					+ TOKLW(sent.getTokens()[i + 2].getLemma().trim()) + "@2");
			addFeature(f, "+2_tok_present_1" + "@2");
		} else {
			addFeature(f, "+2_tok_lw_" + null + "@2");
			addFeature(f, "+2_tok_present_0" + "@2");
		}
		if (i + 3 <= sent.tokenLength() - 1) {
			addFeature(f, "+3_tok_lw_"
					+ TOKLW(sent.getTokens()[i + 3].getLemma().trim()) + "@3");
			addFeature(f, "+3_tok_present_1" + "@3");
		} else {
			addFeature(f, "+3_tok_lw_" + null + "@3");
			addFeature(f, "+3_tok_present_0" + "@3");
		}
		if (i + 4 <= sent.tokenLength() - 1) {
			// addFeature(f, "+_tok_lw_" +
			// TOKLW(sent.getTokens()[i+4].getLemma()));
			addFeature(f, "+4_tok_present_1" + "@4");
		} else {
			addFeature(f, "+4_tok_present_0" + "@4");
		}
	}

	private static void genBrownClusterFeatures(List<Feature> f,
			Token[] tokens, int i) throws IOException {

		if (clusters.containsKey((tokens[i].getToken()))
				&& clusters.get((tokens[i].getToken())) != null) {
			// System.out.println(clusters.get(TOKLW(t_data[i])));
			int a = clusters.get(tokens[i].getToken()).length();
			if (clusters.get(tokens[i].getToken()).length() <= 8&&clusters.get(tokens[i].getToken()).length() > 0) {
				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken())));

				if (i - 1 > 0) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i - 1]
											.getToken())) + "@-1");
					// addFeature(f, tokens[i].getNE() +
					// clusters.get(TOKLW(tokens[i-1].getToken())).substring(0,12)+"@-1");
				}
				if (i + 1 < tokens.length) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i + 1]
											.getToken())) + "@1");
					// addFeature(f, tokens[i].getNE() +
					// clusters.get(TOKLW(tokens[i+1].getToken())).substring(0,12)+"@1");
				}
			} else if (clusters.get(tokens[i].getToken()).length() <= 12
					&& clusters.get(tokens[i].getToken()).length() > 8) {

				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken()))
										.substring(0, 8));
				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken())));

				if (i - 1 > 0) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i - 1]
											.getToken())) + "@-1");
				}
				if (i + 1 < tokens.length) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i + 1]
											.getToken())) + "@1");
					// addFeature(f, tokens[i].getNE() +
					// clusters.get(TOKLW(tokens[i+1].getToken())).substring(0,12)+"@1");
				}

			}else if (clusters.get(tokens[i].getToken()).length() > 12){
				


				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken()))
										.substring(0, 8));
				
				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken()))
										.substring(0, 12));
				addFeature(
						f,
						tokens[i].getNE()
								+ clusters.get((tokens[i].getToken())));

				if (i - 1 > 0) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i - 1]
											.getToken())) + "@-1");
				}
				if (i + 1 < tokens.length) {
					addFeature(
							f,
							tokens[i].getNE()
									+ clusters.get((tokens[i + 1]
											.getToken())) + "@1");
					// addFeature(f, tokens[i].getNE() +
					// clusters.get(TOKLW(tokens[i+1].getToken())).substring(0,12)+"@1");
				}

			
				
			}

		}

		else
			addFeature(f, "BrownCluster_-1");

	}

	private static void genTagFeatures(List<Feature> f, Token[] tokens, int i)
			throws IOException {
		if (i - 1 > 0)
			addFeature(f, tokens[i].getNE() + tokens[i - 1].getNE() + "@-1");
		addFeature(f, tokens[i].getNE() + tokens[i].getToken());
		if (i - 1 > 0)
			addFeature(f, tokens[i].getNE() + tokens[i - 1].getToken() + "@-1");
		if (i + 1 < tokens.length)
			addFeature(f, tokens[i].getNE() + tokens[i + 1].getToken() + "@1");

	}

	private static void genWordShapeFeatures(List<Feature> f, Token[] tokens,
			int i) throws IOException {
		int classifierToUse = WORDSHAPECHRIS1;
		String shapeIns = WordShapeClassifier.wordShape(tokens[i].getToken(),
				classifierToUse);
		addFeature(f, tokens[i].getToken() + shapeIns);
		if (i - 1 > 0)
			addFeature(f, tokens[i - 1].getToken() + shapeIns + "@-1");
		if (i + 1 < tokens.length)
			addFeature(f, tokens[i + 1].getToken() + shapeIns + "@1");
	}

	private static void genChunkFeatures(List<Feature> f, Sentence sent,
			String[] tokens, ArrayList<String> npChunks, int i) {

		int wasInNPChunks = 0;
		int featval = 0, count = 0;

		for (String np : npChunks) {

			if (np.contains(tokens[i])) {
				String[] chunkWords = np.split(" ");
				wasInNPChunks = 1;
				// Check for last word feature
				int length = chunkWords.length;
				String lastWord = chunkWords[length - 1];
				if (unnamedLocationsList.contains(lastWord.toLowerCase()))
					featval = 1;

				// Check for last 2 words

				if (chunkWords.length >= 2) {
					String last2Words = chunkWords[length - 2] + " "
							+ chunkWords[length - 1];
					if (unnamedLocationsList.contains(last2Words.toLowerCase()))
						featval = 1;
				}
				addFeature(f, "0_lword_Unloc_" + featval);

				// First Letter Capitalized in each word of the chunk
				featval = 0;

				for (String cw : chunkWords) {
					if (MPHCAPbool(cw)) {
						count += 1;
					}
				}

				if (count == chunkWords.length) {
					featval = 1;
				}

				addFeature(f, "0_lword_FirstCap_" + featval);

				// Last word OR last two words are in named organization
				// indicator list
				// Last word OR last two words are in named organization
				// indicator list
				featval = 1;

				if (namedOrgIndicatorList.contains(lastWord))
					featval = 0;

				addFeature(f, "0_lword_NamedOrgIndicator_" + featval);
				// Last word OR last two words are in street list

				featval = 0;

				if (streetsuffixList.contains(lastWord))
					featval = 1;

				addFeature(f, "0_lword_StreetSuffix_" + featval);

				featval = 1;

				if (newsPaperList.contains(np) || sportsTeamsList.contains(np)) // ||
																				// tvStationList.contains(np)
				{
					featval = 0;
				}

				addFeature(f, "0_lword_NonLoc_" + featval);

				// 12. If a word in the chunk is on <personal name>, and each
				// word in chunk is upper case…might be preceded by <name title>
				// and sometimes period

				// 2. Phrase might not start with, but include letters and
				// numerals or word-number(s) [requires word list of numbers]
				featval = 0;
				for (String cw : chunkWords) {
					if (numbersList.contains(cw) || cw.matches(".*\\d.*")) {
						featval = 1;
					}
				}
				addFeature(f, "0_lword_Numerals_" + featval);

				featval = 1;
				count = 0;
				for (String cw : chunkWords) {
					if (personNamesList.contains(cw) && MPHCAPbool(cw)) {
						count += 1;
					}
				}

				if (count == chunkWords.length) {
					featval = 0;
				}

				addFeature(f, "0_lword_Person_" + featval);

				// Chunk matching <toponym> or <street> <location abbreviation>
				// or <building/business> or <unnamed location> or <named
				// natural feature> list word is preceded by <spatial verb>
				// within 2 words of the phrase

				// cross the united states
				int s = sen.indexOf(np);
				String preString = sen.substring(0, s + np.length());
				String[] preStrings = preString.split(" ");

				featval = 0;
				int l = preStrings.length;
				// Chunk matching <unnamed location>
				if (unnamedLocationsList.contains(lastWord.toLowerCase())) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialVerbsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 3]))
								featval = 1;
						}

					}

				}
				addFeature(f, "0_unnamedLocation_spatialverbs_" + featval);

				// Chunk matching <toponym>
				featval = 0;
				if (toponymsList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialVerbsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 3]))
								featval = 1;
						}

					}
				}
				addFeature(f, "0_toponyms_spatialverbs_" + featval);

				// Chunk matching <street>
				featval = 0;
				if (streetsuffixList.contains(lastWord)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialVerbsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 3]))
								featval = 1;
						}

					}
				}
				addFeature(f, "0_street_spatialverbs_" + featval);

				// Chunk matching <<named natural feature>>

				featval = 0;
				if (naturalFeaturesList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialVerbsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialVerbsList.contains(preStrings[l - 3]))
								featval = 1;
						}

					}
				}
				addFeature(f, "0_naturalFeatures_spatialverbs_" + featval);
				// Chunk matching <<building/business>>

				// Chunk matching <toponym> phrase and it is preceded within 3
				// by <spatial preposition indicator> featval=0;

				if (unnamedLocationsList.contains(lastWord.toLowerCase())) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_unnamedLocation_spatialprep_" + featval);

				featval = 0;
				if (toponymsList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}

					}
				}
				addFeature(f, "0_lword_toponym_spatialprep_" + featval);

				// Chunk matching <street>
				featval = 0;
				if (streetsuffixList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_street_spatialprep_" + featval);

				featval = 0;
				if (naturalFeaturesList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_naturalFeature_spatialprep_" + featval);

				// Chunk matching <toponym> phrase and it is preceded within 3
				// by <spatial relationships indicator> featval=0;

				if (unnamedLocationsList.contains(lastWord.toLowerCase())) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialRelationsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialRelationsList
									.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialRelationsList
									.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_unnamedLocation_spatialrelation_"
						+ featval);

				featval = 0;
				if (toponymsList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}
				addFeature(f, "0_lword_toponym_spatialrelation_" + featval);
				// Chunk matching <street>
				featval = 0;
				if (streetsuffixList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_street_spatialrelation_" + featval);

				featval = 0;
				if (naturalFeaturesList.contains(np)) {
					if (!(preStrings == null) || !(preStrings.length == 0)) {
						if (spatialPrepsList.contains(preStrings[l - 1]))
							featval = 1;
						if (l - 1 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 2]))
								featval = 1;
						}
						if (l - 2 > 0) {
							if (spatialPrepsList.contains(preStrings[l - 3]))
								featval = 1;
						}
					}
				}

				addFeature(f, "0_lword_naturalFeature_spatialrelation_"
						+ featval);

				break;

			}

		}

		if (wasInNPChunks == 0) {
			addFeature(f, "0_lword_Unloc_" + 0);
			addFeature(f, "0_lword_FirstCap_" + 0);
			addFeature(f, "0_lword_NamedOrgIndicator_" + 0);
			addFeature(f, "0_lword_StreetSuffix_" + 0);
			addFeature(f, "0_lword_NonLoc_" + 0);
			addFeature(f, "0_lword_Numerals_" + 0);
			addFeature(f, "0_lword_Person_" + 0);

			addFeature(f, "0_unnamedLocation_spatialverbs_" + featval);
			addFeature(f, "0_toponym_spatialverbs_" + featval);
			addFeature(f, "0_street_spatialverbs_" + featval);
			addFeature(f, "0_naturalFeature_spatialverbs_" + featval);

			addFeature(f, "0_lword_unnamedLocation_spatialprep_" + featval);
			addFeature(f, "0_lword_toponym_spatialprep_" + featval);
			addFeature(f, "0_lword_street_spatialprep_" + featval);
			addFeature(f, "0_lword_naturalFeature_spatialprep_" + featval);

			addFeature(f, "0_lword_unnamedLocation_spatialrelation_" + featval);
			addFeature(f, "0_lword_toponym_spatialrelation_" + featval);
			addFeature(f, "0_lword_street_spatialrelation_" + featval);
			addFeature(f, "0_lword_naturalFeature_spatialrelation_" + featval);

		}

	}

	private static Boolean MPHCAPbool(String string) {

		boolean a = Character.isUpperCase(string.charAt(0));
		return a;
	}

	private static void genLookupListFeatures(List<Feature> f, Sentence sent,
			int i) {
		/*
		 * addFeature( f, "Presence_LUnnamedLocation_" +
		 * unnamedLocationsList.contains(TOKLW(sent.getTokens()[i]
		 * .getToken())));
		 * 
		 * addFeature( f, "Presence_LPersonNames_" +
		 * personNamesList.contains(TOKLW(sent.getTokens()[i] .getToken())));
		 * addFeature( f, "Presence_LNamedOrganization_" +
		 * namedOrganizationsList.contains(TOKLW(sent
		 * .getTokens()[i].getToken())));
		 */
		addFeature(
				f,
				"Presence_LToponym_"
						+ toponymsList.contains(TOKLW(sent.getTokens()[i]
								.getToken().trim())));
	}

	private static void genWordnetFeatures(List<Feature> f, Sentence sent, int i) {

		ArrayList<String> wordlist = new ArrayList<String>();
		Set<String> wordnet = new HashSet<String>();
		String res = "false";
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

		wordnet = WordnetApi.WordnetFeature(sent.getTokens()[i].getToken());
		for (String w : wordlist) {
			if (wordnet.contains(w))
				res = "true";

		}

		addFeature(f, "0_wordnet_" + res);

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

		lemmastring = StringUtil
				.getDeAccentLoweredString(tokentype(lemmastring));
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
				history += " "
						+ StringUtil
								.getDeAccentLoweredString(sent.getTokens()[j]
										.getNorm());
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
	private static HashMap<Integer, String> safeTag(String[] t_tweet,
			String[] t_street, String[] t_building, String[] t_toponym,
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
	private static void fillinTag(String[] t_tweet, String[] t_location,
			HashMap<Integer, String> tagresults, String TAG) {

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
					if (StringUtil
							.getDeAccentLoweredString(
									(t_tweet[i].startsWith("#") && t_tweet[i]
											.length() > 1) ? t_tweet[i]
											.substring(1) : t_tweet[i]).equals(
									ntoken)) {
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

}
