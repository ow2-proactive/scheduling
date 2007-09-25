#Local deployment of Grid Scilab ToolBox
#To define enviroment Variables

#export SCIDIR=<your current scilab path>
#export SCI=<your current scilab path>
#export MATLAB_DIR=<your current matlab installation path>
#export MATLAB=<the name of your matlab command> 
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCIDIR/bin:$MATLAB_DIR/bin/glnx86:$MATLAB_DIR/sys/os/glnx86:.

export SCIDIR=/user/fviale/home/bin/scilab-4.1.1/
export MATLAB_DIR=/usr/local/matlab2006b
export MATLAB=matlab2006b
export SCI=$SCIDIR
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCIDIR/bin:$MATLAB_DIR/bin/glnx86:$MATLAB_DIR/sys/os/glnx86:.

