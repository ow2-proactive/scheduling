package modelisation.mixed;

import modelisation.forwarder.DummyObject;

import modelisation.server.AgentWithExponentialMigrationAndServer;

import modelisation.statistics.RandomNumberFactory;

import org.objectweb.proactive.core.node.Node;


public class AgentWithExponentialMigrationMixed
    extends AgentWithExponentialMigrationAndServer
    implements org.objectweb.proactive.RunActive, java.io.Serializable {
    public AgentWithExponentialMigrationMixed() {
        super();
    }

    public AgentWithExponentialMigrationMixed(Double nu, Node[] array,
        Long lifeTime) throws IllegalArgumentException {
        //  this.expo = new ExponentialLaw(nu.doubleValue());
        this.expo = RandomNumberFactory.getGenerator("nu");
        this.expo.initialize(nu.doubleValue());

        hosts = array;
        System.out.println(
            "AgentWithExponentialMigrationMixed: array contains " +
            array.length + " destinations");
        for (int i = 0; i < array.length; i++)
            System.out.println("destination " + i + " = " + array[i]);
        index = 0;
        this.lifeTime = lifeTime.longValue();
    }

    public DummyObject echoObject() {
        // System.out.println("I am here");
        return null;
    }

    //
    //    public static void main(String[] args) {
    //        try {
    //
    //            AgentWithExponentialMigrationMixed agent = (AgentWithExponentialMigrationMixed)ProActive.newActive(
    //                                                               AgentWithExponentialMigrationMixed.class, 
    //                                                                null, (Node)null, 
    //                                                               (Active)null, 
    //                                                               TimedMixedMetaObjectFactory.newInstance());
    //                                                               
    //              
    //    Object[] constructorParameters,
    //    Node node,
    //    Active activity,
    //    MetaObjectFactory factory)
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
}
