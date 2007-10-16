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
package org.objectweb.proactive.examples.algebra;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Column extends Vector implements Serializable, Cloneable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public Column(int _size) {
        super(_size);
    }

    public Column(double[] tab) {
        super(tab);
    }

    /*public Column (Vector v_)
       {
         super(v_.elements);
       }*/
    @Override
    public synchronized void display() {
        int i;

        for (i = 0; i < this.size; i++) {
            System.out.println(this.getElement(i));
        }

        return;
    }

    /*
       public Column multiplicate (double a)
         {
           Vector v;
           Column c;
           v = super.multiplicate (a);
           c = new Column (v);
           return c;
         }*/
}
