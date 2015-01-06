package edu.cmu.geolocator.resource.Map;

public abstract class Map<T> {

  public abstract T load();
  public abstract T getValue(String code);
}
