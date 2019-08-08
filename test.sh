#!/bin/bash

set -e

SCRIPTPATH="$(cd "$(dirname "$0")"; pwd -P)"

while getopts "hp:" opt; do
  case "${opt}" in
    h )
      echo "Run tests for a project."
      echo "Options: "
      echo "-h : Display this help message."
      echo "-p : name of a project to run tests for."
      exit 0
      ;;
    p )
      PROJECT="$OPTARG"
      ;;
  esac
done

if [[ "$PROJECT" == "" ]]; then
  echo "Must give a project name.";
  exit 1
fi

if [[ ! -d "$SCRIPTPATH/projects/$PROJECT/" ]]; then
  echo "\"$PROJECT\" is not a valid project in this repository."
  exit 1
fi

groovy -cp "$SCRIPTPATH/bin/:$SCRIPTPATH/third_party/*" "$SCRIPTPATH"/projects/"$PROJECT/${PROJECT}_test.groovy"
