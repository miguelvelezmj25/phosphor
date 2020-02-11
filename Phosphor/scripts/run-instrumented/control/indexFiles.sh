#!/usr/bin/env bash

ARG0=${1}
ARG1=${2}
ARG2=${3}
ARG3=${4}
ARG4=${5}
ARG5=${6}
ARG6=${7}
ARG7=${8}
ARG8=${9}
ARG9=${10}
ARG10=${11}
ARG11=${12}
ARG12=${13}
ARG13=${14}
ARG14=${15}
ARG15=${16}
ARG16=${17}
ARG17=${18}

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/control/lucene-7.4.0.jar
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
  local arg6=$9
  local arg7=${10}
  local arg8=${11}
  local arg9=${12}
  local arg10=${13}
  local arg11=${14}
  local arg12=${15}
  local arg13=${16}
  local arg14=${17}
  local arg15=${18}
  local arg16=${19}

  ../../../../integration-tests/target/jvm-inst-optimized-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16
}

rm -rf $BASE/../../../examples/control/$PROGRAM_NAME
mkdir $BASE/../../../examples/control/$PROGRAM_NAME
time run $PROGRAM_JAR $MAIN_CLASS $ARG1 $ARG2 $ARG3 $ARG4 $ARG5 $ARG6 $ARG7 $ARG8 $ARG9 $ARG10 $ARG11 $ARG12 $ARG13 $ARG14 $ARG15 $ARG16 $ARG17
