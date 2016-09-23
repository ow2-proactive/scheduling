#!/bin/bash
# Automatic install Radical server
# Updated to use python 3.4

## Install required packages

cd ..
cwd=$(pwd)
## Install radicale
cd $cwd/tools/radicale/linux
tar xvzf Radicale-1.1.1.tar.gz

sudo apt-get update
sudo apt-get -y install python3
sudo apt-get -y install python3-setuptools
sudo apt-get -y install apache2-utils
cd Radicale-1.1.1
sudo python3 setup.py install
cd $cwd
sudo rm -rf $cwd/tools/radicale/linux/Radicale-1.1.1

mkdir -p ~/.config/radicale/collections

#check if path is not empty string
loginFile="$cwd/config/authentication/login.cfg"
#check if file exist
if [ ! -e "$loginFile" ] 
    then
    echo "authentication file doesnt exists"
    exit
fi

cp -f $cwd/tools/radicale/linux/config ~/.config/radicale/config
cp -f $cwd/tools/radicale/linux/logging ~/.config/radicale/logging
rm -rf ~/.config/radicale/users
touch ~/.config/radicale/users
mkdir ~/.config/radicale/log

echo "Radicale server is installed, starting the server..."
cd ~/.config/radicale/log

radicale -d -S

echo "Radicale server started"

#delete radicale folder
cd $cwd/tools/

read -e -p "Would you like to start Calendar Service right now? (Y/N) :" input

answer=$input
if ((answer == "y")) || ((answer == "Y"))
	then
	source $cwd/tools/calendar-service.sh start
elif ((answer == "n")) || ((answer == "N"))
	then
	exit 1
fi
