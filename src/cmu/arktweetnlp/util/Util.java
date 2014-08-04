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
package cmu.arktweetnlp.util;

import java.util.ArrayList;
import java.util.Arrays;

import edu.stanford.nlp.util.StringUtils;


public class Util { 
	public static void p(Object x) { System.out.println(x); }
	public static void p(String[] x) { p(Arrays.toString(x)); }
	public static void p(double[] x) { p(Arrays.toString(x)); }
	public static void p(int[] x) { p(Arrays.toString(x)); }
	public static void p(double[][] x) {

		System.out.printf("(%s x %s) [\n", x.length, x[0].length);
		for (double[] row : x) {
			System.out.printf(" ");
			p(Arrays.toString(row));
		}
		p("]");
	}
	public static String sp(double[] x) {
		ArrayList<String> parts = new ArrayList<String>();
		for (int i=0; i < x.length; i++)
			parts.add(String.format("%.2g", x[i]));
		return "[" + StringUtils.join(parts) + "]";
	}
	//	public static void p(int[][] x) { p(Arrays.toString(x)); }
	public static void p(String x) { System.out.println(x); }
}
