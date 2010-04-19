package com.googlecode.phpreboot;

import java.io.PrintWriter;

import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.tools.Analyzers;

public class Main {
  public static void main(String[] args) throws java.io.IOException {
      java.io.Reader reader;
      if (args.length>0) {
        reader = new java.io.FileReader(args[0]);
      } else {
        reader = new java.io.InputStreamReader(System.in);
      }
  
      PrintWriter writer = new PrintWriter(System.out);
      Scope scope = new Scope(null);
      Interpreter interpreter = new Interpreter(writer, new Evaluator(), scope);
      Analyzers.run(reader, interpreter, interpreter, null, null);
      //System.out.println(interpreter.getScript());
    }
}

