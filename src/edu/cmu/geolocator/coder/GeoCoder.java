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
package edu.cmu.geolocator.coder;

import java.util.List;

import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.Tweet;

public interface GeoCoder {

  List<CandidateAndFeature> resolve(Tweet example, String mode,String filter) throws Exception;

}
