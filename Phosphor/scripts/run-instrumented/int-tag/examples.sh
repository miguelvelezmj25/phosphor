#!/usr/bin/env bash

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/int-tag/Phosphor-Examples-1.0-SNAPSHOT.jar
MAIN_CLASS=edu.cmu.cs.mvelezce.inttag.Test

function run {
    local program_jar=$1
    local main_class=$2

    ../../../target/jre-inst-int/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class
#    ../../../target/jre-inst-int/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar $main_class
}

time run $PROGRAM_JAR $MAIN_CLASS
