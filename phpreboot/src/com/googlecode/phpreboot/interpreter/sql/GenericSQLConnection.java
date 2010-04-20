package com.googlecode.phpreboot.interpreter.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;

public class GenericSQLConnection implements SQLConnection {
  private final String connectionURL;
  private final SQLTreeVisitor sqlTreeVisitor;
  private Connection connection; 
  
  public GenericSQLConnection(String connectionURL, Evaluator evaluator) {
    this.connectionURL = connectionURL;
    this.sqlTreeVisitor = new SQLTreeVisitor(evaluator);
  }
  
  @Override
  public void executeStatement(Sql sql, Scope scope) {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection(connectionURL);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    sqlTreeVisitor.executeQuery(connection, sql, scope);
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
