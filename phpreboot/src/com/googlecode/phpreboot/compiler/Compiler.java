package com.googlecode.phpreboot.compiler;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.runtime.RT;

public class Compiler {
  private static int counter;
  
  public static MethodHandle compile(Function function) {
    String name = function.getName();
    
    LocalScope localScope = new LocalScope(function.getScope());
    for(Parameter parameter: function.getParameters()) {
      Type type = parameter.getType();
      localScope.register(new LocalVar(parameter.getName(), true, type, localScope.nextSlot(type)));
    }
     
    Block functionNode = function.getBlock();
    TypeChecker typeChecker = new TypeChecker();
    BindMap bindMap = new BindMap();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, function.getReturnType(), bindMap);
    
    Type liveness;
    try {
      liveness = typeChecker.typeCheck(functionNode, typeCheckEnv);
    } catch(CodeNotCompilableException e) {
      return null;
    }
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "GenStub$"+(counter++)+'$'+name, null, "java/lang/Object", null);
    cw.visitSource("script", null);
    
    MethodType methodType = asMethodType(function, bindMap);
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, name, desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(mv);
    gen.gen(functionNode, new GenEnv(bindMap.getSlotCount(), null));
    if (liveness == LivenessType.ALIVE) {
      gen.defaultReturn(function.getReturnType());
    }
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    generateStaticInit(cw);
    
    cw.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    //CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    
    MethodHandle mh = define(name, array, methodType);
    if (bindMap.getSlotCount() != 0) {
      mh = MethodHandles.insertArguments(mh, 0, bindMap.getReferences());
    }
    
    //System.err.println("compiled method "+mh.type());
    
    return mh;
  }
  
  private static void generateStaticInit(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
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
      switch((PrimitiveType)type) {
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
    for(int i=0; i<count; i++) {
      parameterArray[i] = bindMap.getReferenceClass(i);
    }
    parameterArray[count] = /*EvalEnv.class*/ Object.class;
    for(int i = 0; i < parameters.size(); i++) {
      parameterArray[count + 1 + i] = asClass(parameters.get(i).getType());
    }
    return MethodType.methodType(asClass(function.getReturnType()), parameterArray);
  }
  
  private static MethodHandle define(String name, byte[] bytecodes, MethodType methodType) {
    Class<?> declaredClass;
    if (ANONYMOUS_CLASS_DEFINE != null) {
      declaredClass = AnonymousLoader.define(bytecodes);
    } else {
      declaredClass = StandardLoader.define(bytecodes);
    }
    return MethodHandles.lookup().findStatic(declaredClass, name, methodType);
  }
  
  static final MethodHandle ANONYMOUS_CLASS_DEFINE;
  static {
    MethodHandle define;
    try {
    Class<?> anonymousClassLoaderClass = Class.forName("sun.dyn.anon.AnonymousClassLoader");
    define = MethodHandles.publicLookup().findVirtual(anonymousClassLoaderClass, "loadClass",
        MethodType.methodType(Class.class, byte[].class));
    
    } catch(ClassNotFoundException e) {
      define = null;
    }
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
        //FIXME use invokeExact instead
        return ANONYMOUS_CLASS_DEFINE.invokeGeneric(ANONYMOUS_CLASS_LOADER, bytecodes);
      } catch(Throwable t) {
        t.printStackTrace();
        throw new AssertionError(t);
      }
    }
  }
  
  static class StandardLoader extends ClassLoader {
    private static final StandardLoader STANDARD_LOADER = new StandardLoader();
    
    static Class<?> define(byte[] bytecodes) {
      return STANDARD_LOADER.defineClass(null, bytecodes, 0, bytecodes.length);
    }
  }
}
