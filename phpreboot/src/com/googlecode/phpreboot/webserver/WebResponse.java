package com.googlecode.phpreboot.webserver;

import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public abstract class WebResponse {
  public abstract void addHeader(String name, String value);
  
  static class GrizzlyWebResponse extends WebResponse {
    private final GrizzlyResponse<?> response;
    
    GrizzlyWebResponse(GrizzlyResponse<?> response) {
      this.response = response;
    }

    @Override
    public void addHeader(String name, String value) {
      response.addHeader(name, value);
    }
  }
}
