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


/**
 * This class provides a conditional branching {@link Skeleton}.
 *
 * Depending on the result of the {@link Condition}, either
 * one nested {@link Skeleton} is executed or the other.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class If<P extends java.io.Serializable, R extends java.io.Serializable> implements Skeleton<P, R> {
    Condition<P> cond;
    Skeleton<P, ?> ifChild;
    Skeleton<P, ?> elseChild;

    /**
     * This is the main constructor.
     *
     * @param cond The {@link Condition} to evaluate.
     * @param ifChild The <code>true</code> case {@link Skeleton}.
     * @param elseChild The <code>false</code> case {@link Skeleton}.
     */
    public If(Condition<P> cond, Skeleton<P, R> ifChild, Skeleton<P, R> elseChild) {
        this.cond = cond;
        this.ifChild = ifChild;
        this.elseChild = elseChild;
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
