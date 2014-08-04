package edu.cmu.geolocator.model;

import java.util.ArrayList;

public class TagDocument {

	String did;
	ArrayList<ACE_NETag> tags;
	
	public TagDocument() {
		tags = new ArrayList<ACE_NETag>();
	}
	
	public String getDid() {
		return did;
	}
	public void setDid(String did) {
		this.did = did;
	}
	public ArrayList<ACE_NETag> getTags() {
		return tags;
	}
	public void setTags(ArrayList<ACE_NETag> tags) {
		this.tags = tags;
	}

	public void addTag(ACE_NETag tag) {
		// TODO Auto-generated method stub
		this.tags.add(tag);
	}
	
}
