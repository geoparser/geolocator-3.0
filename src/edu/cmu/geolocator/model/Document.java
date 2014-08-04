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
 @author Wei Zhang
*/
package edu.cmu.geolocator.model;

import java.util.ArrayList;

public class Document {

	public String getDid() {
		return did;
	}
	public void setDid(String did) {
		this.did = did;
	}

	String did;

	ArrayList<Paragraph> p;
	public ArrayList<Paragraph> getP() {
		return p;
	}
	public void setP(ArrayList<Paragraph> p) {
		this.p = p;
	}

	
	String headline;
	int headlineStart;
	
	public String getHeadline() {
		return headline;
	}
	public void setHeadline(String headline) {
		this.headline = headline;
	}
	public int getHeadlineStart() {
		return headlineStart;
	}
	public void setHeadlineStart(int headlineStart) {
		this.headlineStart = headlineStart;
	}

	
}
