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

import java.io.Serializable;


/**
 * Skeleton structure visitors must implement this interface.
 *
 * @author The ProActive Team (mleyton)
 */
public interface SkeletonVisitor {
    public <P extends Serializable, R extends Serializable> void visit(Farm<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Pipe<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Seq<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(If<P, R> skeleton);

    public <P extends Serializable> void visit(For<P> skeleton);

    public <P extends Serializable> void visit(While<P> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Map<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(Fork<P, R> skeleton);

    public <P extends Serializable, R extends Serializable> void visit(DaC<P, R> skeleton);
}
