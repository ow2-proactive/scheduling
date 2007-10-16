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
package org.objectweb.proactive.benchmarks.timit.util.basic;

import java.util.Iterator;
import java.util.List;

import org.objectweb.proactive.benchmarks.timit.result.BasicResultWriter;


/**
 * A bag of timers results.
 * @author vbodnart
 */
public class ResultBag {
    protected String className;
    protected String uniqueID;
    protected String otherInformation;
    protected List<BasicTimer> timersList;

    public ResultBag(final String className, final String uniqueID,
        final List<BasicTimer> timersList, final String otherInformation) {
        this.className = className;
        this.uniqueID = uniqueID;
        this.timersList = timersList;
        this.otherInformation = otherInformation;
    }

    public void printXMLFormattedTree() {
        BasicResultWriter s = new BasicResultWriter("output" + this.uniqueID);
        s.addTimersElement(this);
        s.printMe();
    }

    public void printToFile() {
        BasicResultWriter s = new BasicResultWriter("output" + this.uniqueID);
        s.addTimersElement(this);
        s.writeToFile();
    }

    public void addResultsTo(BasicResultWriter writer) {
        writer.addTimersElement(this);
    }

    public String toString() {
        String result = className + " : " + " shortUniqueID : " +
            this.uniqueID + "\n";
        Iterator<BasicTimer> it = this.timersList.iterator();
        while (it.hasNext()) {
            result += ("" + it.next() + "\n");
        }
        return result;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the otherInformation
     */
    public String getOtherInformation() {
        return otherInformation;
    }

    /**
     * @param otherInformation the otherInformation to set
     */
    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    /**
     * @return the timersList
     */
    public List<BasicTimer> getTimersList() {
        return timersList;
    }

    /**
     * @param timersList the timersList to set
     */
    public void setTimersList(List<BasicTimer> timersList) {
        this.timersList = timersList;
    }

    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID the uniqueID to set
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }
}
