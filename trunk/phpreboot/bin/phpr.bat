:: shell script to start phpr on windows NT+
java -server -XX:+AnonymousClasses -XX:+UnlockExperimentalVMOptions -XX:+EnableInvokeDynamic -jar %~dp0\..\lib\phpreboot.jar %1 %2 %3 %4 %5 %6 %7 %8 %9