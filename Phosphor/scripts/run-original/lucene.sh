#!/usr/bin/env bash

BASE=$(pwd)
BASE=$BASE/../../../../performance-mapper-evaluation/original/lucene
PROGRAM_JAR=$BASE/demo/target/lucene-demo-7.4.0.jar
DEPENDENCIES=$BASE/jars/analysis/common/lucene-analyzers-common-7.4.0.jar:$BASE/jars/core/lucene-core-7.4.0.jar
MAIN_CLASS=org.apache.lucene.demo.IndexFiles

function run {
    local program_jar=$1
    local dependencies=$2
    local main_class=$3

    java -cp $program_jar:$dependencies  $main_class -docs ../../src/
}

time run $PROGRAM_JAR $DEPENDENCIES $MAIN_CLASS
