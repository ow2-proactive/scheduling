/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.mpi.control;

public class ProActiveMPIData implements java.io.Serializable {
    private int jobID = 0;
    private int TAG1 = 0;

    // number of element in the message
    private int count = 0;

    // form sender of the message
    private int src = 0;

    // receiver of message
    private int dest = 0;

    // type of data in buffer
    private int datatype = 0;

    // special tag
    private int tag = 0;

    // name of the method called on user object
    private String method = null;

    //  name of the method called on user object
    private String clazz = null;

    // data
    private byte[] data;

    // parameters call from native code 
    private String parameters;

    // the tab of parameters
    private String[] params;

    /////////////////////
    ///// SETTERS  //////
    /////////////////////
    public void setJobID(int idJob) {
        this.jobID = idJob;
    }

    public void setData(byte[] data) {
        //  this.data = new byte[data.length];
        this.data = data;
    }

    public void parseParameters() {
        if (parameters != null) {
            this.params = parameters.split("\t");
        }
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public void setDest(int dest) {
        this.dest = dest;
    }

    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    /////////////////////
    ///// GETTERS  //////
    /////////////////////
    public int getTag1() {
        return this.TAG1;
    }

    public int getSrc() {
        return this.src;
    }

    public int getDest() {
        return this.dest;
    }

    public int getjobID() {
        return jobID;
    }

    public String getMethod() {
        return method;
    }

    public String getClazz() {
        return clazz;
    }

    public int getDatatype() {
        return datatype;
    }

    public String[] getParams() {
        return params;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n ######## Message Received ######### ");
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n idJob: " + this.jobID);
        sb.append("\n TAG1: " + this.TAG1);
        sb.append("\n Count: " + this.count);
        sb.append("\n src: " + this.src);
        sb.append("\n dest: " + this.dest);
        sb.append("\n datatype: " + this.datatype);
        return sb.toString();
    }

    public int getCount() {
        return count;
    }
}
