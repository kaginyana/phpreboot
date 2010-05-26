package com.googlecode.phpreboot.compiler;

import jsr292.weaver.OnlineWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class LegacyWeaver extends OnlineWeaver {
  public LegacyWeaver(ClassVisitor cv, boolean retransform) {
    super(cv, retransform);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    if (version >= Opcodes.V1_7)
      version = Opcodes.V1_5;

    super.visit(version, access, name, signature, superName, interfaces);
  }

  public static ClassVisitor weave(ClassVisitor cv) {
    return new LegacyWeaver(cv, true);
  }
}