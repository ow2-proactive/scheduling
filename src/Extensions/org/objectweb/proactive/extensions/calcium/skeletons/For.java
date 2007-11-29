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
 * The <code>For</code> {@link Skeleton} represents iteration. It is used
 * to execute a nested {@link Skeleton} a specific number of times.
 */
@PublicAPI
public class For<P extends java.io.Serializable> implements Skeleton<P, P> {
    Skeleton<P, P> child;
    int times;

    /**
     * This is the main constructor.  
     * 
     * @param times The number of times to execute the nested {@link Skeleton}
     * @param nested The nested {@link Skeleton}.
     */
    public For(int times, Skeleton<P, P> nested) {
        this.child = nested;
        this.times = times;
    }

    /**
     * This constructor wraps the {@link Execute} parameter in a {@link Seq}
     * skeleton and invokes the main constructor: {@link For#For(int, Skeleton)}.
     * 
     * @param muscle The muscle to wrap in a {@link Seq} {@link Skeleton}.
     */
    public For(int times, Execute<P, P> muscle) {
        this.child = new Seq<P, P>(muscle);
        this.times = times;
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
