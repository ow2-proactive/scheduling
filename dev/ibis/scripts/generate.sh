#!/bin/bash
ROOT=/home1/fabrice
. ./env.sh

chooseJdk() {
 if [ $1 == "sun" ]
 then
  export JAVA_HOME=/usr/local/sun-java/jdk1.4
#  export JAVART=/usr/local/sun-java/jdk1.4/jre/lib//rt.jar
  JAVART=`ls /usr/local/sun-java/jdk1.4/jre/lib/*.jar| tr '\n' ':'`
  export baseDir=/var/tmp/fabrice/Ibis/Sun
  echo "---- sun java choosen"
 else 
  export JAVA_HOME=/usr/local/ibm-java/IBMJava2-141/
  JAVART=`ls /usr/local/ibm-java/IBMJava2-141/jre/lib/*.jar| tr '\n' ':'`
  export baseDir=/var/tmp/fabrice/Ibis/Ibm
  echo "---- ibm java choosen"
 fi

}

jdk() {
echo "###### JDK ######"
 echo $baseDir
cd $baseDir/jdk && \
rm -r * 
~/workIbis/Ibis/bin/extract_jars  ${JAVART} \
&&   /home1/fabrice/workIbis/Ibis/bin/tree_ioc -dir *
echo "###### Copying JDK ####"  
rm -r  ~/IOFAb/jdk
cp -r  $baseDir/jdk ~/IOFAb/jdk

}


lib() {
echo "###### LIB ######"
cd $baseDir/lib && \
rm -r * 

JAR_LIST="/home1/fabrice/workProActive/ProActive/lib/asm.jar /home1/fabrice/workProActive/ProActive/lib/bcel.jar  /home1/fabrice/workProActive/ProActive/lib/log4j.jar /home1/fabrice/workProActive/ProActive/lib/reggie.jar /home1/fabrice/workProActive/ProActive/lib/xercesImpl.jar /home1/fabrice/workProActive/ProActive/lib/fractal.jar"

 for file in  $JAR_LIST # /home1/fabrice/workProActive/ProActive/lib/*jar # /home1/fabrice/workProActive/ProActive/dev/lib/*jar
  do
   echo $file;
   ~/workIbis/Ibis/bin/extract_jars $file
 done
echo "###### Copying lib ####"  
rm -r  ~/IOFAb/lib
cp -r  $baseDir/lib ~/IOFAb/lib
}

proactive() {
### generate proactive files
echo "###### PROACTIVE ######"

DIR_LIST="/home1/fabrice/workProActive/ProActive/classes/org \
/home1/fabrice/workProActive/ProActive/classes/modelisation  \
/home1/fabrice/workProActive/ProActive/classes/util "


export CLASSPATH=$CLASSPATH:/home1/fabrice/workProActive/ProActive/lib/fractal.jar
cd $baseDir/ProActive && \
rm -r * 
cp -r  $DIR_LIST . && \
rm -r org/objectweb/proactive/ic2d && \
/home1/fabrice/workIbis/Ibis/bin/tree_ioc -dir * && \
echo "###### Copying proactive ####" 
rm -r  ~/IOFAb/ProActive
cp -r  $baseDir/ProActive ~/IOFAb


}


ibis() {
echo "###### IBIS ######"
cd $baseDir/Ibis && \
rm -r * 
cp -r /home1/fabrice/workIbis/Ibis/classes/* . \
&&  /home1/fabrice/workIbis/Ibis/bin/tree_ioc -dir * && \
echo "###### Copying ibis ####"  
rm -r  ~/IOFAb/Ibis
cp -r  $baseDir/Ibis ~/IOFAb

}


jem3D() {
echo "###### Jem3D ######"
cd $baseDir/jem3D && \
rm -r * 
cp -r /home1/fabrice/workProActive/jem3D/bin/* . \
&&  /home1/fabrice/workIbis/Ibis/bin/tree_ioc -dir *
echo "###### Copying Jem3D ####"  
rm -r  ~/IOFAb/jem3D
cp -r  $baseDir/jem3D ~/IOFAb
}

if [ $# -le 0 ]
then 
 echo "Usage: generate.sh [sun|ibm] [jdk] [lib] [proactive] [ibis] [jem3D]"
 exit 0
fi 

chooseJdk $1
shift
#exit

for i in $@
do
$i
done
