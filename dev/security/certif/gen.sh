openssl req -x509 -newkey rsa:1024 -outform PEM -keyout $1.key.enc -out $1.cert
openssl pkcs8 -topk8  -outform DER -nocrypt -in $1.key.enc -out $1.key
