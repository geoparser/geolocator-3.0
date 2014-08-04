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
package edu.cmu.geolocator.nlp.soundex;

/*************************************************************************
 *  Compilation:  javac Soundex.java
 *  Execution:    java surname1 surname2
 *
 *
 *  % java Soundex Wohrzhick Warzick
 *  W622: Wohrzhick 
 *  W622: Warzick
 *
 *  % java Soundex Smith Smyth
 *  S530: Smith
 *  S530: Smyth
 *
 *  % java Soundex Washington Lee
 *  W252: Washington
 *  L000: Lee
 *
 *  % java Soundex Pfister Jackson
 *  P236: Pfister
 *  J250: Jackson
 *
 *  % java Soundex Scott Numbers
 *  S300: Scott
 *  N516: Numbers

 *
 *  Note: we ignore the "Names with Prefix" and "Constant Separator"
 *  rules from 
 *  http://www.archives.gov/research_room/genealogy/census/soundex.html
 *
 *************************************************************************/

public class Soundex { 
    public static String soundex(String s) { 
	  
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];

        // convert letters to numeric code
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V': { x[i] = '1'; break; }

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z': { x[i] = '2'; break; }

                case 'D':
                case 'T': { x[i] = '3'; break; }

                case 'L': { x[i] = '4'; break; }

                case 'M':
                case 'N': { x[i] = '5'; break; }

                case 'R': { x[i] = '6'; break; }

                default:  { x[i] = '0'; break; }
            }
        }

        // remove duplicates
        String output = "" + firstLetter;
        for (int i = 1; i < x.length; i++)
            if (x[i] != x[i-1] && x[i] != '0')
                output += x[i];

        // pad with 0's or truncate
        output = output + "0000";
        return output.substring(0, 4);
    }


    public static void main(String[] args) {
        String name1 ="George Town";//= args[0];
        String name2 ="George Colony";//= args[1];
        String code1 = soundex(name1);
        String code2 = soundex(name2);
        System.out.println(code1 + ": " + name1);
        System.out.println(code2 + ": " + name2);
    }
}
