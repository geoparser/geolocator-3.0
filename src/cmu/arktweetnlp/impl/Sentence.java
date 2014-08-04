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
*/
package cmu.arktweetnlp.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds textual and linguistic information for a sentence.
 * Theoretically could add additional textual,syntactic,etc. annotations as inputs
 */
public class Sentence {
	public List<String> tokens;
	/** This is intended to be null for runtime, used only for training **/
	public List<String> labels;

	public Sentence() {
		this.tokens = new ArrayList<String>();
		this.labels = new ArrayList<String>();
	}

	public int T() {
		return tokens.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int t = 0; t < T(); t++) {
			sb.append(tokens.get(t)).append("/").append(labels.get(t));
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

}