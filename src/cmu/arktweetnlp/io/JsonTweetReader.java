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
package cmu.arktweetnlp.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

import cmu.arktweetnlp.util.BasicFileIO;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 */
public class JsonTweetReader  {
	ObjectMapper mapper;
	
	public JsonTweetReader() {
		mapper = new ObjectMapper();
	}
	
	/**
	 * Get the text from a raw Tweet JSON string.
	 * 
	 * @param tweetJson
	 * @return null if there is no text field, or invalid JSON.
	 */
	public String getText(String tweetJson) {
		JsonNode rootNode; 
		
		try {
			rootNode = mapper.readValue(tweetJson, JsonNode.class);
		} catch (JsonParseException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		
		if (! rootNode.isObject())
			return null;
		
		JsonNode textValue = rootNode.get("text");
		if (textValue==null)
			return null;
		
		return textValue.asText();
	}
	
	public boolean isJson(String isThisJson) {
		JsonNode rootNode; 
		
		if (isThisJson.charAt(0) != '{')
			return false;
		
		try {
			rootNode = mapper.readValue(isThisJson, JsonNode.class);
		} catch (JsonParseException e) {
			return false;
		} catch (IOException e) {
			System.err.println("WTF -- got IOException in isJson()");
			return false;
		}
		return true;
		
	}

}
