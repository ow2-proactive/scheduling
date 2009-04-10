/**
 *
 */
package org.ow2.proactive.scheduler.util.logforwarder;

import java.io.Serializable;

import org.apache.log4j.Appender;


/**
 * @author cdelbe
 *
 */
public interface AppenderProvider extends Serializable {

    public Appender getAppender();

}
