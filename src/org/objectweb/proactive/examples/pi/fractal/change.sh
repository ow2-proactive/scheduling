#!/bin/bash
sed -i "s/$1/$2/g" *
rename $1 $2 *
cd ../..
tar czf ~/work/save/pi.$(date +%F).$(date +%T).tgz *
cd - > /dev/null
