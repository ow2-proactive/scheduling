TO_KILL=` ps ax|grep java|grep -v eclipse|grep -v grep|awk '{print $1}'`
for p in $TO_KILL; do
	echo "killing $p";
	kill -9 $p;
done
echo "killing rmid";
killall rmid 2>/dev/null
echo "killing registry";
killall rmiregistry 2>/dev/null
