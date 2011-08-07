package com.googlecode.phpreboot.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.interpreter.EvalEnv;

public class SQLEnv {
  private final EvalEnv evalEnv;
  private final Connection connection;
  private final ArrayList<Object> parameters = new ArrayList<>();
  private final StringBuilder builder =
    new StringBuilder();
  
  public SQLEnv(Connection connection, EvalEnv evalEnv) {
    this.connection = connection;
    this.evalEnv = evalEnv;
  }
  
  public EvalEnv getEvalEnv() {
    return evalEnv;
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
