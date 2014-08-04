/**
 * 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 * 
 * @author Wei Zhang,  Language Technology Institute, School of Computer Science, Carnegie-Mellon University.
 * email: wei.zhang@cs.cmu.edu
 * 
 */
package edu.cmu.geolocator.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;

import edu.cmu.geolocator.common.OSUtil;

public class GetReader {

  public static BufferedReader getUTF8FileReader(String filename) throws FileNotFoundException,
          UnsupportedEncodingException {
    File file = new File(filename);
    BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(file),
            "utf-8"));
    return bin;
  }

  public static IndexSearcher getIndexSearcherFromWriter(String filename) throws IOException {
    IndexWriter iw = GetWriter.getIndexWriter(filename, 1024);
    IndexReader ir = IndexReader.open(iw, false);
    IndexSearcher searcher = new IndexSearcher(ir);
    return searcher;
  }

  public static IndexSearcher getIndexSearcher(String filename, String diskOrMem) throws Exception {
    IndexReader reader;
    Directory d ;
    if (diskOrMem.equals("disk"))
      d = FSDirectory.open(new File(filename));
    else if (diskOrMem.equals("mmap"))
      d=  MmapDirectory(new File(filename));
    else
      throw new Exception("parameter for directory type not defined.");
    
    if(OSUtil.isWindows())
     reader = IndexReader.open(FSDirectory.open(new File(filename)));
    else
      reader = IndexReader.open(NIOFSDirectory.open(new File(filename))); 
    IndexSearcher searcher = new IndexSearcher(reader);
    return searcher;
  }

  private static Directory MmapDirectory(File file) {
    // TODO Auto-generated method stub
    return null;
  }

  public static BufferedReader getCommandLineReader() throws UnsupportedEncodingException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
    return br;
  }

}
