#!/bin/bash
# Automatic install Radical server
# Updated to use python 3.4

script_dir=$(dirname "$0")
radicale_version="1.1.1"

which python3
python3Installed=$?

if [ "$python3Installed" -ne 0 ] ; then
  ## Install required packages
  sudo apt-get update
  sudo apt-get -y install python3
  sudo apt-get -y install python3-setuptools
else  	
  echo "Python3 is already installed"
fi

which radicale
radicaleInstalled=$?

if [ "$radicaleInstalled" -ne 0 ] ; then
  ## Install radicale
  cd "$script_dir/radicale/linux"
  tar xvzf "Radicale-$radicale_version.tar.gz"
  cd "Radicale-$radicale_version"
  sudo python3 setup.py install
  cd "$script_dir"
  sudo rm -rf "$script_dir/radicale/linux/Radicale-$radicale_version"
else
  echo "Radicale is already installed"  
  source "$script_dir/radicale.sh" stop
  rm -rf ~/.config/radicale/*
fi

mkdir -p ~/.config/radicale/collections
mkdir -p ~/.config/radicale/log

cp -f "$script_dir/radicale/linux/config" ~/.config/radicale/config
cp -f "$script_dir/radicale/linux/logging" ~/.config/radicale/logging
cp -f "$script_dir/radicale/linux/rights" ~/.config/radicale/rights

source "$script_dir/radicale.sh" start

