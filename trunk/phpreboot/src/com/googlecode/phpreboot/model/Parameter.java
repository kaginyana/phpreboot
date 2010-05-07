package com.googlecode.phpreboot.model;

public class Parameter {
  private final String name;
  private final /*maybenull*/TypeToken type;

  public Parameter(String name, /*maybenull*/TypeToken type) {
    this.name = name;
    this.type = type;
  }
  
  public String getName() {
    return name;
  }
  public /*maybenull*/TypeToken getType() {
    return type;
  }
}
