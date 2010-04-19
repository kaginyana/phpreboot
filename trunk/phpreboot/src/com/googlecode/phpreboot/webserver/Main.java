package com.googlecode.phpreboot.webserver;

import java.io.IOException;
import java.nio.file.Paths;

import com.sun.grizzly.http.embed.GrizzlyWebServer;

public class Main {
  public static void main(String[] args) throws IOException {
    GrizzlyWebServer ws = new GrizzlyWebServer(8080);

    WebScriptDispatcher webDispatcher = new WebScriptDispatcher(Paths.get("www"));
    ws.addGrizzlyAdapter(webDispatcher, new String[]{"/"});

    System.out.println("Grizzly WebServer listening on port 8080");
    ws.start();
  }
}
