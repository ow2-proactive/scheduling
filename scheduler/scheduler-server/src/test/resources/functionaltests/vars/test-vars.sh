#!/bin/bash

if [[ "$variables_var" == "pre-script-1" ]] ; then
    exit 0
else
    exit 1
fi
