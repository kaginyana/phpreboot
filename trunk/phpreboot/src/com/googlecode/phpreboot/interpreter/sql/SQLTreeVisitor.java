package com.googlecode.phpreboot.interpreter.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import com.googlecode.phpreboot.ast.ColumnConstraintNotNull;
import com.googlecode.phpreboot.ast.ColumnDefinition;
import com.googlecode.phpreboot.ast.ColumnNameOptColumnname;
import com.googlecode.phpreboot.ast.ConditionValueDollarAccess;
import com.googlecode.phpreboot.ast.ConditionValueId;
import com.googlecode.phpreboot.ast.ConditionValueLiteral;
import com.googlecode.phpreboot.ast.ConditionValueTableId;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.FromClause;
import com.googlecode.phpreboot.ast.GroupbyClause;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.InsertStatement;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NumberPrecisionDouble;
import com.googlecode.phpreboot.ast.NumberPrecisionInteger;
import com.googlecode.phpreboot.ast.OrderbyClause;
import com.googlecode.phpreboot.ast.Query;
import com.googlecode.phpreboot.ast.SearchCondition;
import com.googlecode.phpreboot.ast.SelectStar;
import com.googlecode.phpreboot.ast.SelectSublistId;
import com.googlecode.phpreboot.ast.SelectSublistTableId;
import com.googlecode.phpreboot.ast.SelectSublistTableStar;
import com.googlecode.phpreboot.ast.SelectSublists;
import com.googlecode.phpreboot.ast.SetQuantifierAll;
import com.googlecode.phpreboot.ast.SetQuantifierDistinct;
import com.googlecode.phpreboot.ast.SetQuantifierEmpty;
import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.ast.SqlInsert;
import com.googlecode.phpreboot.ast.SqlQuery;
import com.googlecode.phpreboot.ast.SqlTableDefinition;
import com.googlecode.phpreboot.ast.SqlTypeChar;
import com.googlecode.phpreboot.ast.SqlTypeDate;
import com.googlecode.phpreboot.ast.SqlTypeFloat;
import com.googlecode.phpreboot.ast.SqlTypeInteger;
import com.googlecode.phpreboot.ast.SqlTypeNumber;
import com.googlecode.phpreboot.ast.SqlTypeSmallint;
import com.googlecode.phpreboot.ast.SqlTypeTime;
import com.googlecode.phpreboot.ast.SqlTypeTimestamp;
import com.googlecode.phpreboot.ast.SqlTypeVarchar;
import com.googlecode.phpreboot.ast.TableConstraint;
import com.googlecode.phpreboot.ast.TableDefinition;
import com.googlecode.phpreboot.ast.TableExpression;
import com.googlecode.phpreboot.ast.UniqueSpecificationPrimarykey;
import com.googlecode.phpreboot.ast.UniqueSpecificationUnique;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.WhereClause;
import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.model.Symbol;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.SQLCursor;

public class SQLTreeVisitor extends Visitor<Void, SQLEnv, RuntimeException>{
  private final Evaluator evaluator;
  
  public SQLTreeVisitor(Evaluator evaluator) {
    this.evaluator = evaluator;
  }

  public void executeQuery(Connection connection, Sql sql, Scope scope) {
    SQLEnv env = new SQLEnv(scope, connection);
    tree(sql, env);
  }
  
  void tree(Node node, SQLEnv env) {
    node.accept(this, env);
  }
  
  
  // --- helper methods
  
  private void join(String delimiter, Collection<? extends Node> c, SQLEnv env) {
    if (c.isEmpty())
      return;
    
    for(Node o: c) {
      tree(o, env);
      env.append(delimiter);
    }
    StringBuilder builder = env.getBuilder();
    builder.setLength(builder.length() - delimiter.length());
  }
  
  
  // --- generic visit
  
  @Override
  protected Void visit(Node node, SQLEnv env) {
    for(Node subNode: node.nodeList()) {
      if (subNode.isToken())
        continue;
      tree(subNode, env);
    }
    return null;
  }
  
  
  // --- visits
  
  @Override
  public Void visit(IdToken id, SQLEnv env) {
    env.append(id.getValue());
    return null;
  }
  
  @Override
  public Void visit(SqlQuery sql_query, SQLEnv env) {
    String name = sql_query.getId().getValue();
    tree(sql_query.getQuery(), env);
    String sqlQuery = env.getBuilder().toString();
    
    ResultSet resultSet;
    List<Object> parameters = env.getParameters();
    Connection connection = env.getConnection();
    try {
      Statement statement;
      if (parameters.isEmpty()) {
        statement = connection.createStatement();
        resultSet = statement.executeQuery(sqlQuery);
        
      } else {
        PreparedStatement prepareStatement = connection.prepareStatement(sqlQuery);
        int index = 1;
        for(Object value: parameters) {
          prepareStatement.setObject(index++, value);
        }
        resultSet = prepareStatement.executeQuery();
      }
      
    } catch (SQLException e) {
      throw (RuntimeException)new RuntimeException(sqlQuery).initCause(e);
    }
    SQLCursor cursor = new SQLCursor(resultSet);
    
    Scope scope = env.getScope();
    Symbol symbol = scope.lookup(name);
    if (symbol == null) {
      ScriptVar scriptVar = new ScriptVar(name, PrimitiveType.ANY, cursor);
      scope.register(scriptVar);
    } else {
      if (!(symbol instanceof ScriptVar)) {
        throw RT.error("%s is not a variable", name);
      }
      ScriptVar scriptVar = (ScriptVar)symbol;
      scriptVar.setValue(cursor);
    }
    return null;
  }
  
  @Override
  public Void visit(SqlTableDefinition sql__table_definition, SQLEnv env) {
    tree(sql__table_definition.getTableDefinition(), env);
    String sql = env.getBuilder().toString();
    
    try {
      Statement statement = env.getConnection().createStatement();
      statement.executeUpdate(sql);
    } catch (SQLException e) {
      throw (RuntimeException)new RuntimeException(sql).initCause(e);
    }
    return null;
  }
  
  @Override
  public Void visit(SqlInsert sql_insert, SQLEnv env) {
    tree(sql_insert.getInsertStatement(), env);
    String sql = env.getBuilder().toString();
    
    try {
      Statement statement = env.getConnection().createStatement();
      statement.executeUpdate(sql);
    } catch (SQLException e) {
      throw (RuntimeException)new RuntimeException(sql).initCause(e);
    }
    return null;
  }
  
  
  // insert statement
  
  @Override
  public Void visit(InsertStatement insert_statement, SQLEnv env) {
    env.append("INSERT INTO "+insert_statement.getId().getValue()+' ');
    tree(insert_statement.getColumnNameOpt(), env);
    env.append(" VALUES (");
    
    StringBuilder builder = env.getBuilder();
    for(Expr expr: insert_statement.getExprPlus()) {
      Object exprValue = evaluator.eval(expr, new EvalEnv(env.getScope(), /*FIXME*/null));
      if (exprValue instanceof String) {
        builder.append('\'').append(exprValue).append('\'');
      } else {
        builder.append(exprValue);
      }
      builder.append(", ");
    }
    builder.setLength(builder.length() - 2);
    builder.append(')');
    return null;
  }
  
  @Override
  public Void visit(ColumnNameOptColumnname column_name_opt_columnname, SQLEnv env) {
    env.append("(");
    join(", ", column_name_opt_columnname.getIdPlus(), env);
    env.append(")");
    return null;
  }
  
  // --- table definition
  
  @Override
  public Void visit(TableDefinition table_definition, SQLEnv env) {
    env.append("CREATE TABLE "+table_definition.getId().getValue()+" (");
    join(", ", table_definition.getTableElementStar(), env);
    env.append(")");
    return null;
  }
  
  @Override
  public Void visit(SqlTypeChar sql_type_char, SQLEnv env) {
    env.append("CHAR("+sql_type_char.getValueLiteral().getValue()+')');
    return null;
  }
  @Override
  public Void visit(SqlTypeVarchar sql_type_varchar, SQLEnv env) {
    env.append("VARCHAR("+sql_type_varchar.getValueLiteral().getValue()+')');
    return null;
  }
  @Override
  public Void visit(SqlTypeNumber sql_type_number, SQLEnv env) {
    env.append("NUMBER(");
    tree(sql_type_number.getNumberPrecision(), env);
    env.append(")");
    return null;
  }
  @Override
  public Void visit(NumberPrecisionInteger number_precision_integer, SQLEnv env) {
    env.append(number_precision_integer.getValueLiteral().getValue().toString());
    return null;
  }
  @Override
  public Void visit(NumberPrecisionDouble number_precision_double, SQLEnv env) {
    env.append(number_precision_double.getValueLiteral().getValue()+","+
               number_precision_double.getValueLiteral2().getValue());
    return null;
  }
  @Override
  public Void visit(SqlTypeSmallint sql_type_smallint, SQLEnv env) {
    env.append("SMALLINT");
    return null;
  }
  @Override
  public Void visit(SqlTypeInteger sql_type_integer, SQLEnv env) {
    env.append("INTEGER");
    return null;
  }
  @Override
  public Void visit(SqlTypeFloat sql_type_float, SQLEnv env) {
    env.append("FLOAT");
    return null;
  }
  @Override
  public Void visit(SqlTypeDate sql_type_date, SQLEnv env) {
    env.append("DATE");
    return null;
  }
  @Override
  public Void visit(SqlTypeTime sql_type_time, SQLEnv env) {
    env.append("TIME");
    return null;
  }
  @Override
  public Void visit(SqlTypeTimestamp sql_type_timestamp, SQLEnv env) {
    env.append("TIMESTAMP");
    return null;
  }
  
  @Override
  public Void visit(ColumnDefinition column_definition, SQLEnv env) {
    env.append(column_definition.getId().getValue()+" ");
    tree(column_definition.getSqlType(), env);
    env.append(" ");
    return null;
  }
  
  @Override
  public Void visit(ColumnConstraintNotNull column_constraint_not_null, SQLEnv env) {
    env.append("NOT NULL");
    return null;
  }
  @Override
  public Void visit(UniqueSpecificationUnique unique_specification_unique, SQLEnv env) {
    env.append("UNIQUE");
    return null;
  }
  @Override
  public Void visit(UniqueSpecificationPrimarykey unique_specification_primarykey, SQLEnv env) {
    env.append("PRIMARY KEY");
    return null;
  }
  
  @Override
  public Void visit(TableConstraint table_constraint, SQLEnv env) {
    tree(table_constraint.getUniqueSpecification(), env);
    env.append("(");
    join(", ", table_constraint.getIdPlus(), env);
    env.append(")");
    return null;
  }
  
  // --- query
  
  @Override
  public Void visit(Query query, SQLEnv env) {
    env.append("SELECT ");
    tree(query.getSetQuantifier(), env);
    env.append(" ");
    tree(query.getSelectList(), env);
    env.append(" ");
    tree(query.getTableExpression(), env);
    return null;
  }
  
  @Override
  public Void visit(TableExpression table_expression, SQLEnv env) {
    tree(table_expression.getFromClause(), env);
    WhereClause whereClause = table_expression.getWhereClauseOptional();
    if (whereClause != null) {
      tree(whereClause, env);
      env.append(" ");
    }
    GroupbyClause groupbyClause = table_expression.getGroupbyClauseOptional();
    if (groupbyClause != null) {
      tree(groupbyClause, env);
      env.append(" ");
    }
    OrderbyClause orderbyClause = table_expression.getOrderbyClauseOptional();
    if (orderbyClause != null) {
      tree(orderbyClause, env);
      env.append(" ");
    }
    return null;
  }
  
  @Override
  public Void visit(SetQuantifierAll setQuantifierAll, SQLEnv env) {
    env.append("ALL");
    return null;
  }
  @Override
  public Void visit(SetQuantifierDistinct setQuantifierDistinct, SQLEnv env) {
    env.append("DISTINCT");
    return null;
  }
  @Override
  public Void visit(SetQuantifierEmpty setQuantifierEmpty, SQLEnv env) {
    return null;
  }
  
  @Override
  public Void visit(SelectStar select_star, SQLEnv env) {
    env.append("*");
    return null;
  }
  @Override
  public Void visit(SelectSublists select_sublists, SQLEnv env) {
    join(", ", select_sublists.getSelectSublistPlus(), env);
    return null;
  }
  
  @Override
  public Void visit(SelectSublistId select_sublist_id, SQLEnv env) {
    env.append(select_sublist_id.getId().getValue());
    return null;
  }
  @Override
  public Void visit(SelectSublistTableId select_sublist_table_id, SQLEnv env) {
    env.append(select_sublist_table_id.getId().getValue()).append(".").append(select_sublist_table_id.getId2().getValue());
    return null;
  }
  @Override
  public Void visit(SelectSublistTableStar select_sublist_table_star, SQLEnv env) {
    env.append(select_sublist_table_star.getId().getValue()).append(".*");
    return null;
  }
  
  
  @Override
  public Void visit(FromClause from_clause, SQLEnv env) {
    env.append("FROM ");
    join(", ", from_clause.getIdPlus(), env);
    return null;
  }

  
  @Override
  public Void visit(WhereClause where_clause, SQLEnv env) {
    env.append(" WHERE ");
    tree(where_clause.getSearchCondition(), env);
    return null;
  }
  
  @Override
  protected Void visit(SearchCondition search_condition, SQLEnv env) {
    ProductionEnum kind = search_condition.getKind();
    List<Node> nodeList = search_condition.nodeList();
    
    // unary
    switch(kind) {
    case search_condition_not:
      env.append("NOT ");
      tree(nodeList.get(0), env);
      return null;
    case search_condition_parens:
      env.append("(");
      tree(nodeList.get(0), env);
      env.append(")");
      return null;
    }
    
    // binary
    tree(nodeList.get(0), env);
    
    switch(kind) {
    case search_condition_like:
      env.append(" LIKE ");
      break;
    case search_condition_and:
      env.append(" AND ");
      break;
    case search_condition_or:
      env.append(" OR ");
      break;
    case search_condition_assign:
      env.append(" = ");
      break;
    case search_condition_is:
      env.append(" IS ");
      break;
    case search_condition_spaceship:
      env.append(" <> ");
      break;
    case search_condition_is_not:
      env.append(" IS NOT ");
      break;
    case search_condition_lt:
      env.append(" < ");
      break;
    case search_condition_le:
      env.append(" <= ");
      break;
    case search_condition_gt:
      env.append(" > ");
      break;
    case search_condition_ge:
      env.append(" >= ");
      break;
    }
    
    tree(nodeList.get(1), env);
    return null;
  }
  
  @Override
  public Void visit(ConditionValueLiteral condition_value_literal, SQLEnv env) {
    Object value = evaluator.eval(condition_value_literal.getSingleLiteral(), new EvalEnv(env.getScope(), null));
    String text;
    if (value instanceof String) {
      text = "'"+value.toString()+"'";
    } else {
      text = String.valueOf(value);
    }
    env.append(text);
    return null;
  }
  @Override
  public Void visit(ConditionValueId condition_value_id, SQLEnv env) {
    env.append(condition_value_id.getId().getValue());
    return null;
  }
  @Override
  public Void visit(ConditionValueTableId condition_value_table_id, SQLEnv env) {
    env.append(condition_value_table_id.getId().getValue()+'.'+
        condition_value_table_id.getId2().getValue());
    return null;
  }
  @Override
  public Void visit(ConditionValueDollarAccess condition_value_dollar_access,SQLEnv env) {
    Object value = evaluator.eval(condition_value_dollar_access.getDollarAccess(), new EvalEnv(env.getScope(), null));
    env.append("?");
    env.getParameters().add(value);
    return null;
  }
}
