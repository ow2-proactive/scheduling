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
package org.objectweb.proactive.extensions.logforwarder;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;


public class AppenderTest extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent arg0) {
        if (arg0.getLevel().equals(Level.INFO)) {
            System.out.println("======INFO======> " + arg0.getLoggerName() +
                " : " + arg0.getRenderedMessage());
        } else if (arg0.getLevel().equals(Level.ERROR)) {
            System.out.println("======ERROR=====> " + arg0.getLoggerName() +
                " : " + arg0.getRenderedMessage());
        } else {
            System.out.println("======OTHER=====> " + arg0.getLoggerName() +
                " : " + arg0.getRenderedMessage());
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean requiresLayout() {
        // TODO Auto-generated method stub
        return false;
    }
}
