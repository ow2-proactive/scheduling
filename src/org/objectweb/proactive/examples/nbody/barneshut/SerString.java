/*
 * Created on Feb 7, 2005
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;

public class SerString implements Serializable{
    String string ;
    SerString  (String s) {string = s;}
    public String toString() {return string;}
    }