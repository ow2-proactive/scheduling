package modelisation.multiqueueserver;

import org.objectweb.proactive.Active;

public interface MultiQueueActive extends Active {

  public static String BODY_CLASS_NAME = "modelisation.multiqueueserver.MultiQueueBody";
  public static String PROXY_CLASS_NAME = org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME;
}
