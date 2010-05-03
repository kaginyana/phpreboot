package com.googlecode.phpreboot.interpreter.sql;

import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.interpreter.EvalEnv;

public interface SQLConnection {
  public void executeStatement(Sql sql, EvalEnv evalEnv);
}
