#!/usr/local/bin/zsh

if [ ! $# -ne 2 ] 
then
echo "Usage : $0 certificat_name"
else

openssl req -x509 -newkey rsa:1024 -keyout $1.key -out $1.cert -outform PEM
openssl pkcs8 -topk8 -outform DER -nocrypt < $1.key >! $1.key.der
rm $1.key
mv $1.key.der $1.key
fi
