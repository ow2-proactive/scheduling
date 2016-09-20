#!/bin/bash
# Automatic install Radical server
# Updated to use python 3.4

## Install required packages
if [[ $(id -u) -ne 0 ]] ; then echo "Please run the installation script as root" ; exit 1 ; fi

apt-get update
apt-get -y install python3
apt-get -y install python3-setuptools
apt-get -y install apache2-utils

cd ..
cwd=$(pwd)
## Install radicale
cd $cwd/tools/radicale/linux
tar xvzf Radicale-1.1.1.tar.gz
cd Radicale-1.1.1
python3 setup.py install

mkdir /etc/radicale
mkdir -p ~/.config/radicale/collections

#check if path is not empty string
loginFile="$cwd/config/authentication/login.cfg"
#check if file exist
if [ ! -e "$loginFile" ] 
    then
    echo "authentication file doesnt exists"
    exit
fi

cp -f $cwd/tools/radicale/linux/config /etc/radicale/config
rm -rf /etc/radicale/users
touch /etc/radicale/users

while IFS=':' read -r user password
do
    htpasswd -bs /etc/radicale/users "$user" "$password"    
done < "$loginFile"

echo "Radicale server is installed, starting the server..."

radicale -d -S

echo "Radicale server started"

#delete radicale folder
rm -rf $cwd/tools/radicale/linux/Radicale-1.1.1

cd $cwd/tools

read -e -p "Would you like to start Calendar Service right now? (Y/N) :" input

answer=$input
if ((answer == "y")) || ((answer == "Y"))
	then
	source $cwd/bin/calendar-service.sh start
elif ((answer == "n")) || ((answer == "N"))
	then
	exit 1
fi
