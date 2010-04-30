package com.googlecode.phpreboot.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.phpreboot.ast.ExprRegexMatch;
import com.googlecode.phpreboot.ast.ExprRegexReplace;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.runtime.Array;

public class RegexEvaluator {
  private static int getPatternFlagForMode(IdToken idToken) {
    if (idToken == null)
      return 0;
    return PatternMode.getPatternFlagForMode(idToken.getValue());
  }
  
  public static Object visit(ExprRegexMatch expr_regex_match, EvalEnv env) {
    Object value = Evaluator.INSTANCE.eval(expr_regex_match.getExpr(), env);
    
    String regex = expr_regex_match.getRegexAnycharacter().getValue();
    int mode = getPatternFlagForMode(expr_regex_match.getIdOptional());
    Pattern pattern = Pattern.compile(regex, mode);
    Matcher matcher = pattern.matcher(value.toString()); // nullcheck
    
    int groupCount = matcher.groupCount();
    boolean matches = matcher.matches();
    if (groupCount == 0)
      return matches;
    
    Array array = new Array();
    if (matches) {
      for(int i=1; i<=groupCount; i++) {
        array.set(i, matcher.group(i));
      }
    }
    return array;
  }
  
  public static Object visit(ExprRegexReplace expr_regex_replace, EvalEnv env) {
    Object value = Evaluator.INSTANCE.eval(expr_regex_replace.getExpr(), env);
    
    String regex = expr_regex_replace.getRegexAnycharacter().getValue();
    String replace = expr_regex_replace.getRegexAnycharacter2().getValue();
    int mode = getPatternFlagForMode(expr_regex_replace.getIdOptional());
    Pattern pattern = Pattern.compile(regex, mode);
    Matcher matcher = pattern.matcher(value.toString()); // nullcheck
    return matcher.replaceAll(replace);
  }
}