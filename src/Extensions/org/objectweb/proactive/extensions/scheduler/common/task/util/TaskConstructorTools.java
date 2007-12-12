/**
 *
 */
package org.objectweb.proactive.extensions.scheduler.common.task.util;


/**
 * TaskConstructorTools is used to know if a user executable task (java or ProActive)
 * contains a no parameter constructor. If it is not the case, the executable cannot be launched.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Dec 12, 2007
 * @since ProActive 3.9
 *
 */
public class TaskConstructorTools {

    /**
     * Return true if the given class contains a no parameter constructor.
     *
     * @param cla the class to check.
     * @return true if the given class contains a no parameter constructor, false if not.
     */
    public static boolean hasEmptyConstructor(Class<?> cla) {
        try {
            cla.getDeclaredConstructor(new Class<?>[] {  });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
