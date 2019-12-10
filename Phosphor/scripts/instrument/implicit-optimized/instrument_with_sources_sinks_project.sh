#!/usr/bin/env bash

DIR=${1}
JAR_NAME=${2}

BASE=$(pwd)
BASE=$BASE/../../..
PHOSPHOR_JAR=$BASE/target/Phosphor-0.0.5-SNAPSHOT.jar
INST_DIR=./phosphor

function rm_phosphor_dir {
    printf "\nRemoving phosphor dir\n"
    local dir=$1

    rm -rf $dir
}

function mkdir_phosphor {
    printf "\nmkdir phosphor\n"
    local dir=$1

    rm -rf $dir
}

function mvn_clean {
    printf "\nMVN CLEAN\n"
    mvn clean
}

function mvn_package {
    printf "\nMVN PACKAGE\n"
    mvn -Dmaven.test.skip=true package
}

function inst {
    printf "\nInstrumenting program\n"
    local phosphor_jar=$1
    local jar=$2
    local dir=$3

    java -Xmx10g -jar $phosphor_jar -withCCSinks -withoutBranchNotTaken -controlTrack -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -withCCSinks -withoutBranchNotTaken -controlTrack -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -withCCSinks -withoutBranchNotTaken -controlTrack -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir

#    java -Xmx10g -jar $phosphor_jar -taintSources $sources -taintSinks $sinks -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -taintSources $sources -taintSinks $sinks -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir


#    java -Xmx10g -jar $phosphor_jar -taintSources $sources -taintSinks $sinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir

#    java -Xmx10g -jar $phosphor_jar -taintSources $sources -taintSinks $sinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir

#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -taintSources $sources -taintSinks $sinks -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir


#    java -Xmx10g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -controlStackSingleton -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR




#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR

#    java -Xmx10g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -jar $PHOSPHOR_JAR -controlStackSingleton -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -jar $PHOSPHOR_JAR -withCCSinks -controlStackSingleton -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
#    java -Xmx10g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $PHOSPHOR_JAR -withCCSinks -withoutBranchNotTaken -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR

}

(
cd $DIR
#mvn_clean
#mvn_package
rm_phosphor_dir $INST_DIR
mkdir_phosphor $INST_DIR
inst $PHOSPHOR_JAR $JAR_NAME $INST_DIR
)
