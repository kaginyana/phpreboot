package com.googlecode.phpreboot;

import java.io.PrintWriter;
import java.sql.SQLException;

import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Interpreter;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.interpreter.sql.GenericSQLConnection;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.tools.Analyzers;

public class SQLTest {
  public static void main(String[] args) throws java.io.IOException, ClassNotFoundException, SQLException {
      java.io.Reader reader;
      if (args.length>0) {
        reader = new java.io.FileReader(args[0]);
      } else {
        reader = new java.io.InputStreamReader(System.in);
      }
  
      Evaluator evaluator = new Evaluator();
      
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      String dbName="phprDB";
      String url = "jdbc:derby:" + dbName + ";create=true";

      
      GenericSQLConnection sqlConnection = new GenericSQLConnection(url, evaluator);
      
      Scope scope = new Scope(null);
      scope.register(new ScriptVar("SQL_CONNECTION", PrimitiveType.ANY, sqlConnection));
      
      PrintWriter writer = new PrintWriter(System.out);
      Interpreter interpreter = new Interpreter(writer, evaluator, scope);
      Analyzers.run(reader, interpreter, interpreter, null, null);
      //System.out.println(interpreter.getScript());
      
      sqlConnection.close();
    }
}

