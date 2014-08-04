/**
 * 
 * Copyright (c) 2012 - 2014 Carnegie Mellon University
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
 * 
 */
package edu.cmu.geolocator.model;

import java.util.HashSet;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Status;

/**
 * 
 * This tweet represents the tweet as a natural language sentence for geo-parsing.
 * 
 * Tweet class includes:
 * 
 * id Raw Status sentence extracted locations.
 * 
 * @author indri
 * 
 */
public class Tweet {

  int createFrom; // 0 for null; -1 for non-status. 1 for status.

  String id;

  // backup reference to the status
  Status status;

  Sentence sentence;

  List<LocEntityAnnotation> toponyms;

  String userLocation;

  String timezone;

  String userDescription;

  String place, placeCountry, placeType; // city, admin, poi,neighborhood,country,

  GeoLocation[][] placeBounds;

  double latitude, longitude;

  /**
   * default constructor, all fields are null, except for lat and lon, which are -999.
   */
  public Tweet() {
    latitude = longitude = -999;
  }

  /**
   * constructor with a string. Other fields are defaults.
   * 
   * @param tweetStr
   */
  public Tweet(String tweetStr) {
    this();
    this.sentence = new Sentence(tweetStr);
    this.createFrom = -1;
    // the rest of the fields other than sentence and latitude and longitude has been automatically
    // assigned null;
  }

  /**
   * construct tweet from status. All the fields are filled out automatically.
   * 
   * @param status
   */
  public Tweet(Status status) {
    this();
    this.status = status;
    this.createFrom = 1;
    loadStatus();

  }

  public String getId() {
    return id;
  }

  /**
   * set ID only when tweet is not constructed from status.
   * 
   * @param id
   * @return
   */
  public Tweet setId(String id) {
    if (createFrom == 1){
      System.err.println("Tweet is Created from Status. ID not changable.");
      return this;
    }
    this.id = id;
    return this;
  }

  public Status getStatus() {
    return status;
  }

  /**
   * if the status is re-set, all the fields are changed.
   * 
   * @param status
   * @return
   */
  public Tweet setStatus(Status status) {
    this.status = status;
    loadStatus();
    return this;
  }

  private void loadStatus() {
    // TODO Auto-generated method stub
    this.id = "" + status.getId();
    this.sentence = new Sentence(status.getText());

    if (status.getUser() != null) {
      if (status.getUser().getLocation() != null)
        this.userLocation = status.getUser().getLocation();
      if (status.getUser().getDescription() != null)
        this.userDescription = status.getUser().getDescription();
      if (status.getUser().getTimeZone() != null)
        this.timezone = status.getUser().getTimeZone();
    }
    if (status.getGeoLocation() != null) {
      this.latitude = status.getGeoLocation().getLatitude();
      this.longitude = status.getGeoLocation().getLongitude();
    }
    if (status.getPlace() != null) {
      this.place = status.getPlace().getFullName();
      this.placeCountry = status.getPlace().getCountry();
      placeBounds = status.getPlace().getBoundingBoxCoordinates();
      this.placeType = status.getPlace().getPlaceType();
    }
  }

  public Sentence getSentence() {
    return sentence;
  }

  /**
   * set sentence only when tweet is not constructed from status.
   * 
   * @param sentence
   * @return
   */
  public Tweet setSentence(Sentence sentence) {
    if (createFrom == 1){
      System.err.println("Sentence is Created from Status. Sentence not changable.");
      return this;
    }
    this.sentence = sentence;
    return this;
  }
  
  public Tweet setSentence(String sentence){
    if (createFrom == 1){
      System.err.println("Sentence is Created from Status. Sentence not changable.");
      return this;
    }
    this.sentence = new Sentence(sentence);
    return this;
  }

  public Tweet setText(String text) {
    if (createFrom == 1){
      System.err.println("Text is Created from Status. ID not changable.");
      return this;
    }
    this.sentence = new Sentence(text);
    return this;
  }
  
  public String getText(){
    return this.sentence.getSentenceString();
  }

  public List<LocEntityAnnotation> getToponyms() {
    return toponyms;
  }

  public Tweet setToponyms(List<LocEntityAnnotation> toponyms) {
    this.toponyms = toponyms;
    return this;
  }

  public String getUserLocation() {
    return userLocation;
  }

  public Tweet setUserLocation(String userLocation) {
    if (createFrom == 1){
      System.err.println("UserLocation is Created from Status. ID not changable.");
      return this;
    }
    this.userLocation = userLocation;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public Tweet setTimezone(String timezone) {
    if (createFrom == 1){
      System.err.println("Timezone is Created from Status. ID not changable.");
      return this;
    }

    this.timezone = timezone;
    return this;
  }

  public String getUserDescription() {
    return userDescription;
  }

  public Tweet setUserDescription(String userDescription) {
    if (createFrom == 1){
      System.err.println("UserDescription is Created from Status. ID not changable.");
      return this;
    }

    this.userDescription = userDescription;
    return this;
  }

  /**
   * user information
   * 
   * @return
   */
  public boolean containsUserInfo() {
    if (this.userLocation.length() == 0 && this.userDescription.length() == 0)
      return false;
    else
      return true;
  }

  /**
   * user information
   * 
   * @return
   */
  public String getUserInfo() {
    return userLocation + " : " + userDescription;
  }

  public String getPlace() {
    return place;
  }

  public Tweet setPlace(String place) {
    if (createFrom == 1){
      System.err.println("Place is Created from Status. ID not changable.");
      return this;
    }

    this.place = place;
    return this;

  }

  public String getPlaceCountry() {
    return placeCountry;
  }

  public Tweet setPlaceCountry(String placeCountry) {
    if (createFrom == 1){
      System.err.println("PlaceCountry is Created from Status. ID not changable.");
      return this;
    }

    this.placeCountry = placeCountry;
    return this;
  }

  public GeoLocation[][] getPlaceBounds() {
    return placeBounds;
  }

  public Tweet setPlaceBounds(GeoLocation[][] placeBounds) {
    if (createFrom == 1){
      System.err.println("PlaceBounds is Created from Status. ID not changable.");
      return this;
    }

    this.placeBounds = placeBounds;
    return this;
  }

  public String getPlaceType() {
    return placeType;
  }

  public Tweet setPlaceType(String placeType) {
    if (createFrom == 1){
      System.err.println("PlaceType is Created from Status. ID not changable.");
      return this;
    }

    this.placeType = placeType;
    return this;
  }

  public double getLatitude() {
    return latitude;
  }

  public Tweet setLatitude(double latitude) {
    if (createFrom == 1){
      System.err.println("Latitude is Created from Status. ID not changable.");
      return this;
    }

    this.latitude = latitude;
    return this;
  }

  public double getLongitude() {
    return longitude;
  }

  public Tweet setLongitude(double longitude) {
    if (createFrom == 1){
      System.err.println("longitude is Created from Status. ID not changable.");
      return this;
    }

    this.longitude = longitude;
    return this;
  }

  public boolean containsTweetCoord() {
    if (this.latitude == -999 || this.longitude == -999)
      return false;
    return true;
  }

  public String getToponymsAsText() {
    // TODO Auto-generated method stub
    StringBuilder sb = new StringBuilder();
    for (LocEntityAnnotation e : this.toponyms)
      sb.append(e).append("\n");
    return sb.toString();
  }

  public HashSet<String> getAllIds() {
    // TODO Auto-generated method stub
    if (this.toponyms == null)
      return null;
    HashSet<String> ids = new HashSet<String>();
    for (LocEntityAnnotation topo : this.toponyms) {
      if (topo.getGeonamesIds() == null)
        continue;
      ids.addAll(topo.getGeonamesIds());
    }
    return ids;
  }

  public boolean containsTimezone() {
    // TODO Auto-generated method stub
    return !(timezone == null);
  }

  @Override
  public String toString() {

    return "tweet id: " + id + ", text : " + sentence;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
