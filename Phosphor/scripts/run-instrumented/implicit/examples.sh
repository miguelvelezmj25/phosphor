#!/usr/bin/env bash

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/implicit/Phosphor-Examples-1.0-SNAPSHOT.jar
MAIN_CLASS=edu.cmu.cs.mvelezce.implicit.SimpleConditionals
#MAIN_CLASS=edu.cmu.cs.cc.implicit.Simple

function run {
    local program_jar=$1
    local main_class=$2

    ../../../target/jre-inst-implicit/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class
#    ../../../target/jre-inst-implicit/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -cp $program_jar $main_class
#    ../../../target/jre-inst-implicit/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class -taintSources ../../taint-sources -taintSinks ../../taint-sinks
#    ../../../target/jre-inst-implicit/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar $main_class
}

time run $PROGRAM_JAR $MAIN_CLASS
