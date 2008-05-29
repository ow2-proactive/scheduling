#!/bin/sh

#PROACTIVE=~/ProActive-ssh/ProActive
#TUTORIAL=~/softs/eclipse/workspace/tutorial
TUTORIAL=~/tutorial
PROACTIVE=~/NOSAVE/tmp/ProActive
#PROACTIVE=$TUTORIAL/ProActive
SOPHIA_CLUSTER_KEY=$TUTORIAL/ssh/deployer/sophia
AMSTERDAM_CLUSTER_KEY=$TUTORIAL/ssh/deployer/amsterdam

#alias scp='scp -v'
alias cp='cp -v'


#export INRIA_CLUSTER_KEY
#export AMSTERDAM_CLUSTER_KEY

#sh $PROACTIVE/compile/build clean
#sh $PROACTIVE/compile/build proActiveJar

cp $PROACTIVE/dist/ProActive/ProActive.jar $TUTORIAL/libs/

#copy to INRIA's cluster
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr rm -R tutorial
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/libs
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/ssh
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/ssh/sophia
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/scripts
ssh -i $SOPHIA_CLUSTER_KEY cluster.inria.fr mkdir tutorial/config

#copy ssh key
scp -i $SOPHIA_CLUSTER_KEY $TUTORIAL/ssh/sophia/production mmorel@cluster.inria.fr:~/tutorial/ssh/sophia/
scp -i $SOPHIA_CLUSTER_KEY $TUTORIAL/ssh/sophia/production.pub mmorel@cluster.inria.fr:~/tutorial/ssh/sophia/



scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/ProActive/ProActive.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/asm.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/log4j.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/xercesImpl.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/bouncycastle.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/components/fractal.jar mmorel@cluster.inria.fr:~/tutorial/libs/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/dist/lib/trilead-ssh2.jar mmorel@cluster.inria.fr:~/tutorial/libs/

scp -i $SOPHIA_CLUSTER_KEY $TUTORIAL/config/proactive-log4j mmorel@cluster.inria.fr:~/tutorial/config/
scp -i $SOPHIA_CLUSTER_KEY $TUTORIAL/config/proactive.java.policy mmorel@cluster.inria.fr:~/tutorial/config/
scp -i $SOPHIA_CLUSTER_KEY $PROACTIVE/scripts/unix/cluster/startRuntime.sh mmorel@cluster.inria.fr:~/tutorial/scripts/
scp -i $SOPHIA_CLUSTER_KEY $TUTORIAL/config/proactive-config-sophia-cluster.xml mmorel@cluster.inria.fr:~/tutorial/config/

#copy to Amsterdam's cluster
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl rm -R tutorial
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial/libs
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial/ssh
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial/config
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial/scripts
ssh -l rquilici -i $AMSTERDAM_CLUSTER_KEY fs0.das2.cs.vu.nl mkdir tutorial/ssh/amsterdam

scp -i $AMSTERDAM_CLUSTER_KEY $TUTORIAL/ssh/amsterdam/production rquilici@fs0.das2.cs.vu.nl:~/tutorial/ssh/amsterdam/
scp -i $AMSTERDAM_CLUSTER_KEY $TUTORIAL/ssh/amsterdam/production.pub rquilici@fs0.das2.cs.vu.nl:~/tutorial/ssh/amsterdam/

scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/ProActive/ProActive.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/asm.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/log4j.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/xercesImpl.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/bouncycastle.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/components/fractal.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/dist/lib/trilead-ssh2.jar rquilici@fs0.das2.cs.vu.nl:~/tutorial/libs/

scp -i $AMSTERDAM_CLUSTER_KEY $TUTORIAL/config/proactive-log4j rquilici@fs0.das2.cs.vu.nl:~/tutorial/config/
scp -i $AMSTERDAM_CLUSTER_KEY $TUTORIAL/config/proactive.java.policy rquilici@fs0.das2.cs.vu.nl:~/tutorial/config/
scp -i $AMSTERDAM_CLUSTER_KEY $PROACTIVE/scripts/unix/cluster/startRuntime.sh rquilici@fs0.das2.cs.vu.nl:~/tutorial/scripts/
scp -i $AMSTERDAM_CLUSTER_KEY $TUTORIAL/config/proactive-config-amsterdam-cluster.xml rquilici@fs0.das2.cs.vu.nl:~/tutorial/config/



