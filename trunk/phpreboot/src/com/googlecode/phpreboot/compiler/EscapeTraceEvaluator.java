/**
 * 
 */
package com.googlecode.phpreboot.compiler;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.ElseIf;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrBlock;
import com.googlecode.phpreboot.ast.InstrIf;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;

public class EscapeTraceEvaluator extends Visitor<Void, Node, RuntimeException> {
  static LocalVar createEscapeTraceLocalVar(BindMap bindMap, TypeProfileMap typeProfileMap, Node node, Node traceNode, Scope scope, LinkedHashMap<LocalVar,Var> varMapAssoc) {
    Var[] vars = varMapAssoc.values().toArray(new Var[varMapAssoc.size()]);
    MethodHandle mh = MethodHandles.insertArguments(EscapeTraceEvaluator.ESCAPE_TRACE_MH, 0, node, traceNode, scope, vars);
    mh = MethodHandles.collectArguments(mh, asMethodType(varMapAssoc));
    
    LocalVar localVar = bindMap.bind("<escape>", true, true, mh, EscapeTraceEvaluator.METHOD_HANDLE_TYPE, false, typeProfileMap, null);
    return localVar;
  }
  
  static MethodType asMethodType(LinkedHashMap<LocalVar,Var> varMapAssoc) {
    Class<?>[] parameterArray = new Class<?>[1 + varMapAssoc.size()];
    parameterArray[0] = EvalEnv.class;
    int i = 1;
    for(LocalVar var: varMapAssoc.keySet()) {
      parameterArray[i++] = Compiler.asClass(var.getType());
    }
    return MethodType.methodType(void.class, parameterArray);
  }
  
  public static void escapeTrace(Node node, Node traceNode, Scope scope, Var[] vars, EvalEnv evalEnv, Object[] args) {
    //update vars
    assert vars.length == args.length;
    for(int i=0; i< vars.length; i++) {
      vars[i].setValue(args[i]);
    }
    
    EvalEnv newEnv = new EvalEnv(scope, evalEnv.getEchoer());
    Evaluator.INSTANCE.eval(node, newEnv);
    
    // escape from the trace
    new EscapeTraceEvaluator(traceNode, newEnv).evalEscape(node.getParent(), node);
  }
  
  private final Node rootTraceNode;
  private EvalEnv env;
  
  private EscapeTraceEvaluator(Node rootTraceNode, EvalEnv env) {
    this.rootTraceNode = rootTraceNode;
    this.env = env;
  }

  private void evalEscape(Node parent, Node child) {
    if (parent == rootTraceNode)
      return;
    parent.accept(this, child);
  }
  
  @Override
  protected Void visit(Node parent, Node child) {
    throw new UnsupportedOperationException("No eval escape for node "+parent.getKind());
  }
  
  @Override
  public Void visit(Block parent, Node child) {
    Iterator<Instr> it = parent.getInstrStar().iterator();
    for(;;) { // skip first nodes
      if (it.next() == child) {
        break; 
      }
    }

    Evaluator evaluator = Evaluator.INSTANCE;
    EvalEnv env = this.env;
    while(it.hasNext()) {
      Instr instr = it.next();
      evaluator.eval(instr, env);
    }
    
    this.env = new EvalEnv(env.getScope().getParent(), env.getEchoer());
    evalEscape(parent.getParent(), parent);
    return null;
  }
  
  @Override
  public Void visit(InstrBlock instr_block, Node child) {
    evalEscape(instr_block.getParent(), instr_block);
    return null;
  }
  
  @Override
  public Void visit(InstrIf instr_if, Node child) {
    evalEscape(instr_if.getParent(), instr_if);
    return null;
  }
  @Override
  protected Void visit(ElseIf elseIf, Node child) {
    evalEscape(elseIf.getParent(), child);
    return null;
  }
  
  static final MethodHandle ESCAPE_TRACE_MH;
  static final Type METHOD_HANDLE_TYPE = new Type() {
    @Override
    public String getName() {
      return "<MethodHandle>";
    }
    @Override
    public Class<?> getRuntimeClass() {
      return MethodHandle.class;
    }
    @Override
    public Class<?> getUnboxedRuntimeClass() {
      return MethodHandle.class;
    }
    @Override
    public String toString() {
      return getName();
    }
  };
  static {
    ESCAPE_TRACE_MH = MethodHandles.publicLookup().findStatic(EscapeTraceEvaluator.class, "escapeTrace",
        MethodType.methodType(void.class, Node.class, Node.class, Scope.class, Var[].class, EvalEnv.class, Object[].class));
  }
}