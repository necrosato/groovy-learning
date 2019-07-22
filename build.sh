#!/bin/bash

SCRIPTPATH="$(cd "$(dirname "$0")"; pwd -P)"

mkdir -p "$SCRIPTPATH/bin/"
groovyc -d "$SCRIPTPATH/bin/" "$SCRIPTPATH"/projects/*/*.groovy
