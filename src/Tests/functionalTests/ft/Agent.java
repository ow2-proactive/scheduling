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
package functionalTests.ft;

import java.io.Serializable;


/**
 * @author cdelbe
 */
public class Agent implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 5663916362893375558L;
    private Agent neighbour;
    private int counter;
    private int iter;
    private Collector launcher;

    public Agent() {
    }

    public void initCounter(int value) {
        this.counter = value;
        this.iter = 0;
    }

    public void setNeighbour(Agent n) {
        this.neighbour = n;
    }

    public void setLauncher(Collector l) {
        this.launcher = l;
    }

    public ReInt doStuff(ReInt param) {
        this.counter += param.getValue();
        return new ReInt(this.counter);
    }

    public ReInt getCounter() {
        return new ReInt(this.counter);
    }

    public void startComputation(int max) {
        iter++;
        ReInt a = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt b = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt c = this.neighbour.doStuff(new ReInt(this.counter));
        ReInt d = this.neighbour.doStuff(new ReInt(this.counter));
        this.counter += a.getValue();
        this.counter += b.getValue();
        this.counter += c.getValue();
        this.counter += d.getValue();

        if (iter < max) {
            neighbour.startComputation(max);
        } else {
            this.launcher.finished(this.counter);
        }
    }
}
