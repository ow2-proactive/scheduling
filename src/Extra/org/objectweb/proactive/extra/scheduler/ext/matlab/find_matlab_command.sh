#!/bin/bash
matlabs=(matlab matlab2006b matlab2006a matlab7 matlab6.5)
i=0
cmd_line=
while [[ $i -lt 5 && "$cmd_line" == "" ]]; do
    m1=`which ${matlabs[i]} 2>/dev/null|grep -v alias`
    if [ "$m1" != "" ]; then
	cmd_line=`readlink -f $m1`
	bin_dir=`dirname $cmd_line`
	cd $bin_dir&&bin_dir=`find * -name "*.so"|cut -d '/' -f 1|awk '{if (l!=$0) {l=$0;print l}}'`
    fi
    i=$i+1
done
if [ "$cmd_line" != "" ]; then
    echo $cmd_line
else
    echo "Matlab not found"
    echo
fi
if [ "$bin_dir" != "" ]; then
    echo $bin_dir
else
    echo "Matlab bin dir not found"
    echo
    exit 1
fi
echo