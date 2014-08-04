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

import java.util.Arrays;

public class Sentence {

	String sentenceString;

	Token[] tokens;

	String id;

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

	public Sentence(String s) {
		this.setSentenceString(s);
	}

	public void setTokens(Token[] tokens) {
		this.tokens = tokens;
	}

	Dependency[] dependencies;

	public String getSentenceString() {
		return sentenceString;
	}

	public Sentence setSentenceString(String sentenceString) {
		this.sentenceString = sentenceString;
		return this;
	}

	public Token[] getTokens() {
		return tokens;
	}

	public String getId() {
		return id;
	}

	public Sentence setId(String sentenceId) {
		this.id = sentenceId;
		return this;
	}

	public Dependency[] getDependencies() {
		return dependencies;
	}

	public Sentence setDependencies(Dependency[] dependencies) {
		this.dependencies = dependencies;

		return this;
	}

	public int tokenLength() {
		if (tokens == null)
			return 0;
		return tokens.length;
	}

	public int charLength() {
		return sentenceString.length();
	}

	public String toString() {
		return "id is " + id + ", sentence string is : " + sentenceString
				+ "\n tokens are : " + Arrays.asList(tokens);
	}
}
