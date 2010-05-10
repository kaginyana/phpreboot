package com.googlecode.phpreboot.doc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.tools.Analyzers;
import com.sun.javadoc.RootDoc;

public class Doclet {
  public static boolean start(RootDoc rootDoc) throws IOException {
    Scope rootScope = new Scope(null);
    rootScope.register(new Var("ROOT_DOC", true, PrimitiveType.ANY, rootDoc));
    
    PrintWriter writer = new PrintWriter(new FileWriter("doc.html"));
    Reader reader = new InputStreamReader(
        Doclet.class.getResourceAsStream("doclet.phpr"));
    
    Interpreter interpreter = new Interpreter(writer, rootScope);
    Analyzers.run(reader, interpreter, interpreter, null, null);
    
    return true;
  }
}
