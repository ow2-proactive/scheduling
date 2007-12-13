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
package org.objectweb.proactive.core.migration;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * MigrationStrategyImpl contains destination. It allows us to program an Agent to follow an itinary
 * so that it can perform automaticly some method upon arrival on a site.
 * An migrationStrategy is instanciated by the user.
 */
public class MigrationStrategyImpl implements java.io.Serializable, MigrationStrategy {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    private java.util.Vector<Destination> table;
    private int index;

    /**
     * Creates an empty MigrationStrategyImpl
     */
    public MigrationStrategyImpl() {
        super();
        table = new java.util.Vector<Destination>();
        index = -1; //Negative value for first execution of getNextDestination
    }

    /**
     * Creates an itinary using a text-file.
     * Should implement verification of the existence of the methods to avoid
     * problems at run-time.
     */
    public MigrationStrategyImpl(String filename) {
        super();
        table = new java.util.Vector<Destination>();
        index = -1; //Negative value for first execution of getNextDestination
        String s;
        java.io.FileReader f_in = null;
        try {
            f_in = new java.io.FileReader(filename);
        } catch (java.io.FileNotFoundException e) {
            logger.error("File not Found");
        }

        // on ouvre un "lecteur" sur ce fichier
        java.io.BufferedReader _in = new java.io.BufferedReader(f_in);

        // on lit a partir de ce fichier
        // NB : a priori on ne sait pas combien de lignes on va lire !!
        try {
            // tant qu'il y a quelque chose a lire
            while (_in.ready()) {
                // on le lit
                s = _in.readLine();
                java.util.StringTokenizer tokens = new java.util.StringTokenizer(s, " ");
                this.add(new NodeDestination(new String(tokens.nextToken()), tokens.nextToken()));
            }
        } // catch (IOException e) {}
        catch (Exception e) {
        }

        try {
            _in.close();
        } catch (java.io.IOException e) {
        }
    }

    /**
     * Adds a Destination to an itinary
     */
    public void add(Destination r) {
        table.addElement(r);
    }

    public void add(String nodeURL, String method) {
        table.addElement(new NodeDestination(nodeURL, method));
    }

    /**
     * Adds a Destination for the next migration
     *
     */
    public void addNext(Destination r) {
        if ((index == -1) || (index == (table.size() - 1))) {
            table.addElement(r);
        } else {
            table.add(index + 1, r);
        }
    }

    public void addNext(String nodeURL, String method) {
        if ((index == -1) || (index == (table.size() - 1))) {
            table.addElement(new NodeDestination(nodeURL, method));
        } else {
            table.add(index + 1, new NodeDestination(nodeURL, method));
        }
    }

    public void remove(String nodeURL, String method) {
        removeFromItinerary(new NodeDestination(nodeURL, method));
    }

    public void remove(Destination d) {
        removeFromItinerary(d);
    }

    //Maybe we should return something
    private void removeFromItinerary(Destination r) {
        //System.out.println("MigrationStrategyImpl: removeFromItinerary() the result is " + table.removeElement(r));
        int i = 0;
        Destination r2;
        while (i < table.size()) {
            r2 = table.elementAt(i);
            if ((r2.getDestination().equals(r.getDestination())) &&
                (r2.getMethodName().equals(r.getMethodName()))) {
                table.removeElementAt(i);
                //we have removed an element before the index , so we shift the index
                if (i < index) {
                    index--;
                }
                return;
            }
            i++;
        }
    }

    /**
     * Returns the next destination on the list and increase index by one.
     * If there is no more destination, then return null
     */
    public Destination next() {
        index++;
        if (index < table.size()) {
            Destination r = table.elementAt(index);

            //index++;
            return (r);
        } else {
            //System.out.println("MigrationStrategyImpl: next() no next destination found");
            return (null);
        }
    }

    /**
     * Returns the current Destination.
     * Returns null if nothing is available
     */
    public Destination getCurrentDestination() {
        if ((index < table.size()) && (index >= 0)) {
            Destination r = table.elementAt(index);
            return (r);
        } else //should never happens
        {
            return (null);
        }
    }

    /**
     * Returns the next destination in the migrationStrategy which is not s
     */
    public Destination getNextExcept(String s) {
        Destination temp;
        if (s == null) {
            return next();
        }
        while ((temp = next()) != null) {
            if (!temp.getDestination().equals(s)) {
                return temp;
            }
        }
        return null;
    }

    public int size() {
        return table.size();
    }

    public void decrease() {
        if (index >= 0) {
            index--;
        }
    }

    public void reset() {
        index = -1; //Negative value for first execution of getNextDestination
    }

    /**
     * Return a java.util.Vector made of strings representing the destinations
     */
    public java.util.Vector<String> toVector() {
        java.util.Vector<String> temp = new java.util.Vector<String>();
        for (int i = 0; i < table.size(); i++) {
            temp.add(table.elementAt(i).getDestination());
        }
        return temp;
    }
}
