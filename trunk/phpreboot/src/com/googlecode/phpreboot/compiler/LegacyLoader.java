package com.googlecode.phpreboot.compiler;

import jsr292.weaver.OnlineWeaver;
import jsr292.weaver.opt.Optimizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.googlecode.phpreboot.compiler.Compiler.StandardLoader;

class LegacyLoader extends ClassLoader {
  // downgrade version to 1.5
  static class LegacyWeaver extends OnlineWeaver {
      public LegacyWeaver(ClassVisitor cv, boolean retransform) {
          super(cv, retransform);
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
          if (version >= Opcodes.V1_7)
              version = Opcodes.V1_5;
          
          super.visit(version, access, name, signature, superName, interfaces);
      }
  }
  
  static Class<?> define(String className, byte[] bytecodes) {
    try {
      ClassReader reader = new ClassReader(bytecodes);
      ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

      
      reader.accept(new LegacyWeaver(writer, true), ClassReader.SKIP_FRAMES);
      

      //System.out.println("instrument "+className);

      byte[] byteArray = writer.toByteArray();
      //CheckClassAdapter.verify(new ClassReader(byteArray), loader, true, new PrintWriter(System.out));

      //if (retransformSupported) {
        Optimizer.registerClassDefinition(className, byteArray);
      //}
      return StandardLoader.define(className, byteArray);

    } catch(Throwable t) {
      System.err.println("JSR292 Backport Agent Error:");
      t.printStackTrace();
      throw (LinkageError)new LinkageError().initCause(t);
    }
  }
}