#!/usr/bin/env bash

ARG0=${1}
ARG1=${2}
ARG2=${3}
ARG3=${4}
ARG4=${5}
ARG5=${6}
ARG6=${7}

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/control/h2-1.4.201-SNAPSHOT.jar
MAIN_CLASS=${ARG0}
PROGRAM_NAME=${MAIN_CLASS##*.}

function run() {
  local program_jar=$1
  local main_class=$2
  local arg0=$3
  local arg1=$4
  local arg2=$5
  local arg3=$6
  local arg4=$7
  local arg5=$8

  ../../../../integration-tests/target/jvm-inst-optimized-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5
  #  ../../../../integration-tests/target/jvm-inst-optimized-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4
}

rm -rf $BASE/../../../examples/control/$PROGRAM_NAME
mkdir $BASE/../../../examples/control/$PROGRAM_NAME
rm -rf data
mkdir data
time run $PROGRAM_JAR $MAIN_CLASS $ARG1 $ARG2 $ARG3 $ARG4 $ARG5 $ARG6
rm -rf data
