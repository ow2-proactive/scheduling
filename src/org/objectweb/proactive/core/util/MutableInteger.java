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
package org.objectweb.proactive.core.util;

import java.io.Serializable;

/**
 * Defines a mutable, serializable and not final Integer.
 * @author cdelbe
 */
public class MutableInteger implements Serializable {

    private int value;
    
    public MutableInteger(){}
    
    public MutableInteger(int value) {
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
    
    public void setValue(int value){
        this.value = value;
    }
    
    
    public int add(int toAdd){
        this.value+=toAdd;
        return this.value;
    }
 
    public int hashCode(){
        return this.value;
    }
    
    public boolean equals(Object mi){
        if (mi instanceof MutableInteger) {
            return this.value==((MutableInteger)mi).getValue();
        } else {
            return false;
        }
    }
    
}
