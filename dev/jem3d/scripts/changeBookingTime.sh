if [ $# -ne 2 ]
then
echo "Usage: $0 <file> <newNumber>" 
exit
fi

sed "s|\(.*bookingDuration>\)\(.*\)\(</bookingDuration\)|\1$2\3|" $1
