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

/**
 * Class used only for performances
 * 
 * @author Brian Amedro, Vladimir Bodnartchouk
 * 
 */
public class FakeTimer extends HierarchicalTimer {
    /**
     * 
     */
    private static final long serialVersionUID = -4023758257227790549L;

    private static HierarchicalTimer timer = new FakeTimer();

    public void start(int n) {
    }

    public void stop(int n) {
    }

    public void resetTimer(int n) {
    }

    public void setValue(int n, int t) {
    }

    public void addValue(int n, int t) {
    }

    public boolean isStarted(int n) {
        return false;
    }

    public int getElapsedTime() {
        return 0;
    }

    public int getHierarchicalTime() {
        return 0;
    }

    public int getTotalTime() {
        return 0;
    }

    public static HierarchicalTimer getInstance() {
        return FakeTimer.timer;
    }
}
