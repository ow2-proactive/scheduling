filename=$1
echo "using filename $filename"
destination=`grep "successfully" $filename | awk '{print $2}' | tr '\n' ' '`
echo "Destinations $destination"
prun -1 -no-panda -t 00:05:00 /home1/fabrice/workIbis/Ibis/bin/start_ibis.sh 1  modelisation.forwarder.Agent  $destination
