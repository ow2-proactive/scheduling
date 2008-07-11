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
 */
package org.ow2.proactive.resourcemanager.common.scripting;

import java.io.FileReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * The script Loade
 *
 *
 * @author The ProActive Team
 * @version 1.0, Jun 6, 2007
 * @since ProActive 3.2
 */
public class ScriptLoader {

    /** Create a script handler active object that performs a script execution
     * @param node ProActive node on which the handler must be created.
     * @return a ScriptHandler stub (reference to a remote object).
     * @throws ActiveObjectCreationException if script AO creation fails
     * @throws NodeException if an error occurs on the target node (node unreachable...).
     */
    public static ScriptHandler createHandler(Node node) throws ActiveObjectCreationException, NodeException {
        return (ScriptHandler) PAActiveObject.newActive(ScriptHandler.class.getCanonicalName(), null, node);
    }

    /**
     * Main function
     * @param args command arguments.
     * @throws Exception if fails. 
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage : scriptloader script");
            System.exit(1);
        }

        String filename = args[0];
        String[] split = filename.split("\\.");

        if (split.length < 2) {
            System.err.println("Script must have an extension");
            System.exit(-2);
        }

        Reader reader = new FileReader(args[0]);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByExtension(split[split.length - 1]);
        engine.eval(reader);
    }
}
