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
package org.objectweb.proactive.examples.algebra;

import java.io.Serializable;

public class Vector extends Object implements Serializable, Cloneable {

  int size;
  double[] elements;


  public Vector() {
    super();
  }


  public Vector(int _size) {
    super();
    this.elements = new double[_size];
    this.size = _size;
  }


  public Vector(double[] tab) {
    this(tab.length);
    int index;
    for (index = 0; index < tab.length; index++) {
      this.setElement(index, tab[index]);
    }
  }


  public void randomizeFillIn() {
    int i;

    for (i = 0; i < this.size; i++) {
      this.setElement(i, Math.random());
    }
    return;
  }


  public int getSize() {
    return this.size;
  }


  public synchronized void setElement(int index, double x) {
    this.elements[index] = x;
    return;
  }


  public synchronized double getElement(int index) {
    return this.elements[index];
  }


  public synchronized Vector multiplicate(double a) {
    int index;
    Vector v;

    v = new Vector(this.size);

    for (index = 0; index < this.size; index++) {
      v.setElement(index, a * this.getElement(index));
    }

    return v;
  }


  public double distance(Vector _v) {
    if (_v.getSize() != this.getSize())
      return -1;

    int i;
    double max = 0, current = 0;
    for (i = 0; i < _v.getSize(); i++) {
      current = Math.abs(_v.getElement(i) - this.getElement(i));
      if (current > max)
        max = current;
    }
    return max;
  }


  public Vector concat(Vector _v) {
    int i;
    Vector result = new Vector(this.getSize() + _v.getSize());

    for (i = 0; i < this.getSize(); i++) {
      result.setElement(i, this.getElement(i));
    }
    for (i = 0; i < _v.getSize(); i++) {
      result.setElement(i + this.getSize(), _v.getElement(i));
    }
    return result;
  }


  public void display() {
    int i;
    System.out.println("___");
    for (i = 0; i < this.size; i++) {
      System.out.println("|" + this.getElement(i) + "|");
    }
    System.out.println("---");
    return;
  }


  public synchronized Vector add(Vector a) {
    int index;
    Vector v;

    v = new Vector(this.size);

    for (index = 0; index < size; index++) {
      v.setElement(index, a.getElement(index) + this.getElement(index));
    }

    return v;
  }
}
