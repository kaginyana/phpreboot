package com.googlecode.phpreboot.interpreter.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.interpreter.Scope;

public class SQLEnv {
  private final Scope scope;
  private final Connection connection;
  private final ArrayList<Object> parameters =
    new ArrayList<Object>();
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
  
  public List<Object> getParameters() {
    return parameters;
  }
  
  public SQLEnv append(String s) {
    builder.append(s);
    return this;
  }
  
  public StringBuilder getBuilder() {
    return builder;
  }
}
