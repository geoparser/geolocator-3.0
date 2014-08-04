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
 @author Xu Chen, Wei Zhang
*/
package edu.cmu.geolocator.model;

import java.util.ArrayList;

public class Paragraph {

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(ArrayList<Sentence> sentences) {
		this.sentences = sentences;
	}

	ArrayList<Sentence> sentences;

	int paraStart;
	public int getParaStart() {
		return paraStart;
	}

	public void setParaStart(int paraStart) {
		this.paraStart = paraStart;
	}

	String paragraphString;
	public String getParagraphString() {
		return paragraphString;
	}

	public void setParagraphString(String paragraphString) {
		this.paragraphString = paragraphString;
	}


}
