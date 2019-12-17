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
    mvn package
}

function inst {
    printf "\nInstrumenting program\n"
    local phosphor_jar=$1
    local jar=$2
    local dir=$3

    java -Xmx12g -jar $phosphor_jar -serialization -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir

#    java -Xmx12g -jar $phosphor_jar -disableJumpOptimizations -serialization -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
#    java -Xmx12g -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar $phosphor_jar -withCCSinks -serialization -forceUnboxAcmpEq -withEnumsByValue ./target/$jar $dir
}

(
cd $DIR
#mvn_clean
#mvn_package
rm_phosphor_dir $INST_DIR
mkdir_phosphor $INST_DIR
inst $PHOSPHOR_JAR $JAR_NAME $INST_DIR
)