#!/usr/bin/env bash

ARG0=${1}
ARG1=${2}
ARG2=${3}
ARG3=${4}
ARG4=${5}
ARG5=${6}

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/implicit-optimized/prevayler-1.0-SNAPSHOT.jar
MAIN_CLASS=${ARG1}
PROGRAM_NAME=${ARG0}

function run {
    local program_jar=$1
    local main_class=$2
    local arg0=$3
    local arg1=$4
    local arg2=$5
    local arg3=$6

    ../../../target/jre-inst-implicit-optimized/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3
#    ../../../target/jre-inst-implicit-optimized/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3
#    ../../../target/jre-inst-implicit-optimized/bin/java -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar=taintSources=../../../examples/implicit-optimized/prevayler-sources -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3

}

rm -rf $BASE/../../../examples/implicit-optimized/$PROGRAM_NAME
mkdir $BASE/../../../examples/implicit-optimized/$PROGRAM_NAME
time run $PROGRAM_JAR $MAIN_CLASS $ARG2 $ARG3 $ARG4 $ARG5