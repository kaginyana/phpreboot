package com.googlecode.phpreboot.interpreter;

import java.dyn.MethodHandle;
import java.util.List;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.compiler.BindMap;
import com.googlecode.phpreboot.compiler.LocalVar;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.runtime.RT;

public interface Profile {
  // marker interface
  
  public class LoopProfile implements Profile {
    int counter;
    private /*@Nullable*/BindMap bindMap;
    private /*@Nullable*/MethodHandle trace;
    
    public void recordTrace(BindMap bindMap, MethodHandle trace) {
      this.bindMap = bindMap;
      this.trace = trace;
    }

    boolean hasATrace() {
      return bindMap != null;
    }
    
    boolean callTrace(EvalEnv env) {
      // check if bindMap is compatible with current env
      // and create the trace parameter array
      List<LocalVar> references = bindMap.getReferences();
      int size = references.size();
      Object[] args = new Object[2 * size + 1];
      for(int i=0; i<size; i++) {
        LocalVar localVar = references.get(i);
        Var var = env.getScope().lookup(localVar.getName());
        Object value = var.getValue();
        args[i + 1] = value;
        args[i + size + 1] = var;
        if (!localVar.isOptimistic()) {
          continue;
        }
        
        // check is optimistic type is still valid
        if (!localVar.getType().getRuntimeClass().isInstance(value)) {
          bindMap = null;
          trace = null;
          return false;
        }
      }
      
      args[0]=env;
      
      try {
        trace.invokeVarargs(args);
      } catch(Error e) {
        throw e;
      } catch (Throwable e) {
        throw RT.error((Node)null, e);
      }
      return true;
    }
  }
  
  public class VarProfile implements Profile {
    Var var;
    
    public Var getVar() {
      return var;
    }
  }
}
