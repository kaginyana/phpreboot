package com.googlecode.phpreboot.interpreter;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.IdentityHashMap;

import com.googlecode.phpreboot.lexer.RuleEnum;
import com.googlecode.phpreboot.parser.NonTerminalEnum;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.parser.TerminalEnum;
import com.googlecode.phpreboot.parser.VersionEnum;
import com.googlecode.phpreboot.tools.Analyzers;
import com.googlecode.phpreboot.tools.GrammarEvaluator;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

import fr.umlv.tatoo.runtime.buffer.impl.LocationTracker;
import fr.umlv.tatoo.runtime.buffer.impl.ReaderWrapper;
import fr.umlv.tatoo.runtime.lexer.RuleActivator;
import fr.umlv.tatoo.runtime.tools.Debug;
import fr.umlv.tatoo.runtime.tools.SemanticStack;
import fr.umlv.tatoo.runtime.tools.builder.Builder.AnalyzerParserBuilder;

public class Analyzer {
  static class InterpreterRuleActivator implements RuleActivator<RuleEnum> {
    private final RuleActivator<RuleEnum> ruleActivator;
    private final Interpreter interpreter;
    private final IdentityHashMap<RuleEnum[], RuleEnum[]> cache =
      new IdentityHashMap<RuleEnum[], RuleEnum[]>();
    
    InterpreterRuleActivator(RuleActivator<RuleEnum> ruleActivator, Interpreter interpreter) {
      this.ruleActivator = ruleActivator;
      this.interpreter = interpreter;
    }

    @Override
    public RuleEnum[] activeRules() {
      RuleEnum[] activeRules = ruleActivator.activeRules();
      if (interpreter.enableLineComment) {
        return activeRules;
      }
      RuleEnum[] rules = cache.get(activeRules);
      if (rules == null) {
        rules = suppressLineCommentRule(activeRules);
        cache.put(activeRules, rules);
      }
      return rules;
    }
    
    private RuleEnum[] suppressLineCommentRule(RuleEnum[] activeRules) {
      RuleEnum[] rules = new RuleEnum[activeRules.length - 1];
      int j = 0;
      for(int i=0; i<activeRules.length; i++) {
        RuleEnum rule = activeRules[i];
        if (rule != RuleEnum.oneline_comment) {
          rules[j++] = rule;
        }
      }
      return rules;
    }
  }
  
  public static void analyze(Reader reader, PrintWriter writer, Scope rootScope) {
    Interpreter interpreter = new Interpreter(writer, rootScope);
    ReaderWrapper buffer = new ReaderWrapper(reader, new LocationTracker());
    
    //TerminalEvaluator<CharSequence> terminalEvaluator = Debug.createTraceProxy(TerminalEvaluator.class, interpreter);
    //GrammarEvaluator grammarEvaluator = Debug.createTraceProxy(GrammarEvaluator.class, interpreter);
    TerminalEvaluator<CharSequence> terminalEvaluator = interpreter;
    GrammarEvaluator grammarEvaluator = interpreter;
    AnalyzerParserBuilder<RuleEnum, ReaderWrapper, TerminalEnum, NonTerminalEnum, ProductionEnum, VersionEnum> builder =
      Analyzers.analyzerTokenBufferBuilder(buffer,
          terminalEvaluator,
          grammarEvaluator,
          new SemanticStack()).
        expert().
        advanced();
    InterpreterRuleActivator ruleActivator = new InterpreterRuleActivator(builder.getDefaultActivator(), interpreter);
    builder.activator(ruleActivator).
      createAnalyzer().
      getLexer().
      run();
  }
}
