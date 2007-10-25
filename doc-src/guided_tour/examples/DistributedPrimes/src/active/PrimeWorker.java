package active;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;

public class PrimeWorker implements Serializable{
	private Vector<LongWrapper> primes = new Vector<LongWrapper>();
	public PrimeWorker(){}//empty no-arg constructor needed by ProActive
	//check for primes
	public BooleanWrapper isPrime(LongWrapper number){
		if (primes.isEmpty()) //if no number has been received yet
			return new BooleanWrapper(true);
		else {
			int i=0;
			int size = primes.size(); //store the size of the vector so we don't call the size() method repeatedly
			long value = number.longValue(); //store the value so we don't call the longValue() repeatedly
			while ((i < size) &&  //has not reached the end of the Vector
					(value % primes.get(i).longValue() != 0)) //does not divide
				i++;
			//if it goes through all the Vector then it is prime
			if (i == size) return  new BooleanWrapper(true);
				else return new BooleanWrapper(false);
		}
	}
	//add a prime to the Vector 
	public void addPrime(LongWrapper number){
		primes.add(number);
	}
}
