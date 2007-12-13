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
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;


/**
 * This skeleton represents Divide and Conquer parallelism (data parallelism).
 * A {@link Divide}, {@link Condition}, and {@link Conquer} objects must be passed as
 * parameter.
 *
 * If the {@link Condition} is met (<code>true</code>), a parameter <code>P</code> will
 * be divided using the {@link Divide} object. If the {@link Condition} is not met (<code>false</code>),
 * the nested skeleton will be executed.
 *
 * Once the nested skeleton has finished its execution, the {@link Conquer} object will be invoked to reduce
 * the results into a single one.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class DaC<P extends java.io.Serializable, R extends java.io.Serializable> implements Skeleton<P, R> {
    Divide<P, P> div;
    Conquer<R, R> conq;
    Condition<P> cond;
    Skeleton<P, R> child;

    /**
     * Creates a Divide and Conquer {@link Skeleton}.
     *
     * @param div
     *            {@link Divide}s a single parameter into several
     * @param cond
     *            <code>true</code> if {@link Divide} should be applied to the parameter, <code>false</code> if not.
     * @param child
     *            The nested {@link Skeleton} that will be invoked.
     * @param conq
     *            {@link Conquer}s the results of the nested {@link Skeleton} into a single task.
     */
    public DaC(Divide<P, P> div, Condition<P> cond, Skeleton<P, R> child, Conquer<R, R> conq) {
        this.div = div;
        this.cond = cond;
        this.child = child;
        this.conq = conq;
    }

    /**
     * This method wraps the {@link Execute} parameter into a {@link Seq}, and then invokes the
     * other constructor {@link #DaC(Divide, Condition, Skeleton, Conquer)}}.
     *
     * @param muscle The muscle to wrap in a {@link Seq} {@link Skeleton}
     */
    public DaC(Divide<P, P> div, Condition<P> cond, Execute<P, R> muscle, Conquer<R, R> conq) {
        this.div = div;
        this.cond = cond;
        this.child = new Seq<P, R>(muscle);
        this.conq = conq;
    }

    @Override
    public String toString() {
        return "DaC";
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
