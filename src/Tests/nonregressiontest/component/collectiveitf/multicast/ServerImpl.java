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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;

import nonregressiontest.component.Message;

import org.apache.tools.ant.types.Assertions;


public class ServerImpl implements
        ServerTestItf, Identifiable {
    
    int id = 0;


    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#processOutputMessage(nonregressiontest.component.Message)
     */
    public Message processOutputMessage(Message message) {

        
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testAllModes(java.util.List, java.util.List, java.lang.MyObject, java.lang.MyObject)
     */
    public WrappedInteger testAllStdModes_Param(List<WrappedInteger> defaultDispatch, List<WrappedInteger> broadcastDispatch, WrappedInteger oneToOneDispatch, WrappedInteger roundRobinDispatch, WrappedInteger singleElement) {

        testBroadcast_Param(defaultDispatch);
        testBroadcast_Param(broadcastDispatch);
        testOneToOne_Param(oneToOneDispatch);
        testRoundRobin_Param(roundRobinDispatch);
        Assertions.assertTrue(singleElement.getIntValue().equals(42));
        return new WrappedInteger(id);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testBroadcast(java.util.List)
     */
    public WrappedInteger testBroadcast_Param(List<WrappedInteger> listOfMyObject) {

        Assertions.assertTrue(listOfMyObject.size() == Test.NB_CONNECTED_ITFS);
        boolean test = (new WrappedInteger(12).equals(new WrappedInteger(12)));
        Assertions.assertTrue(listOfMyObject.get(0).equals(new WrappedInteger(0)) && listOfMyObject.get(1).equals(new WrappedInteger(1)));
        return new WrappedInteger(id);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testOneToOne(java.lang.MyObject)
     */
    public WrappedInteger testOneToOne_Param(WrappedInteger a) {

        Assertions.assertEquals(a.getIntValue(), id);
        return new WrappedInteger(id);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testRoundRobin(java.lang.MyObject)
     */
    public WrappedInteger testRoundRobin_Param(WrappedInteger a) {

        if (a.getIntValue()<Test.NB_CONNECTED_ITFS) {
            Assertions.assertEquals(a.getIntValue(), id);
        } else {
            Assertions.assertEquals(Test.NB_CONNECTED_ITFS % a.getIntValue(), id);
        }
        return new WrappedInteger(id);
    }

    
    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testCustom(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger testCustom_Param(WrappedInteger a) {

        return a;
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#setID(int)
     */
    public void setID(String id) {

        this.id = new Integer(id);
        
    }
    
    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#getID()
     */
    public String getID() {

        return ((Integer)id).toString();
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testBroadcast_Method(java.util.List)
     */
    public WrappedInteger testBroadcast_Method(List<WrappedInteger> listOfMyObject) {

        return testBroadcast_Param(listOfMyObject);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testCustom_Method(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger testCustom_Method(WrappedInteger a) {

        return testCustom_Param(a);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testOneToOne_Method(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger testOneToOne_Method(WrappedInteger a) {

        return testOneToOne_Param(a);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.ServerTestItf#testRoundRobin_Method(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger testRoundRobin_Method(WrappedInteger a) {

        return testRoundRobin_Param(a);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Itf1#dispatch(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger dispatch(WrappedInteger a) {

        return testOneToOne_Param(a);
    }
    
    
    
    

    
}
