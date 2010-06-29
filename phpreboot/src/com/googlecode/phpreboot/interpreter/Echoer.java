package com.googlecode.phpreboot.interpreter;

import java.io.PrintWriter;

import com.googlecode.phpreboot.runtime.Array;

public abstract class Echoer {
  public abstract void echo(boolean value);
  public abstract void echo(int value);
  public abstract void echo(double value);
  public abstract void echo(String value);
  public abstract void echo(Object value);
  
  static Echoer defaultWriterEchoer() {
    return writerEchoer(new PrintWriter(System.err));
  }
  
  public static Echoer writerEchoer(final PrintWriter writer) {
    return new Echoer() {
      @Override
      public void echo(boolean value) {
        writer.println(value);
        writer.flush();
      }
      
      @Override
      public void echo(int value) {
        writer.println(value);
        writer.flush();
      }
      
      @Override
      public void echo(double value) {
        writer.println(value);
        writer.flush();
      }
      
      @Override
      public void echo(String value) {
        writer.println(value);
        writer.flush();
      }
      
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
      public void echo(boolean value) {
        array.add(value);
      }
      
      @Override
      public void echo(int value) {
        array.add(value);
      }
      
      @Override
      public void echo(double value) {
        array.add(value);
      }
      
      @Override
      public void echo(String value) {
        array.add(value);
      }
      
      @Override
      public void echo(Object value) {
        array.add(value);
      }
    };
  }
}
