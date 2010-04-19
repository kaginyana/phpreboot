package com.googlecode.phpreboot.interpreter.sql;

import java.sql.Connection;
import java.util.HashSet;

import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Symbol;

public class SQLEnv {
  private final Scope scope;
  private final Connection connection;
  private final HashSet<Symbol> parameterSet =
    new HashSet<Symbol>();
  private final StringBuilder builder =
    new StringBuilder();
  
  public SQLEnv(Scope scope, Connection connection) {
    this.scope = scope;
    this.connection = connection;
  }
  
  public Scope getScope() {
    return scope;
  }
  public Connection getConnection() {
    return connection;
  }
  
  public void addParameter(Symbol symbol) {
    parameterSet.add(symbol); 
  }
  
  public SQLEnv append(String s) {
    builder.append(s);
    return this;
  }
  
  public StringBuilder getBuilder() {
    return builder;
  }
}
