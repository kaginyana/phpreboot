#!/bin/bash
#
# phpr.sh - shell script for starting PHP Reboot
#
# this file is shamelessly a copy/paste of the jruby.sh from JRuby project
#

## resolve links - $0 may be a link to home
PRG=$0
progname=`basename "$0"`

while [ -h "$PRG" ] ; do
ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    if expr "$link" : '/' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname ${PRG}`/${link}"
    fi
  else
    PRG="`dirname $PRG`/$link"
  fi
done

PHPR_HOME_1=`dirname "$PRG"` # the ./bin dir
if [ "$PHPR_HOME_1" = '.' ] ; then
  cwd=`pwd`
  PHPR_HOME=`dirname $cwd`
else
  PHPR_HOME=`dirname "$PHPR_HOME_1"` # the . dir
fi

LIB=$PHPR_HOME/lib
#java -server -ea -XX:+AnonymousClasses -XX:+AggressiveOpts -XX:MaxBCEAEstimateLevel=20 -XX:MaxBCEAEstimateSize=1500 -XX:MaxInlineLevel=20 -XX:MaxRecursiveInlineLevel=3 -XX:MaxInlineSize=150 -XX:MaxTrivialSize=25 -XX:PerMethodTrapLimit=1000 -XX:TypeProfileWidth=3 -XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions -XX:ScavengeRootsInCode=2 -XX:+EnableInvokeDynamic -XX:MethodHandlePushLimit=5 -Xbootclasspath/p:$LIB/phpreboot.jar:$LIB/tatoo-runtime.jar:$LIB/asm-all-3.2.jar:$LIB/hs19-b01-jsr292-patch.jar:$LIB/grizzly-servlet-webserver-1.9.18-k.jar:$LIB/derby.jar com.googlecode.phpreboot.Main $@
java -server -ea -XX:+AnonymousClasses  -XX:+UnlockExperimentalVMOptions -XX:+EnableInvokeDynamic -Xbootclasspath/p:$LIB/phpreboot.jar:$LIB/tatoo-runtime.jar:$LIB/asm-all-3.2.jar:$LIB/hs19-b01-jsr292-patch.jar:$LIB/grizzly-servlet-webserver-1.9.18-k.jar:$LIB/derby.jar com.googlecode.phpreboot.Main $@