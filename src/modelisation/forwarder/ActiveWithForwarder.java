package modelisation.forwarder;

import org.objectweb.proactive.Active;

public interface ActiveWithForwarder extends Active {

    public static String BODY_CLASS_NAME = "modelisation.forwarder.TimedBody";
    //public static String BODY_CLASS_NAME = org.objectweb.proactive.core.Constants.DEFAULT_BODY_CLASS_NAME;
    public static String PROXY_CLASS_NAME = org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME;

}
