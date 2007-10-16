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
package org.objectweb.proactive.examples.components.helloworld;

public class ServerImpl implements Service, ServiceAttributes {
    private String header = "";
    private int count = 0;

    public ServerImpl() {
        // the following instruction was removed, because ProActive requires empty no-args constructors
        // otherwise this instruction is executed also at the construction of the stubs
        // System.err.println("SERVER created");
    }

    public void print(final String msg) {
        new Exception() {
                @Override
                public String toString() {
                    return "Server: print method called";
                }
            }.printStackTrace();
        System.err.println("Server: begin printing...");
        for (int i = 0; i < count; ++i) {
            System.err.println(header + msg);
        }
        System.err.println("Server: print done.");
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }
}
