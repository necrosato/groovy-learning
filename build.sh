#!/bin/bash

SCRIPTPATH="$(cd "$(dirname "$0")"; pwd -P)"

groovyc -d "$SCRIPTPATH/bin/" "$SCRIPTPATH"/projects/*/*.groovy
