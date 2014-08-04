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
package edu.cmu.geolocator.model;

/**
 * Token is sentence specific. 
 * same string in different sentence are different tokens. However, WORD type is sentence-irrelevant.
 * @author indri
 *
 */
public class Token implements Comparable<Token> {

  String token;

  /**
   * which token it is.
   */
  int position;

  String sentId;

  String tokenizerType;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	int start;

	int end;
  /**
   * use labels to store features and results.
   */
  String norm;

  String lemma;

  String POS;

  boolean inLocationGazetteer;

  boolean inNameGazetteer;

  String NE;
  String NEprediction;

  public String getNEprediction() {
	return NEprediction;
}
public void setNEprediction(String nEprediction) {
	NEprediction = nEprediction;
}
/** 
   * Token string, sentence id, and position in the sentence.
   * @param token
   * @param sid
   * @param pos
   */
  public Token(String token,String sid, int pos){
    this.token=token;
    sentId = sid;
    position = pos;
  }
  public String getNE() {
    return NE;
  }

  public Token setNE(String nE) {
    NE = nE;
    return this;
  }
  
  

  public String getNorm() {
    return norm;
  }

  public Token setNorm(String norm) {
    this.norm = norm;
    return this;
  }

  public String getLemma() {
    return lemma;
  }

  public Token setLemma(String lemma) {
    this.lemma = lemma;
    return this;
  }

  public String getPOS() {
    return POS;
  }

  public Token setPOS(String pOS) {
    POS = pOS;
    return this;
  }

  public boolean isInLocationGazetteer() {
    return inLocationGazetteer;
  }

  public Token setInLocationGazetteer(boolean inLocationGazetteer) {
    this.inLocationGazetteer = inLocationGazetteer;
    return this;
  }

  public boolean isInNameGazetteer() {
    return inNameGazetteer;
  }

  public Token setInNameGazetteer(boolean inNameGazetteer) {
    this.inNameGazetteer = inNameGazetteer;
    return this;
  }

  public String getToken() {
    return token;
  }

  public Token setToken(String token) {
    this.token = token;
    return this;

  }

  public int getPosition() {
    return position;
  }

  public Token setPosition(int position) {
    this.position = position;
    return this;

  }

  public String getSentId() {
    return sentId;
  }

  public Token setSentId(String sentId) {
    this.sentId = sentId;
    return this;

  }

  public String getTokenizerType() {
    return tokenizerType;
  }

  public Token setTokenizerType(String tokenizerType) {
    this.tokenizerType = tokenizerType;
    return this;

  }

  public String toString() {
    return token + " : (TokenizerID:" + tokenizerType + ") (NEtype:" + NE + ") ( position:"
            + position + ")" + "(norm:" + norm + ") (lemma:" + lemma + " ) ( POS:" + POS + " ) ";
  }

  public int hashCode() {
    return token.hashCode();
  }

  @Override
  /**
   * This only compares tokens in a single sentence. Never use it on multiple sentences.
   */
  public int compareTo(Token o) {
    if (this.position == o.position)
      return 0;
    if (this.position > o.position)
      return 1;
    return -1;
  }

  /**
   * Equals only check tokens in a single sentence. Don't use it across sentences.
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof Token) {
      if (((Token) o).getToken().equals(this.getToken())
              && ((Token) o).getPosition() == this.getPosition())
        return true;
    }
    return false;
  }

  @Override
  /**
   * sentenceID, position should not be cloned, maybe.
   */
  public Token clone() {
    
    Token c = new Token(this.token,this.sentId,this.position);
    // it does not matter the string equal copies the ref of the string.
    
    /**
     * Don't clone the sentence id and position. Those has to be specificed later.
     */
    c.tokenizerType = new String(this.tokenizerType);
    c.norm = new String(this.norm);
    c.lemma = new String(this.lemma);
    c.POS = new String(this.POS);
    c.inLocationGazetteer = this.inLocationGazetteer;
    c.inNameGazetteer = this.inNameGazetteer;
    c.NE = new String(this.NE);
    return c;
  }
}
