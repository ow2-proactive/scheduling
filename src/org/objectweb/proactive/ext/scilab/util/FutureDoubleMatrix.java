package org.objectweb.proactive.ext.scilab.util;

/**
 * This class represents a matrix of double with a waiting by necessity
 */
public class FutureDoubleMatrix {
	private double[] res;
	private String name;
	private int nbRow;
	private int nbCol;
	
	public FutureDoubleMatrix(String name, int nbRow, int nbCol){
		this.name = name;
		this.nbRow = nbRow;
		this.nbCol = nbCol;
	} 
	
	public synchronized void set(double[] res) {
		this.res = res;
		this.notifyAll();
	}
	
	public synchronized double[] get() {
		try{
			while (res == null) {
				this.wait();
			}
		}
		catch(InterruptedException e){
		}
			
		return res;
	}

	public String getName() {
		return name;
	}

	public int getNbCol() {
		return nbCol;
	}

	public int getNbRow() {
		return nbRow;
	}
}