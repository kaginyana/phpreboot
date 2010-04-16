package com.googlecode.phpreboot;

import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.tools.Analyzers;

public class Main {
  public static void main(String[] args) throws java.io.IOException {
      java.io.Reader reader;
      if (args.length>0) {
        reader = new java.io.FileReader(args[0]);
      } else {
        reader = new java.io.InputStreamReader(System.in);
      }
  
      Interpreter interpreter = new Interpreter();
      
      Analyzers.run(reader, interpreter, interpreter, null, null);
      //System.out.println(interpreter.getScript());
    }
}

