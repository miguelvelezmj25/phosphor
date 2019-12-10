#!/usr/bin/env bash

BASE=$(pwd)
PROGRAM_JAR=$BASE/../../../../performance-mapper-evaluation/original/berkeley-db/target/berkeleydb-1.0-SNAPSHOT.jar
MAIN_CLASS=com.sleepycat.analysis.Run

function run {
    local program_jar=$1
    local main_class=$2

    java -cp $program_jar $main_class
}

time run $PROGRAM_JAR $MAIN_CLASS
