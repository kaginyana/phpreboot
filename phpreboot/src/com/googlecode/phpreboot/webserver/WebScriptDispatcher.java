package com.googlecode.phpreboot.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.interpreter.sql.GenericSQLConnection;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.tools.Analyzers;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public class WebScriptDispatcher extends GrizzlyAdapter {
  private final Path rootPath;
  private final String jdbcURL;
  private final Evaluator evaluator;
  
  public WebScriptDispatcher(Path rootPath, Evaluator evaluator, String jdbcURL) {
    this.rootPath = rootPath;
    addRootFolder(rootPath.toAbsolutePath().toString());
    this.evaluator = evaluator;
    this.jdbcURL = jdbcURL;
  }
  
  @Override
  public void service(GrizzlyRequest request, @SuppressWarnings("rawtypes") GrizzlyResponse response) {
    String uri = request.getDecodedRequestURI();
    if (uri.length() == 1) {  // if '/'
      uri = "/index.phpr";
    }
    Path path = rootPath.resolve(uri.substring(1));
    String name = path.getName().toString();
    
    try {
      InputStream input = path.newInputStream();
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
    server.__set__("SERVER_PROTOCOL", request.getProtocol());
    server.__set__("SERVER_ADDR", request.getLocalAddr());
    server.__set__("SERVER_NAME", request.getLocalName());
    server.__set__("REMOTE_ADDR", request.getRemoteAddr());
    server.__set__("REMOTE_NAME", request.getRemoteHost());
    server.__set__("REMOTE_PORT", request.getRemotePort());
    server.__set__("REQUEST_METHOD", request.getMethod());
    server.__set__("REQUEST_URI", request.getRequestURI());
    server.__set__("DOCUMENT_ROOT", rootPath.toString());
    //server.__set__("AUTH_TYPE", request.getAuthType());
    
    scope.register(new ScriptVar("_SERVER", PrimitiveType.ANY, server));
  }
  
  private void fillGetOrPost(GrizzlyRequest request, Scope scope) {
    Array parameters = new Array();
    for(Map.Entry<?,?> entry: ((Map<?,?>)request.getParameterMap()).entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Object[]) {
        value = ((Object[])value)[0];
      }
      parameters.__set__(entry.getKey(), value);
    }
    
    ScriptVar get = new ScriptVar("_GET", PrimitiveType.ANY, null);
    ScriptVar post = new ScriptVar("_POST", PrimitiveType.ANY, null);
    scope.register(get);
    scope.register(post);
    
    String method = request.getMethod();
    if ("GET".equals(method)) {
      get.setValue(parameters);
      post.setValue(new Array());
    } else
      if ("POST".equals(method)) {
        post.setValue(parameters);
        get.setValue(new Array());
      }
  }
  
  private void handleScript(InputStream input, OutputStream output, GrizzlyRequest request) {
    Scope rootScope = new Scope(null);
    fillRequestInfos(request, rootScope);
    
    GenericSQLConnection sqlConnection = new GenericSQLConnection(jdbcURL, evaluator);
    rootScope.register(new ScriptVar("SQL_CONNECTION", PrimitiveType.ANY, sqlConnection));
    
    Reader reader = new InputStreamReader(input);
    PrintWriter writer = new PrintWriter(output);
    try {
      Interpreter interpreter = new Interpreter(writer, evaluator, new Scope(rootScope));
      Analyzers.run(reader, interpreter, interpreter, null, null);
    } finally {
      sqlConnection.close();
    }
  }
}
