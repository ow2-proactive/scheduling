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
package org.objectweb.proactive.examples.webservices.c3dWS;

class Hosts {
    protected java.util.Vector allNodes = new java.util.Vector();
    protected int index = 0;

    public Hosts(String filename) throws java.io.IOException {
        //System.out.println("Using host file " + filename);
        java.io.File f = new java.io.File(filename);

        if (f.canRead()) {
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(f));
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(new String(
                        b));

            while (tokenizer.hasMoreTokens()) {
                allNodes.addElement(tokenizer.nextToken());
            }
        }
    }

    public String getNextNode() {
        // NodeLocator result;
        String result;
        result = (String) allNodes.elementAt(index);
        index = (index + 1) % allNodes.size();

        return result;
    }

    public int getMachines() {
        return allNodes.size();
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in)
        throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];

        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }

        return bytecodes;
    }
}
