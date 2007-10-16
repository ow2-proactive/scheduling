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
package org.objectweb.proactive;


/**
 * <P>
 * Active is the root of the all interfaces related to the activity of an
 * active object. It is a convenience for having a common interface that
 * can be used to type objects implementing one or more of the others.
 * </P><P>
 * So far we considered three steps in the lifecycle of an active object.
 * </P>
 * <ul>
 * <li> the initialization of the activity (done only once)</li>
 * <li> the activity itself</li>
 * <li> the end of the activity (unique event)</li>
 * </ul>
 * <P>
 * In case of a migration, an active object stops and restarts its activity
 * automatically without invoking the init or ending phases. Only the
 * activity itself is restarted.
 * </P>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 */
public interface Active {
}
