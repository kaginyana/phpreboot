package com.googlecode.phpreboot.regex;

import java.util.regex.Pattern;

import com.googlecode.phpreboot.runtime.RT;

enum PatternMode {
  CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE),
  DOT_ALL(Pattern.DOTALL),
  ANCHORS_MATCH(Pattern.MULTILINE),
  FREE_SPACING(Pattern.COMMENTS)
  ;
  
  private final int flag;
  
  private PatternMode(int flag) {
    this.flag = flag;
  }
  
  public static int getPatternFlagForMode(String modeText) {
    int flag = 0;
    for(int i=0; i<modeText.length(); i++) {
      char c = modeText.charAt(i);
      PatternMode mode = mode(c);
      if (mode == null) {
        throw RT.error("unknown pattern mode %c", c);
      }
      flag = flag | mode.flag;
    }
    return flag;
  }
  
  private static PatternMode mode(char tag) {
    switch(tag) {
    case 'i':
      return CASE_INSENSITIVE;
    case 's':
      return DOT_ALL;
    case 'm':
      return ANCHORS_MATCH;
    case 'x':
      return FREE_SPACING;
    }
    return null;
  }
}