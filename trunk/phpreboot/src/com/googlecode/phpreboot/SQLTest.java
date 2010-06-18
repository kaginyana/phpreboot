package com.googlecode.phpreboot;

import java.io.PrintWriter;

import com.googlecode.phpreboot.interpreter.Analyzer;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.sql.GenericSQLConnection;

public class SQLTest {
  public static void main(String[] args) throws java.io.IOException, ClassNotFoundException {
      java.io.Reader reader;
      if (args.length>0) {
        reader = new java.io.FileReader(args[0]);
      } else {
        reader = new java.io.InputStreamReader(System.in);
      }
  
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      String dbName="phprDB";
      String url = "jdbc:derby:" + dbName + ";create=true";

      
      GenericSQLConnection sqlConnection = new GenericSQLConnection(url);
      
      Scope scope = new Scope(null);
      scope.register(new Var("SQL_CONNECTION", true, true, PrimitiveType.ANY, sqlConnection));
      
      PrintWriter writer = new PrintWriter(System.out);
      Analyzer.interpret(reader, writer, scope);
      
      sqlConnection.close();
    }
}

