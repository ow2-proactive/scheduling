#Local deployment of Grid Matlab/Scilab ToolBox
#To define enviroment Variables

######### Scilab use ##########

# export SCIDIR=<your current scilab path>
export SCIDIR=/usr/lib/scilab-4.1.2
export SCI=$SCIDIR

#### Scilab libraries
# export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:<path to Scilab libraries>
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCIDIR/bin

######### Matlab use ##########

# export MATLAB_DIR=<your current matlab installation path>
export MATLAB_DIR=/usr/local/matlab2006b

# export MATLAB=<the name of your matlab command> 
export MATLAB=matlab2006b

# export PROACTIVE=<the path to your ProActive installation>
export PROACTIVE=/user/fviale/home/eclipse_workspace/ProActive_Latest


#### Matlab libraries
#### In order to find the right paths, refer to :
# http://www.mathworks.com/access/helpdesk/help/techdoc/index.html?/access/helpdesk/help/techdoc/matlab_external/f39903.html&http://www.mathworks.com/access/helpdesk/help/techdoc/helptoc.html
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$MATLAB_DIR/bin/glnx86:$MATLAB_DIR/sys/os/glnx86

# The script which builds the Matlab<->Java interface copies the library into $PROACTIVE/lib/<Matlab_version>/<matlab relative path to main libraries>
# So in this example (version 7.3) that would be:
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$PROACTIVE/lib/7.3/bin/glnx86


