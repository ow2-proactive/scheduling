if [ $# -ne 2 ]
then
echo "Usage: $0 <file> <newNumber>" 
exit
fi

sed "s|\(.*hostsNumber>\)\(.*\)\(</hostsNumber\)|\1$2\3|" $1
