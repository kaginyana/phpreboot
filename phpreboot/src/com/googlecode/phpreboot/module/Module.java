package com.googlecode.phpreboot.module;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.lang.reflect.Method;

import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Var;

public abstract class Module {
  private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
  
  public void registerModule(Scope scope) {
    for(Method method: this.getClass().getMethods()) {
      if (!method.isAnnotationPresent(Export.class))
        continue;
      
      MethodHandle mh = PUBLIC_LOOKUP.unreflect(method);
      
      // bind the method handles to the current module
      mh = MethodHandles.insertArguments(mh, 0, this);
      
      // add a unused parameter for the environment
      mh = MethodHandles.dropArguments(mh, 0, Object.class/*EvalEnv.class*/);
      
      // generify signature
      mh = MethodHandles.convertArguments(mh, mh.type().generic());
      
      // FIXME
      Function function = new Function(method.getName(), null, null, null, null);
      function.setMethodHandle(mh);
      
      Var var = new Var(method.getName(), true, PrimitiveType.ANY, function);
      scope.register(var);
    }
  }
}
