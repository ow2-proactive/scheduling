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

    @Override
    public void start(int n) {
    }

    @Override
    public void stop(int n) {
    }

    public void resetTimer(int n) {
    }

    @Override
    public void setValue(int n, int t) {
    }

    @Override
    public void addValue(int n, int t) {
    }

    @Override
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
