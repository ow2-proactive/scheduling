#!/bin/bash
# Automatic install Radical server
# Updated to use python 3.4

script_dir=$(dirname "$0")
localRadicale="$(cd ~/.config/radicale; pwd)"
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
  rm -rf "$localRadicale/"*
fi

mkdir -p "$localRadicale/collections"
mkdir -p "$localRadicale/log"

cp -f "$script_dir/radicale/linux/config" "$localRadicale/config"
cp -f "$script_dir/radicale/linux/logging" "$localRadicale/logging"
cp -f "$script_dir/radicale/linux/rights" "$localRadicale/rights"

source "$script_dir/radicale.sh" start
