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

java -server -XX:+UnlockExperimentalVMOptions -XX:+EnableInvokeDynamic -jar $PHPR_HOME/lib/phpreboot.jar $@