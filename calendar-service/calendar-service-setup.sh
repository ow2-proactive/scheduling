#!/bin/bash
# Automatic install Radical server
# Updated to use python 3.4

## Install required packages

cd ..
cwd="$(pwd)"
radicale_version="1.1.1"

## Install radicale
cd "$cwd/calendar-service/radicale/linux"
tar xvzf "Radicale-$radicale_version.tar.gz"

sudo apt-get update
sudo apt-get -y install python3
sudo apt-get -y install python3-setuptools
sudo apt-get -y install apache2-utils
cd "Radicale-$radicale_version"
sudo python3 setup.py install
cd "$cwd"
sudo rm -rf "$cwd/calendar-service/radicale/linux/Radicale-$radicale_version"

mkdir -p ~/.config/radicale/collections

#check if path is not empty string
loginFile="$cwd/config/authentication/login.cfg"
#check if file exist
if [ ! -e "$loginFile" ] 
    then
    echo "authentication file doesnt exists"
    exit
fi

cp -f "$cwd/calendar-service/radicale/linux/config" ~/.config/radicale/config
cp -f "$cwd/calendar-service/radicale/linux/logging" ~/.config/radicale/logging
cp -f "$cwd/calendar-service/radicale/linux/rights" ~/.config/radicale/rights
rm -rf ~/.config/radicale/users
touch ~/.config/radicale/users
mkdir ~/.config/radicale/log

echo "Radicale server is installed, starting the server..."
cd ~/.config/radicale/log

radicale -d -S

echo "Radicale server started"

#delete radicale folder
cd "$cwd/calendar-service/"

read -e -p "Would you like to start Calendar Service right now? (Y/N) :" input

if [ "$input" == "y" ] || [ "$input" == "Y" ]; then
	source $cwd/calendar-service/calendar-service.sh start
elif [ "$input" == "n" ] || [ "$input" == "N" ]; then
	exit 1
fi
