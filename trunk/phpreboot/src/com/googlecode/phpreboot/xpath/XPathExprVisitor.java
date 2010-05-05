package com.googlecode.phpreboot.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPathFunctionContext;
import org.jaxen.expr.DefaultXPathFactory;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.Step;
import org.jaxen.expr.XPathExpr;
import org.jaxen.expr.XPathFactory;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.Operator;

import com.googlecode.phpreboot.ast.AbsoluteLocationPathAbbreviated;
import com.googlecode.phpreboot.ast.AbsoluteLocationPathRelative;
import com.googlecode.phpreboot.ast.AbsoluteLocationPathSlash;
import com.googlecode.phpreboot.ast.AxisSpecifierAbbreviated;
import com.googlecode.phpreboot.ast.AxisSpecifierName;
import com.googlecode.phpreboot.ast.Flwor;
import com.googlecode.phpreboot.ast.ForClause;
import com.googlecode.phpreboot.ast.ForOrLet;
import com.googlecode.phpreboot.ast.ForOrLetFor;
import com.googlecode.phpreboot.ast.ForOrLetLet;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.LocationPathAbsolute;
import com.googlecode.phpreboot.ast.LocationPathRelative;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NodeTestId;
import com.googlecode.phpreboot.ast.NodeTestIdStar;
import com.googlecode.phpreboot.ast.NodeTestQualified;
import com.googlecode.phpreboot.ast.NodeTestStar;
import com.googlecode.phpreboot.ast.NodeTestType;
import com.googlecode.phpreboot.ast.Predicate;
import com.googlecode.phpreboot.ast.RelativeLocationPathAbbreviated;
import com.googlecode.phpreboot.ast.RelativeLocationPathPathStep;
import com.googlecode.phpreboot.ast.RelativeLocationPathStep;
import com.googlecode.phpreboot.ast.StepDot;
import com.googlecode.phpreboot.ast.StepDoubleDot;
import com.googlecode.phpreboot.ast.StepNodeTest;
import com.googlecode.phpreboot.ast.StepNodeTestAxis;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.Xorderby;
import com.googlecode.phpreboot.ast.XpathExpr;
import com.googlecode.phpreboot.ast.XpathExprDollarAccess;
import com.googlecode.phpreboot.ast.XpathExprLocationPath;
import com.googlecode.phpreboot.ast.XpathLiteral;
import com.googlecode.phpreboot.ast.Xwhere;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.XML;
import com.googlecode.phpreboot.runtime.XMLNavigator;

public class XPathExprVisitor extends Visitor<Object, XPathExprEnv, JaxenException> {
  private XPathExprVisitor() {
    // enforce singleton
  }
  
  public static final XPathExprVisitor INSTANCE = new XPathExprVisitor();
  
  // --- helpers
  
  private void gatherSteps(LocationPath locationPath, Node node, XPathExprEnv env) throws JaxenException {
    @SuppressWarnings("unchecked")
    ArrayList<org.jaxen.expr.Step> steps = (ArrayList<org.jaxen.expr.Step>)xpath(node, env);
    for(org.jaxen.expr.Step step: steps) {
      locationPath.addStep(step);
    }
  }
  
  // ---
  
  public Object flwor(Flwor flwor, EvalEnv evalEnv) {
    Scope scope = evalEnv.getScope();
    String newVarName = flwor.getId().getValue();
    if (scope.localExists(newVarName)) {
      throw RT.error("variable %s already exists", newVarName);
    }
    
    EvalEnv newEnv = new EvalEnv(new Scope(evalEnv.getScope()), evalEnv.getEchoer(), evalEnv.getLabel());
    
    Iterator<ForOrLet> it = flwor.getForOrLetPlus().iterator();
    ArrayList<Object> arrayList = new ArrayList<Object>();
    forOrLets(it, flwor.getXwhereOptional(), flwor.getExpr(), arrayList, newEnv);
    
    Xorderby xorderby = flwor.getXorderbyOptional();
    if (xorderby != null) {
      //orderBy(arrayList, name, xorderby, newEnv);
      throw RT.error("xquery 'order by' is not supported yet");
    }
    
    Var var = new Var(newVarName, true, toArray(arrayList));
    scope.register(var);
    return null;
  }
  
  private void forOrLets(Iterator<ForOrLet> forOrLets, Xwhere xwhere, com.googlecode.phpreboot.ast.Expr returnExpr, ArrayList<Object> list, EvalEnv env) {
    if (!forOrLets.hasNext()) {
      if (xwhere != null && !filter(xwhere, env)) {
        return;
      }
      list.add(Evaluator.INSTANCE.eval(returnExpr, env));
      return;
    }
    
    ForOrLet forOrLet = forOrLets.next();
    if (forOrLet instanceof ForOrLetLet) { // it's a let
      Evaluator.INSTANCE.eval(forOrLet, env);
      forOrLets(forOrLets, xwhere, returnExpr, list, env);
    } else {
      ForClause forClause = ((ForOrLetFor)forOrLet).getForClause();
      String name = forClause.getId().getValue();
      Scope scope = env.getScope();
      if (scope.localExists(name)) {
        throw RT.error("variable %s already defined", name);
      }
      
      Var varLoop = new Var(name, false/*FIXME*/, null); 
      scope.register(varLoop);
      for(Object o: forClause(forClause, env)) {
        varLoop.setValue(o);
        forOrLets(forOrLets, xwhere, returnExpr, list, env);
      }
    }
  }
  
  private List<Object> forClause(ForClause forClause, EvalEnv evalEnv) {
    String rootvarName = forClause.getId2().getValue();
    Var rootVar = evalEnv.getScope().lookup(rootvarName);
    Object node = rootVar.getValue();
    if (!(node instanceof XML)) {
      throw RT.error("variable %s value must be a XML tree: %s", rootvarName, node);
    }
    
    //XPathEnv xpathEnv = new XPathEnv(evalEnv);
    //String xpathExpr = XPathVisitor.INSTANCE.xpath(instr_xpath.getLocationPath(), xpathEnv).toString();
    //System.err.println("xpath eval "+xpathExpr);
    
    ContextSupport support = new ContextSupport( 
        new SimpleNamespaceContext(),
        XPathFunctionContext.getInstance(),
        new SimpleVariableContext(),
        new XMLNavigator());
    Context context = new Context( support );
    context.setNodeSet(Collections.singletonList(node));
    
    try {
      XPathExpr xpathExpr = xpathExpr(new DefaultXPathFactory(), forClause.getLocationPath(), evalEnv);
      //System.out.println("xpath expr: "+xpathExpr);
      return xpathExpr.asList(context);
    } catch (JaxenException e) {
      //throw RT.error("xpath evaluation error %s", xpathExpr);  
      throw RT.error(e);
    }
  }
  
  private boolean filter(Xwhere xwhere, EvalEnv env) {
    Object condition = Evaluator.INSTANCE.eval(xwhere.getExpr(), env);
    if (!(condition instanceof Boolean)) {
      throw RT.error("where condition must be a boolean: %s", condition);
    }
    return (Boolean)condition;
  }
  
  private void orderBy(List<Object> list, String name, Xorderby xorderby, final EvalEnv env) {
    final List<com.googlecode.phpreboot.ast.Expr> exprs = xorderby.getExprPlus();
    Scope scope = new Scope(env.getScope());
    final Var var = new Var(name, false, null);
    scope.register(var);
    final EvalEnv newEnv = new EvalEnv(scope, env.getEchoer(), env.getLabel());
    
    Comparator<Object> comparator = new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        for(com.googlecode.phpreboot.ast.Expr expr: exprs) {
          Object key1 = Evaluator.INSTANCE.eval(expr, newEnv);
          var.setValue(o2);
          Object key2 = Evaluator.INSTANCE.eval(expr, newEnv);
          
          int compare;
          if (key1.getClass() == key2.getClass() && key1 instanceof Comparable<?>) {  // nullcheck
            compare = ((Comparable)key1).compareTo(key1);
          } else {
            compare = key1.toString().compareTo(key2.toString());
          }
          if (compare != 0)
            return compare;
        }
        return 0;
      }
    };
    
    Collections.sort(list, comparator);
  }
  
  private Array toArray(List<?> list) {
    Array array = new Array();
    for(Object o: list) {
      array.add(o);
    }
    return array;
  }
  
  
  private XPathExpr xpathExpr(XPathFactory factory, Node node, EvalEnv evalEnv) throws JaxenException {
    Expr expr = (Expr)xpath(node, new XPathExprEnv(factory, Axis.INVALID_AXIS, evalEnv));
    return factory.createXPath(expr);
  }
  
  private Object xpath(Node node, XPathExprEnv env) throws JaxenException {
    return node.accept(this, env);
  }
  
  @Override
  public Object visit(LocationPathAbsolute location_path_absolute, XPathExprEnv env) throws JaxenException {
    return xpath(location_path_absolute.getAbsoluteLocationPath(), env);
  }
  
  @Override
  public Object visit(LocationPathRelative location_path_relative, XPathExprEnv env) throws JaxenException {
    LocationPath relativeLocationPath = env.getFactory().createRelativeLocationPath();
    gatherSteps(relativeLocationPath, location_path_relative.getRelativeLocationPath(), env);
    return relativeLocationPath;
  }
  
  // php.reboot runtime has no top-level XML root document so there is no "absolute path"
  @Override
  public Object visit(AbsoluteLocationPathSlash absolute_location_path_slash, XPathExprEnv env) throws JaxenException {
    //return env.getFactory().createAbsoluteLocationPath();
    return env.getFactory().createRelativeLocationPath();
  }
  @Override
  public Object visit(AbsoluteLocationPathRelative absolute_location_path_relative, XPathExprEnv env) throws JaxenException {
    //LocationPath absoluteLocationPath = env.getFactory().createAbsoluteLocationPath();
    LocationPath absoluteLocationPath = env.getFactory().createRelativeLocationPath();
    gatherSteps(absoluteLocationPath, absolute_location_path_relative.getRelativeLocationPath(), env);
    return absoluteLocationPath;
  }
  @Override
  public Object visit(AbsoluteLocationPathAbbreviated absolute_location_path_abbreviated, XPathExprEnv env) throws JaxenException {
    //LocationPath absoluteLocationPath = env.getFactory().createAbsoluteLocationPath();
    LocationPath absoluteLocationPath = env.getFactory().createRelativeLocationPath();
    
    absoluteLocationPath.addStep(env.getFactory().createAllNodeStep(Axis.DESCENDANT_OR_SELF));
    gatherSteps(absoluteLocationPath, absolute_location_path_abbreviated.getRelativeLocationPath(), env);
    return absoluteLocationPath;
  }
  
  @Override
  public Object visit(RelativeLocationPathStep relative_location_path_step, XPathExprEnv env) throws JaxenException {
    ArrayList<Step> steps = new ArrayList<Step>();
    steps.add((Step)xpath(relative_location_path_step.getStep(), env));
    return steps;
  }
  
  @Override
  public Object visit(RelativeLocationPathPathStep relative_location_path_path_step, XPathExprEnv env) throws JaxenException {
    @SuppressWarnings("unchecked")
    ArrayList<Step> steps = (ArrayList<Step>)xpath(relative_location_path_path_step.getRelativeLocationPath(), env);
    steps.add((Step)xpath(relative_location_path_path_step.getStep(), env));
    return steps;
  }
  @Override
  public Object visit(RelativeLocationPathAbbreviated relative_location_path_abbreviated, XPathExprEnv env) throws JaxenException {
    @SuppressWarnings("unchecked")
    ArrayList<Step> steps = (ArrayList<Step>)xpath(relative_location_path_abbreviated.getRelativeLocationPath(), env);
    steps.add(env.getFactory().createAllNodeStep(Axis.DESCENDANT_OR_SELF));
    steps.add((Step)xpath(relative_location_path_abbreviated.getStep(), env));
    return steps;
  }
  
  @Override
  public Object visit(StepDot step_dot, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createAllNodeStep(Axis.SELF);
  }
  @Override
  public Object visit(StepDoubleDot step_double_dot, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createAllNodeStep(Axis.PARENT);
  }
  @Override
  public Object visit(StepNodeTest step_node_test, XPathExprEnv env) throws JaxenException {
    XPathExprEnv newEnv = new XPathExprEnv(env.getFactory(), Axis.CHILD, env.getEvalEnv());
    Step step = (Step)xpath(step_node_test.getNodeTest(), newEnv);
    for(Predicate predicate: step_node_test.getPredicateStar()) {
      step.addPredicate((org.jaxen.expr.Predicate)xpath(predicate, newEnv));
    }
    return step;
  }
  @Override
  public Object visit(StepNodeTestAxis step_node_test_axis, XPathExprEnv env) throws JaxenException {
    int axis = (Integer)xpath(step_node_test_axis.getAxisSpecifier(), env);
    XPathExprEnv newEnv = new XPathExprEnv(env.getFactory(), axis, env.getEvalEnv());
    Step step = (Step)xpath(step_node_test_axis.getNodeTest(), newEnv);
    for(Predicate predicate: step_node_test_axis.getPredicateStar()) {
      step.addPredicate((org.jaxen.expr.Predicate)xpath(predicate, newEnv));
    }
    return step;
  }
  
  @Override
  public Object visit(AxisSpecifierName axis_specifier_name, XPathExprEnv env) throws JaxenException {
    int axis = Axis.lookup(axis_specifier_name.getAxisName().getValue());
    assert axis != Axis.INVALID_AXIS;
    return axis;
  }
  @Override
  public Object visit(AxisSpecifierAbbreviated axis_specifier_abbreviated, XPathExprEnv env) throws JaxenException {
    return Axis.ATTRIBUTE;
  }
  
  @Override
  public Object visit(NodeTestStar node_test_star, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createAllNodeStep(env.getInheritedAxis());
  }
  @Override
  public Object visit(NodeTestId node_test_id, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createNameStep(env.getInheritedAxis(), "", node_test_id.getId().getValue());
  }
  @Override
  public Object visit(NodeTestIdStar node_test_id_star, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createNameStep(env.getInheritedAxis(), node_test_id_star.getId().getValue(), "*");
  }
  @Override
  public Object visit(NodeTestQualified node_test_qualified, XPathExprEnv env) throws JaxenException {
    StringBuilder builder = new StringBuilder();
    builder.append(node_test_qualified.getId().getValue());
    for(IdToken token: node_test_qualified.getIdPlus()) {
      builder.append('.').append(token.getValue());
    }
    return env.getFactory().createNameStep(env.getInheritedAxis(), "", builder.toString());
  }
  @Override
  public Object visit(NodeTestType node_test_type, XPathExprEnv env)throws JaxenException {
    String type = node_test_type.getNodeType().getValue();
    if ("node".equals(type)) {
      return env.getFactory().createAllNodeStep(env.getInheritedAxis());
    }
    if ("text".equals(type)) {
      return env.getFactory().createTextNodeStep(env.getInheritedAxis());
    }
    throw RT.error("test for node type %s is not implemented", type);
  }
  
  
  // --- predicates
  
  @Override
  public Object visit(Predicate predicate, XPathExprEnv env) throws JaxenException {
    return env.getFactory().createPredicate((Expr)xpath(predicate.getXpathExpr(), env));
  }
  
  private Object xpathEscape(Node node, XPathExprEnv env) throws JaxenException {
    Object o = Evaluator.INSTANCE.eval(node, env.getEvalEnv());
    if (o instanceof String) {
      return env.getFactory().createLiteralExpr((String)o);
    }
    if (o instanceof Integer) {
      return env.getFactory().createNumberExpr((Integer)o);
    }
    if (o instanceof Double) {
      return env.getFactory().createNumberExpr((Double)o);
    }
    throw RT.error("invalid literal %s for xpath", o);
  }
  @Override
  public Object visit(XpathLiteral xpath_literal, XPathExprEnv env) throws JaxenException {
    return xpathEscape(xpath_literal.getSingleLiteral(), env);
  }
  @Override
  public Object visit(XpathExprDollarAccess xpath_expr_dollar_access, XPathExprEnv env) throws JaxenException {
    return xpathEscape(xpath_expr_dollar_access.getDollarAccess(), env);
  }
  
  @Override
  public Object visit(XpathExprLocationPath xpath_expr_location_path, XPathExprEnv env) throws JaxenException {
    return xpath(xpath_expr_location_path.getLocationPath(), env);
  }
  
  @Override
  protected Object visit(XpathExpr xpath_expr, XPathExprEnv env) throws JaxenException {
    List<Node> nodeList = xpath_expr.nodeList();
    Expr expr = (Expr)xpath(nodeList.get(0), env); 
    XPathFactory factory = env.getFactory();
    ProductionEnum kind = xpath_expr.getKind();
    switch(kind) {
    case xpath_expr_unary_not:
      throw RT.error("NYI");
    case xpath_expr_unary_minus:
      return factory.createUnaryExpr(expr, Operator.NEGATIVE);
    case xpath_expr_unary_plus:
      return expr;
    default:
    }
    
    Expr left = expr;
    Expr right = (Expr)xpath(nodeList.get(1), env);
    switch(kind) {
    case xpath_expr_eq:
      return factory.createEqualityExpr(left, right, Operator.EQUALS);
    case xpath_expr_ne:
      return factory.createEqualityExpr(left, right, Operator.NOT_EQUALS);
    case xpath_expr_lt:
      return factory.createRelationalExpr(left, right, Operator.LESS_THAN);
    case xpath_expr_le:
      return factory.createRelationalExpr(left, right, Operator.LESS_THAN_EQUALS);
    case xpath_expr_gt:
      return factory.createRelationalExpr(left, right, Operator.GREATER_THAN);
    case xpath_expr_ge:
      return factory.createRelationalExpr(left, right, Operator.GREATER_THAN_EQUALS);
    case xpath_expr_plus:
      return factory.createAdditiveExpr(left, right, Operator.ADD);
    case xpath_expr_minus:
      return factory.createAdditiveExpr(left, right, Operator.SUBTRACT);
    case xpath_expr_mult:
      return factory.createMultiplicativeExpr(left, right, Operator.MULTIPLY);
    case xpath_expr_div:
      return factory.createMultiplicativeExpr(left, right, Operator.DIV);
    case xpath_expr_mod:
      return factory.createMultiplicativeExpr(left, right, Operator.MOD);
    case xpath_expr_and:
      return factory.createAndExpr(left, right);
    case xpath_expr_or:
      return factory.createOrExpr(left, right);
    default:
      throw new AssertionError("no xpath node for production "+kind);
    }
  }
}
