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
import java.util.HashMap;

import edu.berkeley.nlp.util.StringUtils;

/** Could scrap this and use ark-regression's version -- that one has CheapStrings **/
public class Vocabulary {

	private HashMap<String,Integer> name2num;
	private ArrayList<String> num2name;
	private boolean isLocked = false;

	Vocabulary() { 
		name2num = new HashMap<String, Integer>();
		num2name = new ArrayList<String>();
	}

	public void lock() {
		isLocked = true;
	}
	public boolean isLocked() { return isLocked; }

	public int size() {
		assert name2num.size() == num2name.size();
		return name2num.size();
	}

	/** 
	 *  If not locked, an unknown name is added to the vocabulary.
	 *  If locked, return -1 on OOV.
	 * @param featname
	 * @return
	 */
	public int num(String featname) {
		if (! name2num.containsKey(featname)) {
			if (isLocked) return -1;

			int n = name2num.size();
			name2num.put(featname, n);
			num2name.add(featname);
			return n;
		} else {
			return name2num.get(featname);
		}
	}

	public String name(int num) {
		if (num2name.size() <= num) {
			throw new RuntimeException("Unknown number for vocab: " + num);
		} else {
			return num2name.get(num);
		}
	}

	public boolean contains(String name) {
		return name2num.containsKey(name);
	}


	public String toString() {
		return "[" + StringUtils.join(num2name) + "]";
	}

	/** Throw an error if OOV **/
	public int numStrict(String string) {
		assert isLocked;
		int n = num(string);
		if (n == -1) throw new RuntimeException("OOV happened");
		return n;
	}
}