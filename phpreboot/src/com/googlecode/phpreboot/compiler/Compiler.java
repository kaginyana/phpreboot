package com.googlecode.phpreboot.compiler;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Objects;

import jsr292.weaver.opt.Optimizer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.compiler.LoopStack.Labels;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Profile.LoopProfile;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.runtime.RT;

public class Compiler {
  private static int counter;
  
  public static MethodHandle compile(Function function) {
    String name = function.getName();
    
    LocalScope localScope = new LocalScope(function.getScope());
    localScope.register(new Var(function.getName(), true, PrimitiveType.ANY, function));
    for(Parameter parameter: function.getParameters()) {
      Type type = parameter.getType();
      localScope.register(new LocalVar(parameter.getName(), true, type, false, localScope.nextSlot(type)));
    }
     
    Block functionNode = function.getBlock();
    TypeChecker typeChecker = new TypeChecker();
    LoopStack<Boolean> loopStack = new LoopStack<Boolean>();
    BindMap bindMap = new BindMap();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, loopStack, function.getReturnType(), bindMap, false);
    
    Type liveness;
    try {
      liveness = typeChecker.typeCheck(functionNode, typeCheckEnv);
    } catch(CodeNotCompilableException e) {
      return null;
    }
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    ClassVisitor cv = cw;
    if (LEGACY_MODE) {
      cv = LegacyWeaver.weave(cw);
    }
    String className = "GenStub$"+(counter++)+'$'+name;
    cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
    cv.visitSource("script", null);
    
    MethodType methodType = asMethodType(function, bindMap);
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, name, desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(mv);
    LoopStack<Labels> labelLoopStack = new LoopStack<Labels>();
    gen.gen(functionNode, new GenEnv(bindMap.getSlotCount(), null, labelLoopStack, null));
    if (liveness == LivenessType.ALIVE) {
      gen.defaultReturn(function.getReturnType());
    }
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    generateStaticInit(cv);
    
    cv.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    //bindMap.dump();
    //CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    
    MethodHandle mh = define(className, name, array, methodType);
    
    List<LocalVar> bindReferences = bindMap.getReferences();
    if (!bindReferences.isEmpty()) {
      int size = bindReferences.size();
      Object[] boundArray = new Object[size];
      for(int i=0; i<size; i++) {
        boundArray[i] = bindReferences.get(i).getValue(); 
      }
      
      mh = MethodHandles.insertArguments(mh, 1, boundArray);
    }
    
    //System.err.println("compiled method "+mh.type());
    
    return mh;
  }
  
  
  public static boolean traceCompile(LabeledInstrWhile labeledInstrWhile, LoopProfile profile, boolean optimisticTrace, EvalEnv env) {
    Scope scope = env.getScope();
    LocalScope localScope = new LocalScope(scope);
    
    TypeChecker typeChecker = new TypeChecker();
    LoopStack<Boolean> loopStack = new LoopStack<Boolean>();
    BindMap bindMap = new BindMap();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, loopStack, /*FIXME need the enclosing return type*/null, bindMap, optimisticTrace);
    
    try {
      typeChecker.typeCheck(labeledInstrWhile, typeCheckEnv);
    } catch(CodeNotCompilableException e) {
      return false;
    } catch(OptimiticAssertionException e) {
      System.err.println("optimistic typecheck failed");
      // typecheck again but don't perform optimistic assumption
      bindMap = new BindMap();
      localScope = new LocalScope(scope);
      typeCheckEnv = new TypeCheckEnv(localScope, loopStack, /*FIXME need the enclosing return type*/null, bindMap, false);
      try {
        typeChecker.typeCheck(labeledInstrWhile, typeCheckEnv);
      } catch(CodeNotCompilableException e2) {
        return false;
      }
    }
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    ClassVisitor cv = cw;
    if (LEGACY_MODE) {
      cv = LegacyWeaver.weave(cw);
    }
    String className = "GenStub$"+(counter++)+"$trace";
    cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
    cv.visitSource("script", null);
    
    MethodType methodType = asTraceMethodType(bindMap);
    
    //XXX ricochet not yet implemented in jdk7
    if (!LEGACY_MODE && methodType.parameterCount() > 10) {
      System.err.println("trace:"+methodType);
      System.err.println("ricochet not yet implemented, go back in interpreter mode");
      return false;
    }
    
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "trace", desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(mv);
    LoopStack<Labels> labelLoopStack = new LoopStack<Labels>();
    gen.gen(labeledInstrWhile, new GenEnv(bindMap.getSlotCount() + bindMap.getReferencesCount(), null, labelLoopStack, null));
    
    // restore env vars
    List<LocalVar> references = bindMap.getReferences();
    int size = references.size();
    int outputVarCount = bindMap.getOutputVarCount();
    Object[] args = new Object[size + outputVarCount + 1];
    args[0] = env;
    if (size != 0) {
      gen.restoreEnv(references, bindMap.getSlotCount() -1 /*XXX substract env slot */, scope, args);
    }
    
    mv.visitInsn(Opcodes.RETURN);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    generateStaticInit(cv);
    
    cv.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    //bindMap.dump();
    //CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    
    MethodHandle mh = define(className, "trace", array, methodType);
    
    // record for reuse
    profile.recordTrace(bindMap, mh);
    
    // debug
    //System.err.println("calls "+java.util.Arrays.toString(args));
    
    try {
      mh.invokeVarargs(args);
    } catch(Error e) {
      throw e;
    } catch (Throwable e) {
      throw RT.error((Node)null, e);
    }
    
    //System.err.println("compiled method "+mh.type());
    
    return true;
  }
  
  
  private static void generateStaticInit(ClassVisitor cv) {
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    
    mv.visitLdcInsn(org.objectweb.asm.Type.getType(RT.class));
    mv.visitLdcInsn("bootstrap");
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/dyn/Linkage", "registerBootstrapMethod", "(Ljava/lang/Class;Ljava/lang/String;)V");
    mv.visitInsn(Opcodes.RETURN);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static Class<?> asClass(Type type) {
    if (type instanceof PrimitiveType) {
      switch((PrimitiveType)type) { // use primitive type instead of their wrapper
      case BOOLEAN:
        return boolean.class;
      case INT:
        return int.class;
      case DOUBLE:
        return double.class;
      default:
        return type.getRuntimeClass();
      }
    }
    return void.class;
  }
  
  private static MethodType asMethodType(Function function, BindMap bindMap) {
    List<Parameter> parameters = function.getParameters();
    int count = bindMap.getReferencesCount();
    Class<?>[] parameterArray = new Class<?>[count + 1 + parameters.size()];
    parameterArray[0] = /*EvalEnv.class*/ Object.class;
    for(int i=0; i<count; i++) {
      parameterArray[i + 1] = asClass(bindMap.getReferenceType(i));
    }
    for(int i = 0; i < parameters.size(); i++) {
      parameterArray[i + count + 1] = asClass(parameters.get(i).getType());
    }
    return MethodType.methodType(asClass(function.getReturnType()), parameterArray);
  }
  
  private static MethodType asTraceMethodType(BindMap bindMap) {
    int count = bindMap.getReferencesCount();
    int outputVarsCount = bindMap.getOutputVarCount();
    Class<?>[] parameterArray = new Class<?>[count + outputVarsCount + 1];
    parameterArray[0] = /*EvalEnv.class*/ Object.class;
    for(int i=0; i<count; i++) {
      parameterArray[i + 1] = asClass(bindMap.getReferenceType(i));
    }
    for(int i=0; i<outputVarsCount; i++) {
      parameterArray[i + count + 1] = Var.class;
    }
    return MethodType.methodType(void.class, parameterArray);
  }
  
  
  // --- profile
  
  public static Type inferType(Object value) {
    if (value instanceof Boolean) {
      return PrimitiveType.BOOLEAN;
    }
    if (value instanceof Integer) {
      return PrimitiveType.INT;
    }
    if (value instanceof Double) {
      return PrimitiveType.DOUBLE;
    }
    if (value instanceof String) {
      return PrimitiveType.STRING;
    }
    return PrimitiveType.ANY;
  }
  
  public static boolean enableVarProfile(Object value) {
    return inferType(value) != PrimitiveType.ANY;
  }
  
  
  // --- define
  
  private static MethodHandle define(String className, String name, byte[] bytecodes, MethodType methodType) {
    Class<?> declaredClass;
    if (ANONYMOUS_CLASS_DEFINE != null) {
      declaredClass = AnonymousLoader.define(bytecodes);
    } else {
      declaredClass = StandardLoader.define(className.replace('.', '/'), bytecodes);
    }
    return MethodHandles.lookup().findStatic(declaredClass, name, methodType);
  }
  
  static final MethodHandle ANONYMOUS_CLASS_DEFINE;
  static final boolean LEGACY_MODE;
  static {
    boolean legacyMode;
    MethodHandle define;
    
    try {
      Class.forName("jsr292.weaver.Agent");
      legacyMode = true;
      define = null;
      
    } catch(ClassNotFoundException e2) {
      legacyMode = false;
      try {
        Class<?> anonymousClassLoaderClass = Class.forName("sun.dyn.anon.AnonymousClassLoader");
        define = MethodHandles.publicLookup().findVirtual(anonymousClassLoaderClass, "loadClass",
            MethodType.methodType(Class.class, byte[].class));
      } catch(ClassNotFoundException e) {
        define = null;
      }
    }
    LEGACY_MODE = legacyMode;
    ANONYMOUS_CLASS_DEFINE = define;
  }
  
  static class AnonymousLoader {
    private static final Object ANONYMOUS_CLASS_LOADER;
    static {
      try {
        Class<?> anonymousClassLoaderClass = Class.forName("sun.dyn.anon.AnonymousClassLoader");
        Constructor<?> constructor = anonymousClassLoaderClass.getConstructor(Class.class);
        ANONYMOUS_CLASS_LOADER = constructor.newInstance((Object)null);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        }
        if (cause instanceof Error) {
          throw (Error)cause;
        }
        throw new UndeclaredThrowableException(cause);
      } catch(ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }
    static Class<?> define(byte[] bytecodes) {
      try {
        //XXX workaround bug in jdk7b94, should use invokeGeneric instead
        return (Class<?>)ANONYMOUS_CLASS_DEFINE.invokeVarargs(ANONYMOUS_CLASS_LOADER, bytecodes);
      } catch(Throwable t) {
        t.printStackTrace();
        throw new AssertionError(t);
      }
    }
  }
  
  static class StandardLoader extends ClassLoader {
    private static final StandardLoader STANDARD_LOADER = new StandardLoader();
    
    static Class<?> define(String className, byte[] bytecodes) {
      if (LEGACY_MODE) {
        Optimizer.registerClassDefinition(className, bytecodes);
      }
      
      return STANDARD_LOADER.defineClass(className, bytecodes, 0, bytecodes.length);
    }
  }
}
