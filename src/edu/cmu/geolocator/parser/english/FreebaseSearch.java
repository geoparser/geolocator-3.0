package edu.cmu.geolocator.parser.english;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.http.HttpRequestFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.LocEntityAnnotation;

public class FreebaseSearch {
	public static String API_KEY = "AIzaSyBEUV7DdIwRy0Ei2J5Uc44IodeSaFavbIU";
	List<LocEntityAnnotation> newtopos;

	public List<LocEntityAnnotation> queryTypes(List<LocEntityAnnotation> topos) {
		try {
			newtopos = new ArrayList<LocEntityAnnotation>();
			HttpTransport httpTransport = new NetHttpTransport();
			com.google.api.client.http.HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/search");

			Iterator<LocEntityAnnotation> iterator = topos.iterator();
			while (iterator.hasNext()) {
				LocEntityAnnotation topo = iterator.next();
				String entityName = topo.getTokenString();
				url.put("query", entityName);
				/*
				 * url.put("filter", "(all type:/people/person)");
				 */
				// url.put("query", "Bill");
				// url.put("lang", "zh");
				// url.put("filter","(all type:/government/us_president)");
				// url.put("output", "(description:wikipedia)");
				url.put("limit", "1");
				url.put("indent", "true");
				url.put("key", FreebaseSearch.API_KEY);
				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				JSONObject response = (JSONObject) parser.parse(httpResponse
						.parseAsString());
				JSONArray results = (JSONArray) response.get("result");
				// System.out.println(results);
				if (results.size() > 0) {
					for (Object planet : results) {
						// System.out.println(((JSONObject) planet).get("id"));
						// System.out.println(((JSONObject)
						// planet).get("notable"));
						JSONObject notable = (JSONObject) ((JSONObject) planet)
								.get("notable");
						 //System.out.println(notable.toString());
						if (notable!=null) {
							String types = notable.get("id").toString();

							if (types != null && types.length() != 0) {
								if (types.indexOf("/location") == -1) {
									// System.out.println(notable.get("name"));
									iterator.remove();
								}
							}
						}else
							iterator.remove();
						// System.out.println(((JSONObject)
						// planet).get("score"));
						// System.out.println(((JSONObject)
						// planet).get("name"));
						// System.out.println(((JSONObject)planet).get("output"));
					}
				}

			}
			newtopos = topos;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return newtopos;
	}
}