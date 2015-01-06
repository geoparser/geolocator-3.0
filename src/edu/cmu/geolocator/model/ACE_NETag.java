package edu.cmu.geolocator.model;

public class ACE_NETag {

	
	public ACE_NETag(String mention, int parseInt, int parseInt2, String etype, String esubtype) {
		// TODO Auto-generated constructor stub
		this.phrase=mention;
		this.start=parseInt;
		this.end=parseInt2;
		this.coarseNEType=etype;
		this.fineNEType=esubtype;
	}
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
	public String getPhrase() {
		return phrase;
	}
	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
	public String getCoarseNEType() {
		return coarseNEType;
	}
	public void setCoarseNEType(String coarseNEType) {
		this.coarseNEType = coarseNEType;
	}
	public String getFineNEType() {
		return fineNEType;
	}
	public void setFineNEType(String fineNEType) {
		this.fineNEType = fineNEType;
	}
	int start;
	int end;
	String phrase;
	String coarseNEType;
	String fineNEType;
	
}
