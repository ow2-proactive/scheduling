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
import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;


/**
 * The <code>While</code> {@link Skeleton} represents conditioned iteration.
 * The nested {@link Skeleton} will be executed while the {@link Condition} holds <code>true</code>.
 *
 * @author The ProActive Team (mleyton)
 *
 */
@PublicAPI
public class While<P extends java.io.Serializable> implements Skeleton<P, P> {
    Condition<P> cond;
    Skeleton<P, P> child;

    /**
     * The main constructor.
     *
     * @param cond The {@link Condition} that will be evaluated on each iteration.
     * @param nested The nested skeleton that will be invoked.
     */
    public While(Condition<P> cond, Skeleton<P, P> nested) {
        this.cond = cond;
        this.child = nested;
    }

    /**
     * Like the main constructor, but accepts {@link Execute} objects.
     *
     * The {@link Execute} object will be wrapped in a {@link Seq} {@link Skeleton}.
     *
     * @param cond The {@link Condition} that will be evaluated on each iteration.
     * @param muscle The {@link Execute} that will be invoked on each iteration.
     */
    public While(Condition<P> cond, Execute<P, P> muscle) {
        this.cond = cond;
        this.child = new Seq<P, P>(muscle);
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
