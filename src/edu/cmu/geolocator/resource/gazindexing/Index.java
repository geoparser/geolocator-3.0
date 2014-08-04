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
package edu.cmu.geolocator.resource.gazindexing;

import java.util.ArrayList;
import org.apache.lucene.document.*;
public interface Index {
  
  /**
   * search in the resource for existance.
   * @param phrase
   * @return boolean value
   */
  public boolean inIndex(String phrase);
//  public boolean inIndexStrict(String phrase);
  
  /**
   * search index, return the documents fetched.
   * @param phrase to search
   * @return list of docuements containing detail info.
   */
  public ArrayList<Document> getDocumentsByPhrase(String phrase);
//  public ArrayList<Document> getDocumentsByPhraseStrict(String phrase);
  
  /**
   * search index by id.
   */
  public Document getDocumentsById(String id);
  
  /**
   * open index
   */
  public Index open();
  /**
   * close index
   */
   public void close();
}
