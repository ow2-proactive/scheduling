#!/bin/sh
#rebuild a picture made of n files called $1*.jpg, the output is $2

FILES_NAMES=$1
OUTPUT=$2

if [ $# -ne 2 ]
then
	echo "this script need 2 arguments : the prefix of files to merge, and file output name"
	echo "example : join.sh file_.jpg out.jpg"
	echo "will join file_0.jpg, file_1.jpg ... in a out jpg.file"
	exit 1
fi

PREFIX=`echo "$FILES_NAMES" | cut -d. -f1`
EXT=`echo "$FILES_NAMES" | cut -d. -f2`
FILES_NUMBER=`ls ${PREFIX}*${EXT} | wc -l`

montage -mode concatenate -tile ${FILES_NUMBER}x1 ${PREFIX}*${EXT} $OUTPUT
echo "Merged picture parts in $OUTPUT"
