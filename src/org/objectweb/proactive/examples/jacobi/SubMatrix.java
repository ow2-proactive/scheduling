/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.jacobi;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.group.spmd.ProSPMD;
import org.objectweb.proactive.core.group.topology.Plan;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


public class SubMatrix {

	/** Default width value of a submatrix */
	private static final int DEFAULT_WIDTH = 100;
	/** Default height value of a submatrix */
	private static final int DEFAULT_HEIGHT = 50;

	/** Step */
	private int iterationsToStop = Jacobi.ITERATIONS;
	/** Min Diff of the loop */
	private double minDiff = Jacobi.MINDIFF+1;

	/** the name of the submatrix */
	private String name;

	/** Width of the submatrix */
	private int width; 
	/** Height of the submatrix */
	private int height;
	/** the current values */
	private double[] current;
	/** the old values */
	private double[] old;

	/** North submatrix neighbor */
	private SubMatrix north; 
	/** South submatrix neighbor */ 
	private SubMatrix south; 
	/** West submatrix neighbor */ 
	private SubMatrix west; 
	/** East submatrix neighbor */ 
	private SubMatrix east; 

	/** A ProActive reference to this */
	private SubMatrix asyncRefToMe;

	/** The neighbors submatix */
	private SubMatrix neighbors;

	/** The whole matrix */
	private SubMatrix matrix;
	
	/** Border of the north submatrix neighbor */
	private double[] northNeighborBorder; 
	/** Border of the south submatrix neighbor */
	private double[] southNeighborBorder; 
	/** Border of the west submatrix neighbor */
	private double[] westNeighborBorder; 
	/** Border of the east submatrix neighbor */
	private double[] eastNeighborBorder; 
  


	
	/** Empty constructor */
	public SubMatrix() {
		this(SubMatrix.DEFAULT_WIDTH,SubMatrix.DEFAULT_HEIGHT);
	}

	public SubMatrix(String name) {
		this();
		this.name = name;
	}

	/**
	 * Constructor
	 * @param x the width of the submatrix
	 * @param y the height of the matrix
	 */
	public SubMatrix(int x, int y) {
		this.width = x;
		this.height = y;
		this.current = new double[x*y];
		this.old = new double[x*y];
		for (int i = 0 ; i < this.old.length ; i++) {
			this.old[i] = Math.random() * 10000;
		}
	}

	/**
	 * Returns the value at the specified position
	 * @param x the column of the value
	 * @param y the line of the value
	 * @return the value at the specified position
	 */
	public double get(int x, int y) {
		return this.old[(x*this.width)+y];
	}
	
	/**
	 * Builds the central part of the new submatrix that does not require
	 * communication with other submatrix
	 */
	public void internalCompute() {
		int index = this.width+2;
		double current, diff = Jacobi.MINDIFF+1;
		double b,n,a;
		b = this.old[index-1];
		n = this.old[index];
		a = this.old[index+1];
		for (int y = 1 ; y < this.height-1 ; y++) {
			for (int x = 1 ; x < this.width-1 ; x++) {
				current = (b + a + this.old[index-this.width] + this.old[index+this.width])/4;
				this.current[index] = current;
				diff = Math.abs(current - n);
				if (diff < this.minDiff) {
					this.minDiff = diff;
				}
				b = n;
				n = a;
				a = this.old[index+1];
				index++;
			}
			index += 2;
		}
	}

	/**
	 * Builds the border part of the new submatrix that require the
	 * communications with other submatrix are done.
	 */
	public void borderCompute() {

		int index;
		double current, diff = Jacobi.MINDIFF+1;
		// north-west corner
		index = 0;
		current = (this.northNeighborBorder[0] +
						this.old[index+this.width] +
						this.westNeighborBorder[0] +
						this.old[index+1])/4;
		this.current[index] = current;
		diff = Math.abs(current - this.old[index]);
		if (diff < this.minDiff) {
			this.minDiff = diff;
		}

		// north-east corner
		index = this.width-1;
		current = (this.northNeighborBorder[this.width-1] +
						this.old[index+this.width] +
						this.old[index-1] + 
						this.eastNeighborBorder[0])/4;
		this.current[index] = current;
		diff = Math.abs(current - this.old[index]);
		if (diff < this.minDiff) {
			this.minDiff = diff;
		}

		// north border
		for (index = 1 ; index < this.width-1 ; index++) {
			current = (this.northNeighborBorder[index] + 
							this.old[this.width+index] +
							this.old[index-1] + 
							this.old[index+1])/4;
			this.current[index] = current;
			diff = Math.abs(current - this.old[index]);
			if (diff < this.minDiff) {
				this.minDiff = diff;
			}
		}
		
		// south-west corner
		index = (this.width-1)*this.height;
		current = (this.old[index-this.width] + 
						this.southNeighborBorder[0] +
						this.westNeighborBorder[this.height-1] +
						this.old[index+1])/4;
		this.current[index] = current;
		diff = Math.abs(current - this.old[index]);
		if (diff < this.minDiff) {
			this.minDiff = diff;
		}

		// west border
		index = this.width; 
		for (int i = 1 ; i < this.height-1 ; i++) {
			current = (this.old[index-this.width] + 
							this.old[index+this.width] +
							this.westNeighborBorder[i] + 
							this.old[index+1])/4;
			this.current[index] = current;
			diff = Math.abs(current - this.old[index]);
			if (diff < this.minDiff) {
				this.minDiff = diff;
			}
			index += this.width;
		}
				
		// south-east corner
		index = (this.width*this.height)-1;
		current = (this.old[index-this.width] + 
						this.southNeighborBorder[this.width-1] +
						this.old[index-1] + 
						this.eastNeighborBorder[this.height-1])/4;
		this.current[index] = current;
		diff = Math.abs(current - this.old[index]);
		if (diff < this.minDiff) {
			this.minDiff = diff;
		}

		// south border
		index = (this.width*(this.height-1))+1;
		for (int i = 1 ; i < this.width-1 ; i++) {
			current = (this.old[index-this.width] + 
												this.southNeighborBorder[i] +
												this.old[index-1] + 
												this.old[index+1])/4;
			this.current[index] = current;
			diff = Math.abs(current - this.old[index]);
			if (diff < this.minDiff) {
				this.minDiff = diff;
			}
			index++;
		}
		
		// east border
		index = (this.width*2)-1;
		for (int i = 1 ; i < this.height-1 ; i++) {
			current = (this.old[index-this.width] + 
							this.old[index+this.width] +
							this.old[index-1] + 
							this.eastNeighborBorder[i])/4;
			this.current[index] = current;
			diff = Math.abs(current - this.old[index]);
			if (diff < this.minDiff) {
				this.minDiff = diff;
			}
			index += this.width;
		}
	}

	/**
	 * Exchange the old values with the new
	 */
	public void exchange () {
		double[] tmp = this.current;
		this.current = this.old;
		this.old = tmp;
	}

	/**
	 * Connects this submatrix with its neighbors and send them its borders
	 */
	public void buildNeighborhood () {
		this.matrix = (SubMatrix) ProSPMD.getSPMDGroup();
		Group allSubMatrix = ProActiveGroup.getGroup(this.matrix);
		Plan topology = null;
		try {
			topology = new Plan(allSubMatrix,Jacobi.WIDTH,Jacobi.HEIGHT); }
		catch (ConstructionOfReifiedObjectFailedException e) {
			System.err.println("** ConstructionOfReifiedObjectFailedException ** - Unable to build the plan topology");
			e.printStackTrace();
		}
		
		this.asyncRefToMe = (SubMatrix) ProActive.getStubOnThis();
		this.north = (SubMatrix) topology.up(this.asyncRefToMe);
		this.south = (SubMatrix) topology.down(this.asyncRefToMe);
		this.west = (SubMatrix) topology.left(this.asyncRefToMe);
		this.east = (SubMatrix) topology.right(this.asyncRefToMe);
		
		try {
			this.neighbors = (SubMatrix) ProActiveGroup.newGroup(SubMatrix.class.getName()); }
		catch (ClassNotReifiableException e) {
			System.err.println("** ClassNotReifiableException ** - Unable to build the neighbors group");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("** ClassNotFoundException ** - Unable to build the neighbors group");
			e.printStackTrace();
		}
		Group neighborsGroup = ProActiveGroup.getGroup(this.neighbors);

		if (this.north == null) {
			this.northNeighborBorder = this.buildFakeBorder(this.width);
		}
		else {
			neighborsGroup.add(this.north);
			this.north.setSouthBorder(this.buildNorthBorder());
		}
		if (this.south == null) {
			this.southNeighborBorder = this.buildFakeBorder(this.width);
		}
		else {
			neighborsGroup.add(this.south);
			this.south.setNorthBorder(this.buildSouthBorder());
		}
		if (this.west == null) {
			this.westNeighborBorder = this.buildFakeBorder(this.height);
		}
		else {
			neighborsGroup.add(this.west);
			this.west.setEastBorder(this.buildWestBorder());
		}
		if (this.east == null) {
			this.eastNeighborBorder = this.buildFakeBorder(this.height);
		}
		else {
			neighborsGroup.add(this.east);
			this.east.setWestBorder(this.buildEastBorder());
		}
		neighborsGroup.add(this.asyncRefToMe);
	}

	/**
	 * Builds a "fake border" for the external submatrix
	 * filled with DEFAULT_BORDER_VALUE
	 * @param size - the size of the border
	 * @return a "fake border" for the external submatrix
	 */
	private double[] buildFakeBorder(int size) {
 		double[] line = new double[size];
		for (int i = 0 ; i < line.length ; i++) {
			line[i] = Jacobi.DEFAULT_BORDER_VALUE;
		}
		return line;
	}

	/**
	 * Returns the north border of the submatrix
	 * @return the north border of the submatrix
	 */
	private double[] buildNorthBorder () {
		double[] line = new double[this.width];
		for (int i = 0 ; i < this.width ; i++) {
			line[i] = this.old[i];
		}
		return line;
	}

	/**
	 * Returns the south border of the submatrix
	 * @return the south border of the submatrix
	 */
	private double[] buildSouthBorder () {
		double[] line = new double[this.width];
		int index = this.width*(this.height-1);
		for (int i = 0 ; i < this.width ; i++) {
			line[i] = this.old[index++];
		}
		return line;
	}

	/**
	 * Returns the west border of the submatrix
	 * @return the west border of the submatrix
	 */
	private double[] buildWestBorder () {
		double[] line = new double[this.height];
		int index = 0;
		for (int i = 0 ; i < this.height ; i++) {
			line[i] = this.old[index+this.width];
		}
		return line;
	}

	/**
	 * Returns the west border of the submatrix
	 * @return the west border of the submatrix
	 */
	private double[] buildEastBorder () {
		double[] line = new double[this.height];
		int index = this.width;
		for (int i = 0 ; i < this.height ; i++) {
			line[i] = this.old[index+this.width];
		}
		return line;
	}


	/**
	 * Sends the border values to the corresponding neighbors 
	 */
	public void sendBordersToNeighbors () {
		if (this.north != null) {
			this.north.setSouthBorder(this.buildNorthBorder());
		}
		if (this.south != null) {
			this.south.setNorthBorder(this.buildSouthBorder());
		}
		if (this.west != null) {
			this.west.setEastBorder(this.buildWestBorder());
		}
		if (this.east != null) {
			this.east.setWestBorder(this.buildEastBorder());
		}
	}


	/**
	 * Set the north border
	 * @param border - the north border
	 */
	public void setNorthBorder(double[] border) {
		this.northNeighborBorder = border;		
	}

	/**
	 * Set the south border
	 * @param border - the south border
	 */
	public void setSouthBorder(double[] border) {
		this.southNeighborBorder = border;		
	}

	/**
	 * Set the west border
	 * @param border - the west border
	 */
	public void setWestBorder(double[] border) {
		this.westNeighborBorder = border;
	}

	/**
	 * Set the east border
	 * @param border - the east border
	 */
	public void setEastBorder(double[] border) {
		this.eastNeighborBorder = border;
	}

	/**
	 * Launch the calculus
	 */
	public void compute () {
		this.buildNeighborhood();
		ProSPMD.barrier("InitDone");
		this.asyncRefToMe.loop();
	}

	/**
	 * Launch the main loop  
	 */
	public void loop () {
		// System.out.println("iterations : " + this.iterationsToStop);
		// compute the internal values
		this.internalCompute();
		// synchronization to be sure that all submatrix have exchanged borders
		ProSPMD.barrier("SynchronizationWithNeighbors"+this.iterationsToStop, this.neighbors);
		// compute the border values
		this.borderCompute();
		// decrement the iteration counter
		this.iterationsToStop--;
		// send the borders to neighbors
		this.sendBordersToNeighbors();
		// continue or stop ?
		if ((this.iterationsToStop > 0) && (this.minDiff > Jacobi.MINDIFF)) {
			this.exchange();
			this.asyncRefToMe.loop();
		}
		else {
			System.out.println("[" + this.name + "] Computation over :\n      " +
											this.minDiff + " (asked less than " + Jacobi.MINDIFF + ")");
			if (this.minDiff < Jacobi.MINDIFF) {
				System.out.println("[" + this.name + "] sent the \"end signal\"");
				this.matrix.stop();
			}
		}
	}

	/**
	 * Stops the submatrix
	 */
	public void stop () {
		this.iterationsToStop = 0;
	}
}
