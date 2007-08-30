package org.objectweb.proactive.core.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Provide an incremental per VM unique ID
 *
 * unique id starts from zero and is incremented each time getUniqID is called.
 * If Long.MAX_VALUE is reached then then IllegalStateArgument exception is thrown
 *
 * @See {@link ProActiveRandom}
 *
 */
public class ProActiveCounter {
    static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    static Object lock = new Object();
    static long counter = 0;
    static boolean loop = false;

    synchronized static public long getUniqID() {
        long ret;

        if (loop) {
            throw new IllegalStateException(ProActiveCounter.class.getSimpleName() +
                " counter reached max value");
        }

        synchronized (lock) {
            ret = counter;
            counter++;
        }

        if (ret == Long.MAX_VALUE) {
            loop = true;
        }

        return ret;
    }
}
