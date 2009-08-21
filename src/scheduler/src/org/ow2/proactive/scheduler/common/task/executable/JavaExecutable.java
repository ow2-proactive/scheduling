/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.JavaExecutableInitializer;
import org.ow2.proactive.utils.NodeSet;


/**
 * Extends this abstract class if you want to create your own java task.<br>
 * A java task is a task representing a java process as a java class.<br>
 * This class provides an {@link #init(Map)} that will get your parameters back for this task.
 * By default, this method does nothing.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class JavaExecutable extends Executable {

    private JavaExecutableInitializer execInitializer;

    /**
     * Initialize the executable using the given executable Initializer.
     *
     * @param execContainer the executable Initializer used to init the executable itself
     *
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    // WARNING WHEN REMOVE OR RENAME, called by task launcher by introspection
    private void internalInit(JavaExecutableInitializer execInitializer) throws Exception {
        this.execInitializer = execInitializer;
        init(execInitializer.getArguments());
    }

    /**
     * Initialization default method for a java task.<br>
     * <p>
     * By default, this method try so do some kind of automatic assignment between the value given
     * in the args map and the field contains in your executable.<br>
     * Manage types are String, byte, short, int, long, boolean and the corresponding classes.<br><br>
     * For example, (when making your task) if you set as arguments the key="var", value="12"<br>
     * just add an int (or Integer, long, Long) field named "var" in your executable.
     * The default {@link #init(Map)} method will store your arguments into the integer class field.
     * </p>
     * To avoid this default behavior, just override this method to make your own initialization.
     *
     * @param args a map containing the different parameter names and values given by the user task.
     */
    public void init(Map<String, String> args) throws Exception {
        if (args == null) {
            return;
        }
        for (Entry<String, String> e : args.entrySet()) {
            try {
                Field f = this.getClass().getDeclaredField(e.getKey());
                f.setAccessible(true);
                Class<?> klass = f.getType();
                if (klass.equals(String.class)) {
                    f.set(this, e.getValue());
                } else if (klass.equals(Integer.class) || klass.equals(int.class)) {
                    f.set(this, Integer.parseInt(e.getValue()));
                } else if (klass.equals(Short.class) || klass.equals(short.class)) {
                    f.set(this, Short.parseShort(e.getValue()));
                } else if (klass.equals(Long.class) || klass.equals(long.class)) {
                    f.set(this, Long.parseLong(e.getValue()));
                } else if (klass.equals(Byte.class) || klass.equals(byte.class)) {
                    f.set(this, Byte.parseByte(e.getValue()));
                } else if (klass.equals(Boolean.class) || klass.equals(boolean.class)) {
                    f.set(this, Boolean.parseBoolean(e.getValue()));
                }
            } catch (Exception ex) {
                //field not set
            }
        }
    }

    /**
     * Use this method for a multi-node task. It returns the list of nodes demanded by the user
     * while describing the task.<br>
     * In a task, one node is used to start the task itself, the other are returned by this method.<br>
     * If user describe the task using the "numberOfNodes" property set to 5, then this method
     * returns a list containing 4 nodes. The first one being used by the task itself.
     *
     * @return the list of nodes demanded by the user.
     */
    public NodeSet getNodes() {
        return execInitializer.getNodes();
    }

}
