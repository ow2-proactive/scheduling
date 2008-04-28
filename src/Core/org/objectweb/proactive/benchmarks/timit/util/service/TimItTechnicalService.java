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
package org.objectweb.proactive.benchmarks.timit.util.service;

import java.util.Map;

import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


/**
 * A Technical Service for timing purpose.
 * @author The ProActive Team
 */
public class TimItTechnicalService implements TechnicalService, java.io.Serializable {
    private String timitActivation;
    private String reduceResults;
    private static String generateOutputFile;
    private static String printOutput;

    /**
     * The init method of this service.
     * @param argValues A map of args values
     */
    public void init(Map<String, String> argValues) {
        // First activate timers on the deploying VM
        this.timitActivation = (String) argValues.get("timitActivation");
        if ((this.timitActivation != null) && (!"".equals(this.timitActivation))) {
            TimerWarehouse.enableTimers();
        }
        this.reduceResults = (String) argValues.get("reduceResults");
        setGenerateOutputFile((String) argValues.get("generateOutputFile"));
        setPrintOutput((String) argValues.get("printOutput"));
    }

    /**
     * The apply method of this service.
     * @param node The node on which the properties will be applied
     */
    public void apply(Node node) {
        try {
            if (this.timitActivation != null) {
                node.setProperty("timitActivation", this.timitActivation);
            }
            if (this.reduceResults != null) {
                node.setProperty("reduceResults", this.reduceResults);
            }
            if (generateOutputFile != null) {
                node.setProperty("generateOutputFile", generateOutputFile);
            }
            if (printOutput != null) {
                node.setProperty("printOutput", printOutput);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A setter for the generateOutputFile property.
     * @param value The value of the generateOutputFile property
     */
    public static void setGenerateOutputFile(String value) {
        generateOutputFile = value;
    }

    /**
     * A setter for the printOutput property .
     * @param value The value of the property
     */
    public static void setPrintOutput(String value) {
        printOutput = value;
    }

    /**
     * A getter for the generateOutputFile property
     * @return The generateOutputFile property
     */
    public static String getGenerateOutputFile() {
        return ((generateOutputFile == null) ? "false" : generateOutputFile);
    }

    /**
     * A getter for the printOutput property.
     * @return The printOutput property
     */
    public static String getPrintOutput() {
        return ((printOutput == null) ? "false" : printOutput);
    }
}
