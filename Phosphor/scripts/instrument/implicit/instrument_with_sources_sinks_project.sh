#!/usr/bin/env bash

DIR=${1}
JAR_NAME=${2}

BASE=$(pwd)/../../..
PHOSPHOR_JAR=$BASE/target/Phosphor-0.0.5-SNAPSHOT.jar
SOURCES=$BASE/resources/test-sources
SINKS=$BASE/resources/test-sinks
INST_DIR=./phosphor

function rm_phosphor_dir {
    printf "\nRemoving phosphor dir\n"
    rm -rf $INST_DIR
}

function mkdir_phosphor {
    printf "\nmkdir phosphor\n"
    mkdir $INST_DIR
}

function mvn_clean {
    printf "\nMVN CLEAN\n"
    mvn clean
}

function mvn_package {
    printf "\nMVN PACKAGE\n"
    mvn package
}

function inst {
    printf "\nInstrumenting program\n"
    local jar=$1

    java -Xmx12g -jar $PHOSPHOR_JAR -taintSources $SOURCES -taintSinks $SINKS -controlTrack -multiTaint -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $INST_DIR
}

(
cd $DIR
#mvn_clean
#mvn_package
rm_phosphor_dir
mkdir_phosphor
inst $JAR_NAME
)