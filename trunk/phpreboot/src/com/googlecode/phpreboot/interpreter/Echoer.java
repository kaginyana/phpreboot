package com.googlecode.phpreboot.interpreter;

import java.io.PrintWriter;

import com.googlecode.phpreboot.runtime.Array;

public abstract class Echoer {
  //TODO add overloaded echo, one by primitive types
  public abstract void echo(Object value);
  
  public static Echoer writerEchoer(final PrintWriter writer) {
    return new Echoer() {
      @Override
      public void echo(Object value) {
        writer.println(value);
        writer.flush();
      }
    };
  }
  
  public static Echoer xmlEchoer(final Array array) {
    return new Echoer() {
      @Override
      public void echo(Object value) {
        array.add(value);
      }
    };
  }
}
