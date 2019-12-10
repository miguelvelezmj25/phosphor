#!/usr/bin/env bash

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/implicit-optimized/lucene-demo-7.4.0.jar
DEPENDENCIES=$BASE/../../../examples/implicit-optimized/lucene-analyzers-common-7.4.0.jar:$BASE/../../../examples/implicit-optimized/lucene-core-7.4.0.jar
MAIN_CLASS=org.apache.lucene.demo.IndexFiles

function run {
    local program_jar=$1
    local dependencies=$2
    local main_class=$3

    ../../../target/jre-inst-implicit-optimized/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar:$dependencies $main_class -docs ../../../src/
}

time run $PROGRAM_JAR $DEPENDENCIES $MAIN_CLASS