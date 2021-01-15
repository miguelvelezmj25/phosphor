#!/usr/bin/env bash

ARG0=${1}
ARG1=${2}

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../examples/control/tracing-0.1.0-SNAPSHOT.jar
MAIN_CLASS=${ARG0}
PROGRAM_NAME=${MAIN_CLASS##*.}

function run() {
  local program_jar=$1
  local main_class=$2
  local arg0=$3

  ../../../../integration-tests/target/jvm-inst-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar:/Users/mvelezce/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar $main_class $arg0
#    ../../../../integration-tests/target/jvm-inst-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar:/Users/mvelezce/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16 $arg17 $arg18 $arg19 $arg20 $arg21

#  ../../../../integration-tests/target/jvm-inst-optimized-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -cp $program_jar:/Users/mvelezce/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16 $arg17 $arg18 $arg19 $arg20 $arg21
#    ../../../../integration-tests/target/jvm-inst-optimized-control/bin/java -Xmx28g -Xms28g -Xbootclasspath/a:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -javaagent:../../../target/Phosphor-0.0.5-SNAPSHOT.jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $program_jar:/Users/mvelezce/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar $main_class $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10 $arg11 $arg12 $arg13 $arg14 $arg15 $arg16 $arg17 $arg18 $arg19 $arg20 $arg21
}

rm -rf $BASE/../../../examples/control/$PROGRAM_NAME
mkdir $BASE/../../../examples/control/$PROGRAM_NAME
rm -rf output
mkdir output
time run $PROGRAM_JAR $MAIN_CLASS $ARG1
rm -rf output
