#!/bin/sh
echo
echo --- Compile ---------------------------------------------

if [ $# -ne 1 ]; then
    echo "
    Compile one example
       - parameter : the name of the example to compile (name of directory)
    
    ex : compile readers
    
    List of examples :
      + algebra
      + binarytree
      + boundedbuffer
      + collaborative
      + cruisecontrol
      + cs (client - server)
      + doctor
      + garden
      + hello
      + penguin
      + philosophers
      + readers
    "
    exit 1
fi


JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo "
    The enviroment variable JAVA_HOME must be set the current jdk distribution
    installed on your computer.
    Use 
     export JAVA_HOME=<the directory where is the JDK>
"
exit 1
fi

PATH=$JAVA_HOME/bin:$PATH

cd ../../src 

PROACTIVE=../.

# ----
# Set up the classpath using classes dir or jar files
# 
CLASSPATH=.
if [ -d $PROACTIVE/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes
else
   mkdir $PROACTIVE/classes
fi

if [ -f $PROACTIVE/ProActive.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive.jar
fi

if [ -f $PROACTIVE/ProActive_examples.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive_examples.jar
fi

export CLASSPATH
echo CLASSPATH=$CLASSPATH
echo "compiling java files in org/objectweb/proactive/examples/$1/"
javac -d ../classes org/objectweb/proactive/examples/$1/*.java

echo
echo -----------------------------------------------------------------
