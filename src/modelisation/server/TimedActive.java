package modelisation.server;

import org.objectweb.proactive.Active;

public interface TimedActive extends Active {

  public static String BODY_CLASS_NAME = "modelisation.server.TimedBodyWithServer";
  public static String PROXY_CLASS_NAME = org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME;
}
