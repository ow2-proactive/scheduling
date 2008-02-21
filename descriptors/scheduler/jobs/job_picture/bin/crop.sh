#!/bin/sh
#cut a picture into $3 pictures named  $1_00, $1_01 ...$1_0$2 in the output dir $2

if [ $# -ne 3 ]
then
	echo "this script take 3 arguments : file name, output dir, and number of parts to create" 
	exit 1
fi

FILE_NAME=$1
OUTPUT_DIR=$2
NB_PARTS=$3

if [ ! -f $FILE_NAME ]
then
	echo "$FILE_NAME not found"
	exit 1
fi

HEIGHT=`identify -format "%h" $FILE_NAME`
WIDTH=`identify -format "%w" $FILE_NAME`
MODULO=`expr $WIDTH % $NB_PARTS`
SIZE_CROP=`expr $WIDTH / $NB_PARTS + $MODULO`

#NOM_FICH_OUT=`echo "$FILE_NAME" | cut -d. -f1`
NOM_FICH_OUT=`basename $FILE_NAME | cut -d. -f1`

echo "test $NOM_FICH_OUT"


EXT=`echo "$FILE_NAME" | cut -d. -f2`
convert $FILE_NAME -crop ${SIZE_CROP}x${HEIGHT} ${OUTPUT_DIR}/${NOM_FICH_OUT}_%02d.${EXT}
if [ $? -ne 0 ]
then
	echo "cropping problem"
	exit 1
else
	exit 0
fi
