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
package cmu.arktweetnlp.impl.features;

import java.util.List;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;

import cmu.arktweetnlp.impl.features.FeatureExtractor.FeatureExtractorInterface;
import cmu.arktweetnlp.impl.features.FeatureExtractor.PositionFeaturePairs;

/** We should test if these are obsolete yet **/
public class MetaphoneFeatures {
	
	

	private static Metaphone _metaphone = null;
	private static DoubleMetaphone dblmetaphone = null;
	public static Metaphone getMetaphone() {
		if (_metaphone == null) {
			_metaphone = new Metaphone();
			_metaphone.setMaxCodeLen(100);
		} 
		return _metaphone;
	};
	public static DoubleMetaphone getDblMetaphone() {
		if (dblmetaphone == null) {
			dblmetaphone = new DoubleMetaphone();
			dblmetaphone.setMaxCodeLen(100);
		} 
		return dblmetaphone;
	};
	private String MetaphoneNum(String str){ //change this eventually
		StringBuilder sb = new StringBuilder(str);
		if (str.charAt(str.length()-1)=='1')
			sb.deleteCharAt(str.length()-1).append("one");
		if (str.charAt(0)=='1')
			sb.deleteCharAt(0).insert(0, "one");
		if (str.charAt(0)=='2')
			sb.deleteCharAt(0).insert(0, "two");
		else if(str.charAt(0)=='4')
			sb.deleteCharAt(0).insert(0, "four");
		return sb.toString();
	}
	

	public static class MetaphoneLexical implements FeatureExtractorInterface{
		public void addFeatures(List<String> tokens, PositionFeaturePairs pairs) {
			for (int t=0; t < tokens.size(); t++) {
				String tok = tokens.get(t);
				if(tok.length()>1){
					//String ppword=MetaphoneNum(tok);
					String metaphone_word = getDblMetaphone().encode(tok);
					String alternate_word = getDblMetaphone().doubleMetaphone(tok, true);
					pairs.add(t, "metaphone_word|"+metaphone_word);
					if(!metaphone_word.equals(alternate_word))
						pairs.add(t, "metaphone_word|"+alternate_word);
				}
			}        	
		}
	}
}
