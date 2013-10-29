#!/bin/bash

if [[ "$var_var" == "pre-script-1" ]] ; then
    exit 0
else
    exit 1
fi
