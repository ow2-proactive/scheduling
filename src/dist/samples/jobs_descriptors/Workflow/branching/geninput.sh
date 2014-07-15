
lines=30
chars=80

if [ $? -gt 1 ]; then
	lines=$1
fi
if [ $? -gt 2 ]; then
	chars=$2
fi

for i in $(seq $lines); do
	r=$(($RANDOM % $chars)); 
	for i in $(seq $r); do
		echo -n >> input .; 
	done;
	echo >> input; 
done
