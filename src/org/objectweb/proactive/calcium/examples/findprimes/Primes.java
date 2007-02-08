package org.objectweb.proactive.calcium.examples.findprimes;

import java.io.Serializable;
import java.util.Vector;

public class Primes implements Serializable {

	public Vector<Integer> primes;
	
	public Primes(){
		primes=new Vector<Integer>();
	}
}
