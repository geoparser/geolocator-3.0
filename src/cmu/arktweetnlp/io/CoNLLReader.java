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
import java.util.ArrayList;
import java.util.List;

import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.util.BasicFileIO;
import edu.stanford.nlp.util.Pair;


/**
 * Read a simplified version of the CoNLL format.  Two columns
 *   Word \t POSTag
 *
 * With a blank line separating sentences.
 * 
 * Returns 'null' for the input record string
 */
public class CoNLLReader {
	public static ArrayList<Sentence> readFile(String filename) throws IOException {
		BufferedReader reader = BasicFileIO.openFileToReadUTF8(filename);
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();

		ArrayList<String> curLines = new ArrayList<String>();
		String line;
		while ( (line = reader.readLine()) != null ) {
			if (line.matches("^\\s*$")) {
				if (curLines.size() > 0) {
					// Flush
					sentences.add(sentenceFromLines(curLines));
					curLines.clear();
				}
			} else {
				curLines.add(line);
			}
		}
		if (curLines.size() > 0) {
			sentences.add(sentenceFromLines(curLines));
		}
		return sentences;
	}
//	private static Pair<String,Sentence> wrap(Sentence s) {
//		return new Pair<String,Sentence>(null, s);
//	}

	private static Sentence sentenceFromLines(List<String> lines) {
		Sentence s = new Sentence();

		for (String line : lines) {
			String[] parts = line.split("\t");
			assert parts.length == 2;
			s.tokens.add( parts[0].trim() );
			s.labels.add( parts[1].trim() );
		}
		//        System.out.println(s);
		return s;
	}
}
