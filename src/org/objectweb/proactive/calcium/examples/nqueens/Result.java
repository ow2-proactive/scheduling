package org.objectweb.proactive.calcium.examples.nqueens;

import java.io.Serializable;

public class Result implements Serializable{

	//solutions vector
	public long solutions[];
	public int n;
	
	public Result (int n){

		this.solutions = new long[n];
		this.n=n;
		
		for(int i=0;i<n;i++){
			solutions[i]=0;
		}
	}
	
}
