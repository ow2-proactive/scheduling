#!/bin/sh
tar -zxvf j2re1.4.2_08.tar.gz >/dev/null
export JAVA_HOME=$PWD/j2re1.4.2_08
export PATH=$JAVA_HOME/bin:$PATH
