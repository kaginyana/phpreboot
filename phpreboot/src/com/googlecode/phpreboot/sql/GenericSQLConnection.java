package com.googlecode.phpreboot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.interpreter.EvalEnv;

public class GenericSQLConnection implements SQLConnection {
  private final String connectionURL;
  private Connection connection; 
  
  public GenericSQLConnection(String connectionURL) {
    this.connectionURL = connectionURL;
  }
  
  @Override
  public void executeStatement(Sql sql, EvalEnv evalEnv) {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection(connectionURL);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    SQLTreeVisitor.INSTANCE.executeQuery(sql, connection, evalEnv);
  }

  public void close() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        // do nothing
      }
      connection = null;
    }
  }
}
