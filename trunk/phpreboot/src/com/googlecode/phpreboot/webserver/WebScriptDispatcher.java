package com.googlecode.phpreboot.webserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.tools.Analyzers;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;

public class WebScriptDispatcher extends GrizzlyAdapter {
  private final Path rootPath;
  
  public WebScriptDispatcher(Path rootPath) {
    this.rootPath = rootPath;
    addRootFolder(rootPath.toAbsolutePath().toString());
  }
  
  @Override
  public void service(GrizzlyRequest request, @SuppressWarnings("rawtypes") GrizzlyResponse response) {
    String uri = request.getDecodedRequestURI();
    Path path = rootPath.resolve(uri.substring(1));
    String name = path.getName().toString();
    
    try {
      if (name.endsWith(".phpr")) {
        try {
          handleScript(path, request, response);
        } catch (IOException e) {
          dumpException(response, e);
        }
        return;
      }

      try {
        service(uri, request.getRequest(), response.getResponse());
      } catch (Exception e) {
        dumpException(response, e);
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  private void dumpException(GrizzlyResponse<?> response, Exception e) throws IOException {
    response.setError();
    PrintStream output = new PrintStream(response.getOutputStream());
    try {
      e.printStackTrace(output);
    } finally {
      output.close();
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
    server.__set__("AUTH_TYPE", request.getAuthType());
    
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
    
    String method = request.getMethod();
    if ("GET".equals(method)) {
      scope.register(new ScriptVar("_GET", PrimitiveType.ANY, parameters));   
    } else
      if ("POST".equals(method)) {
        scope.register(new ScriptVar("_POST", PrimitiveType.ANY, parameters));
      }
  }
  
  private void handleScript(Path path, GrizzlyRequest request, GrizzlyResponse<?> response) throws IOException {
    Scope scope = new Scope(null);
    fillRequestInfos(request, scope);
    
    Reader reader = new InputStreamReader(path.newInputStream());
    try {
      PrintWriter writer = new PrintWriter(response.getOutputStream());
      try {
        Interpreter interpreter = new Interpreter(writer, new Evaluator(), scope);
        Analyzers.run(reader, interpreter, interpreter, null, null);
      } finally {
        writer.close();
      }
    } finally {
      reader.close();
    }
  }
}
