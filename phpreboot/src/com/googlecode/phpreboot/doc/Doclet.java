package com.googlecode.phpreboot.doc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import com.googlecode.phpreboot.interpreter.Analyzer;
import com.googlecode.phpreboot.interpreter.Echoer;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;
import com.sun.javadoc.RootDoc;

public class Doclet {
  private Doclet() {
    //enforce singleton
  }

  public static boolean start(RootDoc rootDoc) throws IOException {
    Scope rootScope = new Scope(null);
    rootScope.register(new Var("ROOT_DOC", true, true, PrimitiveType.ANY, rootDoc));
    
    PrintWriter writer = new PrintWriter(new FileWriter("doc.html"));
    try {
      Reader reader = new InputStreamReader(
          Doclet.class.getResourceAsStream("doclet.phpr"));
      try {
        Analyzer.interpret(reader, Echoer.writerEchoer(writer), rootScope);
      } finally {
        reader.close();
      }
    } finally {
      writer.close();
    }
    return true;
  }
}
