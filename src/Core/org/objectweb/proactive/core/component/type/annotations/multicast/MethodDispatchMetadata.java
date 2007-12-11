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
package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.proactive.annotation.PublicAPI;

/**
 * Annotates a method: it specifies the dispatch mode applicable for <b>all</b>
 * parameters of the method.
 * <br>
 * Example:
 * <br>
 * All parameters are dispatched using the BROADCAST mode:
 * <pre>
 * &#064;MethodDispatchMetadata(mode = &#064;ParamDispatchMetadata(mode = ParamDispatchMode.BROADCAST))
 * void compute(List&lt;String&gt; args, String other);
 * </pre>
 *
 * @see org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode
 * @see org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata
 *
 * @author Matthieu Morel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@PublicAPI
public @interface MethodDispatchMetadata {
    /**
     * Specifies the dispatch mode.
     *
     * @return the dispatch mode
     */
    ParamDispatchMetadata mode();
}
