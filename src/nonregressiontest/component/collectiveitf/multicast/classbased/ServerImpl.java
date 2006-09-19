package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.Identifiable;
import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;


public class ServerImpl implements BroadcastServerItf, OneToOneServerItf, Identifiable {

    int id = 0;
    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#getID()
     */
    public String getID() {

        return new Integer(id).toString();
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#setID(java.lang.String)
     */
    public void setID(String id) {

        this.id = new Integer(id);
        
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.classbased.BroadcastServerItf#dispatch(java.util.List)
     */
    public WrappedInteger dispatch(List<WrappedInteger> l) {

        nonregressiontest.component.collectiveitf.multicast.ServerImpl s = new nonregressiontest.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testBroadcast_Param(l);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.classbased.OneToOneServerItf#dispatch(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger dispatch(WrappedInteger i) {

        nonregressiontest.component.collectiveitf.multicast.ServerImpl s = new nonregressiontest.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testOneToOne_Method(i);
    }


}
