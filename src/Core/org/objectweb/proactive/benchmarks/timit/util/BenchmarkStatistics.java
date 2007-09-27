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
package org.objectweb.proactive.benchmarks.timit.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.objectweb.proactive.ProActive;


/**
 * This class represents a set of statistics for one run. It contains
 * informations about memory, Java version, OS version, ProActive version and
 * number of processors available.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class BenchmarkStatistics implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2107445173337154096L;
    private HierarchicalTimerStatistics time;
    private EventStatistics events;

    // private long memory;
    // private int nbProc;
    private String information;

    public BenchmarkStatistics() {
    }

    public BenchmarkStatistics(HierarchicalTimerStatistics time,
        EventStatistics events, String information) {
        this.time = time;
        this.events = events;
        // this.memory=memory;
        // this.nbProc=nbProc;
        this.information = information;
    }

    public HierarchicalTimerStatistics getTimerStatistics() {
        // if( time == null ) {
        // return new HierarchicalTimerStatistics();
        // }
        return this.time;
    }

    public EventStatistics getEventsStatistics() {
        // if( events == null ) {
        // return new EventStatistics();
        // }
        return this.events;
    }

    public String getFinalEnvironment() {
        String res = "Deployer environment :";
        // res += information;
        // res += "\nAvailable processors by JVM : "+nbProc;
        // res += "\nTotal Memory by JVM: "+(memory/1024.0/1024.0)+"MB";
        res += ("\nOS Version : " + System.getProperty("os.arch") + " " +
        System.getProperty("os.name") + " " + System.getProperty("os.version"));
        res += ("\nJava Version : " + System.getProperty("java.version"));
        res += "\nProactive Version : ";
        try {
            res += (String) ProActive.class.getMethod("getProActiveVersion",
                new Class[0]).invoke(null, new Object[0]);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            res += "N/A (<3.1)";
        }

        return res;
    }

    public String getInformation() {
        if (this.information == null) {
            return "";
        }
        return this.information;
    }

    @Override
    public String toString() {
        return "\n" +
        (((this.time != null) && (this.time.toString().length() != 0))
        ? ("Timers statistics\n" + "=================\n" +
        this.time.toString() + "\n\n") : "") +
        (((this.events != null) && (this.events.toString().length() != 0))
        ? ("Events statistics\n" + "=================\n" +
        this.events.toString() + "\n") : "");
    }
}
