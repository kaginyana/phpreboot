package com.googlecode.phpreboot.interpreter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.IdentityHashMap;

import com.googlecode.phpreboot.ast.Script;
import com.googlecode.phpreboot.compiler.Compiler;
import com.googlecode.phpreboot.compiler.LocalScope;
import com.googlecode.phpreboot.lexer.RuleEnum;
import com.googlecode.phpreboot.parser.NonTerminalEnum;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.parser.TerminalEnum;
import com.googlecode.phpreboot.parser.VersionEnum;
import com.googlecode.phpreboot.tools.Analyzers;
import com.googlecode.phpreboot.tools.GrammarEvaluator;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

import fr.umlv.tatoo.runtime.buffer.LexerBuffer;
import fr.umlv.tatoo.runtime.buffer.TokenBuffer;
import fr.umlv.tatoo.runtime.buffer.impl.LocationTracker;
import fr.umlv.tatoo.runtime.buffer.impl.ReaderWrapper;
import fr.umlv.tatoo.runtime.lexer.Lexer;
import fr.umlv.tatoo.runtime.lexer.RuleActivator;
import fr.umlv.tatoo.runtime.tools.SemanticStack;
import fr.umlv.tatoo.runtime.tools.builder.Builder.AnalyzerParserBuilder;

public class Analyzer {
  private Analyzer() {
    // enforce utility class
  }

  private static class CommentSwitcherRuleActivator implements RuleActivator<RuleEnum> {
    private final RuleActivator<RuleEnum> ruleActivator;
    private final ASTHandler astHandler;
    private final IdentityHashMap<RuleEnum[], RuleEnum[]> cache =
      new IdentityHashMap<RuleEnum[], RuleEnum[]>();
    
    CommentSwitcherRuleActivator(RuleActivator<RuleEnum> ruleActivator, ASTHandler astHandler) {
      this.ruleActivator = ruleActivator;
      this.astHandler = astHandler;
    }

    @Override
    public RuleEnum[] activeRules() {
      RuleEnum[] activeRules = ruleActivator.activeRules();
      if (astHandler.enableLineComment) {
        return activeRules;
      }
      RuleEnum[] rules = cache.get(activeRules);
      if (rules == null) {
        rules = suppressLineCommentRule(activeRules);
        cache.put(activeRules, rules);
      }
      return rules;
    }
    
    private static RuleEnum[] suppressLineCommentRule(RuleEnum[] activeRules) {
      RuleEnum[] rules = new RuleEnum[activeRules.length - 1];
      int j = 0;
      int length = activeRules.length;
      for(int i=0; i<length; i++) {
        RuleEnum rule = activeRules[i];
        if (rule != RuleEnum.oneline_comment) {
          rules[j++] = rule;
        }
      }
      return rules;
    }
  }
  
  public static void interpret(Reader reader, Echoer echoer, Scope rootScope) {
    LocationTracker locationTracker = new LocationTracker();
    Interpreter interpreter = new Interpreter(locationTracker, echoer, rootScope);
    ReaderWrapper buffer = new ReaderWrapper(reader, locationTracker);
    
    createAnalyzer(buffer, interpreter).run();
  }
  
  public static void compileAheadOfTime(String scriptName, Reader reader, Scope rootScope) {
    LocationTracker locationTracker = new LocationTracker();
    ReaderWrapper buffer = new ReaderWrapper(reader, locationTracker);
    ASTHandler astHandler = new ASTHandler(locationTracker);
    
    createAnalyzer(buffer, astHandler).run();
    Script script = astHandler.getScript();
    byte[] array = Compiler.compileScriptAheadOfTime(scriptName, script, new LocalScope(rootScope));
    if (array == null) {
      System.err.println("compilation aborted.");
      return;
    }
    
    try {
      FileOutputStream output = new FileOutputStream(scriptName+".class");
      try {
        output.write(array);
        output.flush();
      } finally {
        output.close();
      }
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    
    System.out.println(scriptName+" generated !");
  }
  
  public static <B extends TokenBuffer<CharSequence> & LexerBuffer> Lexer<B> createAnalyzer(B buffer, ASTHandler astHandler) {
    TerminalEvaluator<CharSequence> terminalEvaluator = astHandler;
    GrammarEvaluator grammarEvaluator = astHandler;
    AnalyzerParserBuilder<RuleEnum, B, TerminalEnum, NonTerminalEnum, ProductionEnum, VersionEnum> builder =
      Analyzers.analyzerTokenBufferBuilder(buffer,
          terminalEvaluator,
          grammarEvaluator,
          new SemanticStack()).
        expert().
        advanced();
    CommentSwitcherRuleActivator ruleActivator = new CommentSwitcherRuleActivator(builder.getDefaultActivator(), astHandler);
    return builder.activator(ruleActivator).
      createAnalyzer().
      getLexer();
  }
}
