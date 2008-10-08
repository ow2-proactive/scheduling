#!/bin/bash
# This scripts is used to find Matlab on a remote host.
# it prints out three information, each info is printed on one line
# 1) path to the root directory of matlab
# 2) path to matlab libraries (relative from matlab root)
# 3) matlab version (major.minor i.e. 7.1 7.2 7.3 etc...)
 
matlabs=(matlab matlab2007b matlab2007a matlab2006b matlab2006a matlab71 matlab7)
i=0
cmd_line=
while [[ $i -lt 7 && "$cmd_line" == "" ]]; do
    m1=`which ${matlabs[i]} 2>/dev/null|grep -v alias`
    if [ "$m1" != "" ]; then
		# finding matlab home
		cmd_line=`readlink -f $m1`
		matlab_home=$(readlink -f $(dirname $cmd_line)/..)
		# finding matlab version
		matlab_version=$($cmd_line -nodisplay -nosplash -nojvm -r 'disp(version()); quit;'|tail -n 2|cut -s -d '.' -f 1-2)
		
		#finding matlab architecture directory
		case "`uname -s`" in
			Linux)
				case "`uname -i`" in
					i386*)
						matlab_lib_dir=bin/glnx86
						;;
					x86_64*)
						matlab_lib_dir=bin/glnxa64
						;;
					*)
						matlab_lib_dir=bin/glnx86
						;;
				esac
				;;	
			CYGWIN*)
				matlab_lib_dir=bin/win32
				;;
			SunOS*)
				matlab_lib_dir=extern/lib/sol2
				;;
			*)
				matlab_lib_dir=extern/lib
				;;
		esac
		
		
		#cd $bin_dir&&bin_dir=`find * -name "*.so"|cut -d '/' -f 1|awk '{if (l!=$0) {l=$0;print l}}'`
    fi
    i=$i+1
done
if [ "$matlab_home" != "" ]; then
    echo $matlab_home
    echo ----------------
else
    host=`hostname`
    echo "Matlab not found on $host"
    echo ----------------
    echo
    exit 1
fi
if [ "$matlab_lib_dir" != "" ]; then
    echo $matlab_lib_dir
else
    echo "Matlab lib dir not found"
    echo
    exit 1
fi
if [ "$matlab_version" != "" ]; then
    echo $matlab_version
    echo ----------------
else
    echo "Matlab version unknown"
    echo ----------------
    echo
    exit 1
fi
echo