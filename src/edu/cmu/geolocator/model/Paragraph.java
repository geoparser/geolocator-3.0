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
