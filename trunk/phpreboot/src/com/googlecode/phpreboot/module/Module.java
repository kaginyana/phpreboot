package com.googlecode.phpreboot.module;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.IntrinsicInfo;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;

public abstract class Module {
  private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
  
  public void registerModule(Scope scope) {
    for(Method method: this.getClass().getMethods()) {
      if (!method.isAnnotationPresent(Export.class))
        continue;
      
      MethodHandle mh;
      try {
        mh = PUBLIC_LOOKUP.unreflect(method);
      } catch (IllegalAccessException e) {
        throw (AssertionError)new AssertionError().initCause(e);
      }
      
      if (!Modifier.isStatic(method.getModifiers())) {
        throw new AssertionError("method in module must be static "+method);
      }
      
      // add a unused parameter for the environment if needed
      if (!method.isAnnotationPresent(RequireEnv.class)) {
        mh = MethodHandles.dropArguments(mh, 0, EvalEnv.class);
      }
      
      Function function = createFunction(method);
      function.setMethodHandle(mh, true);
      
      Var var = new Var(method.getName(), true, true, PrimitiveType.ANY, function);
      scope.register(var);
    }
  }
  
  private static Function createFunction(Method method) {
    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
    Class<?>[] parameterTypes = method.getParameterTypes();
    for(int i=0; i< parameterTypes.length; i++) {
      Parameter parameter = new Parameter("arg"+i, asType(parameterTypes[i]), null);
      parameters.add(parameter);
    }
    
    Class<?> declaringClass;
    String name;
    IntrinsicMethod intrinsic = method.getAnnotation(IntrinsicMethod.class);
    if (intrinsic != null) {
      declaringClass = intrinsic.declaringClass();
      name = intrinsic.name();
    } else {
      declaringClass = method.getDeclaringClass();
      name = method.getName();
    }
    
    IntrinsicOpcode intrinsicOpcode = method.getAnnotation(IntrinsicOpcode.class);
    int opcode = (intrinsicOpcode == null)? -1: intrinsicOpcode.value();
    
    IntrinsicInfo instrinsicInfo = new IntrinsicInfo(declaringClass, name, opcode);
    return new Function(method.getName(), parameters, asType(method.getReturnType()), null, instrinsicInfo, null, null);
  }
  
  private static Type asType(Class<?> runtimeClass) {
    //FIXME finish implementation and move the method in package runtime
    if (runtimeClass == boolean.class)
      return PrimitiveType.BOOLEAN;
    if (runtimeClass == int.class)
      return PrimitiveType.INT;
    if (runtimeClass == double.class)
      return PrimitiveType.DOUBLE;
    if (runtimeClass == String.class)
      return PrimitiveType.STRING;
    if (runtimeClass == void.class)
      return PrimitiveType.VOID;
    return PrimitiveType.ANY;
  }
}
