package com.googlecode.phpreboot.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

import com.googlecode.phpreboot.interpreter.Analyzer;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.sql.GenericSQLConnection;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public class LegacyWebScriptDispatcher extends GrizzlyAdapter {
  private final File rootPath;
  private final String jdbcURL;
  private final Scope rootScope;
  
  public LegacyWebScriptDispatcher(File rootPath, String jdbcURL) {
    this.rootPath = rootPath;
    addRootFolder(rootPath.getAbsolutePath().toString());
    this.jdbcURL = jdbcURL;
    
    Scope rootScope = new Scope(null);
    PrintWriter writer = new PrintWriter(System.out);
    //Interpreter interpreter = new Interpreter(writer, rootScope);
    //RT.includeDefaultFunctions(interpreter);
    writer.flush();
    this.rootScope = rootScope;
  }
  
  @Override
  public void service(GrizzlyRequest request, @SuppressWarnings("rawtypes") GrizzlyResponse response) {
    String uri = request.getDecodedRequestURI();
    if (uri.length() == 1) {  // if '/'
      uri = "/index.phpr";
    }
    File path = new File(rootPath, uri.substring(1));
    String name = path.getName().toString();
    
    try {
      InputStream input = new FileInputStream(path);
      try {
        OutputStream output = response.getOutputStream();
        try {

          if (name.endsWith(".phpr")) {
            handleScript(input, output, request);
          } else {
            copy(input, output);
          }
          
        } finally {
          output.close();
        }
      } finally {
        input.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
      try {
        dumpException(response, e);
      } catch(IOException e2) {
        e2.printStackTrace();
      }
    }
    
    
  }

  private static void dumpException(GrizzlyResponse<?> response, Exception e) throws IOException {
    response.setError();
    PrintStream output = new PrintStream(response.getOutputStream());
    try {
      e.printStackTrace(output);
    } finally {
      output.close();
    }
  }
  
  private static void copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while((read = input.read(buffer)) != -1) {
      output.write(buffer, 0, read);
    }
  }
  
  private void fillRequestInfos(GrizzlyRequest request, Scope scope) {
    fillGetOrPost(request, scope); // _GET or _POST
    fillServer(request, scope); // _SERVER
    
  }
  
  private void fillServer(GrizzlyRequest request, Scope scope) {
    Array server = new Array();
    server.set("SERVER_PROTOCOL", request.getProtocol());
    server.set("SERVER_ADDR", request.getLocalAddr());
    server.set("SERVER_NAME", request.getLocalName());
    server.set("REMOTE_ADDR", request.getRemoteAddr());
    server.set("REMOTE_NAME", request.getRemoteHost());
    server.set("REMOTE_PORT", request.getRemotePort());
    server.set("REQUEST_METHOD", request.getMethod());
    server.set("REQUEST_URI", request.getRequestURI());
    server.set("DOCUMENT_ROOT", rootPath.toString());
    //server.__set__("AUTH_TYPE", request.getAuthType());
    
    scope.register(new Var("_SERVER", false, PrimitiveType.ARRAY, server));
  }
  
  private void fillGetOrPost(GrizzlyRequest request, Scope scope) {
    Array parameters = new Array();
    for(Map.Entry<?,?> entry: ((Map<?,?>)request.getParameterMap()).entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Object[]) {
        value = ((Object[])value)[0];
      }
      parameters.set(entry.getKey(), value);
    }
    
    Object get,post;
    String method = request.getMethod();
    if ("GET".equals(method)) {
      get = parameters;
      post = new Array();
    } else
      if ("POST".equals(method)) {
        post = parameters;
        get = new Array();
      } else {
        post = new Array();
        get = new Array();
      }
    
    scope.register(new Var("_GET", false, PrimitiveType.ARRAY, get));
    scope.register(new Var("_POST", false, PrimitiveType.ARRAY, post));
  }
  
  private void handleScript(InputStream input, OutputStream output, GrizzlyRequest request) {
    Scope scope = new Scope(rootScope);
    fillRequestInfos(request, scope);
    
    GenericSQLConnection sqlConnection = new GenericSQLConnection(jdbcURL);
    scope.register(new Var("SQL_CONNECTION", true, PrimitiveType.ANY, sqlConnection));
    
    Reader reader = new InputStreamReader(input);
    PrintWriter writer = new PrintWriter(output);
    try {
      Analyzer.analyze(reader, writer, new Scope(scope));
    } finally {
      sqlConnection.close();
    }
  }
}
