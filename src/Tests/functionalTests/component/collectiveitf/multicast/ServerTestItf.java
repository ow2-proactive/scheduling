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
package functionalTests.component.collectiveitf.multicast;

import java.util.List;


public interface ServerTestItf {
    //  public void  processOutputMessage(@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE) List<Message> message);
    public WrappedInteger testBroadcast_Param(
        List<WrappedInteger> listOfMyObject);

    public WrappedInteger testBroadcast_Method(
        List<WrappedInteger> listOfMyObject);

    public WrappedInteger testOneToOne_Param(WrappedInteger a);

    public WrappedInteger testOneToOne_Method(WrappedInteger a);

    public WrappedInteger testRoundRobin_Param(WrappedInteger a);

    public WrappedInteger testRoundRobin_Method(WrappedInteger a);

    public WrappedInteger testCustom_Param(WrappedInteger a);

    public WrappedInteger testCustom_Method(WrappedInteger a);

    public WrappedInteger testAllStdModes_Param(
        List<WrappedInteger> defaultDispatch,
        List<WrappedInteger> broadcastDispatch,
        WrappedInteger oneToOneDispatch, WrappedInteger roundRobinDispatch,
        WrappedInteger singleElement);
}
