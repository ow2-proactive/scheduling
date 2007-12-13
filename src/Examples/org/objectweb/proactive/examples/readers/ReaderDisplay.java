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
package org.objectweb.proactive.examples.readers;

public class ReaderDisplay implements org.objectweb.proactive.InitActive {
    private AppletReader applet;
    private ReaderWriter rw;
    private Reader[] readers;
    private Writer[] writers;

    public ReaderDisplay() {
    }

    public ReaderDisplay(AppletReader applet) {
        this.applet = applet;
    }

    public void initActivity(org.objectweb.proactive.Body body) {
        readers = new Reader[3];
        writers = new Writer[3];

        Object[] param = new Object[] { org.objectweb.proactive.api.PAActiveObject.getStubOnThis(),
                new Integer(ReaderWriter.DEFAULT_POLICY) };
        try {
            rw = (ReaderWriter) org.objectweb.proactive.api.PAActiveObject.newActive(ReaderWriter.class
                    .getName(), param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        param = new Object[3];
        param[0] = org.objectweb.proactive.api.PAActiveObject.getStubOnThis();
        param[1] = rw;
        for (int i = 0; i < 3; i++) {
            param[2] = new Integer(i);
            try {
                // Readers
                readers[i] = (Reader) org.objectweb.proactive.api.PAActiveObject.newActive(Reader.class
                        .getName(), param);
                // Writers
                writers[i] = (Writer) org.objectweb.proactive.api.PAActiveObject.newActive(Writer.class
                        .getName(), param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setRead(int id, boolean state) {
        applet.readerPanel.setRead(id, state);
    }

    public void setWait(int id, boolean isReader) {
        applet.readerPanel.setWait(id, isReader);
    }

    public void setWrite(int id, boolean state) {
        applet.readerPanel.setWrite(id, state);
    }

    public void setPolicy(int policy) {
        if ((policy < 3) && (policy > -1)) {
            rw.setPolicy(policy);
        }
    }
}
