/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scripting;

import java.io.FileReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * The script Loade
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
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
     * Creates a local script handler: not an AO
     * 
     * @return a ScriptHandler for Script execution
     */
    public static ScriptHandler createLocalHandler() {
        return new ScriptHandler();
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
