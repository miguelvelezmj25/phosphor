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
ARG18=${19}
ARG19=${20}
ARG20=${21}
ARG21=${22}
ARG22=${23}

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/control/original-dconvert-1.0.0-alpha6.jar
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
  local arg17=${20}
  local arg18=${21}
  local arg19=${22}
  local arg20=${23}
  local arg21=${24}

  ../../../../integration-tests/target/jvm-inst-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar:/Users/mvelezce/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16 $arg17 $arg18 $arg19 $arg20 $arg21
  #  ../../../../integration-tests/target/jvm-inst-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16 $arg17 $arg18 $arg19 $arg20 $arg21
}

rm -rf $BASE/../../../examples/control/$PROGRAM_NAME
mkdir $BASE/../../../examples/control/$PROGRAM_NAME
rm -rf output
mkdir output
time run $PROGRAM_JAR $MAIN_CLASS $ARG1 $ARG2 $ARG3 $ARG4 $ARG5 $ARG6 $ARG7 $ARG8 $ARG9 $ARG10 $ARG11 $ARG12 $ARG13 $ARG14 $ARG15 $ARG16 $ARG17 $ARG18 $ARG19 $ARG20 $ARG21 $ARG22
rm -rf output
