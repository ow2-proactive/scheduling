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
package org.objectweb.proactive.extensions.calcium.skeletons;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;


/**
 * The <code>Seq</code> skeleton is a wrapper for {@link Execute} muscle ibjects.
 * It is mainly used as a terminal {@link Skeleton}, ie it does not nest other {@link Skeleton}s.
 *
 * @author The ProActive Team (mleyton)
 *
 */
@PublicAPI
public class Seq<P extends java.io.Serializable, R extends java.io.Serializable>
    implements Skeleton<P, R> {
    Execute<P, R> secCode;

    /**
     * The constructor.
     *
     * @param secCode The {@link Execute} muscle that will be wrapped in this {@link Skeleton}
     */
    public Seq(Execute<P, R> secCode) {
        this.secCode = secCode;
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Seq(" + this.secCode.getClass() + ")";
    }
}
