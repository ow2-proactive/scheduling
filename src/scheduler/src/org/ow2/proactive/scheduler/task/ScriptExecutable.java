/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.TaskScript;
import org.apache.log4j.Logger;


/**
 * This is the execution entry point for the script task.
 * The execute(TaskResult...) method will be override by the scheduler to launch the script engine.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
public class ScriptExecutable extends Executable {

    public static final Logger logger = Logger.getLogger(ScriptExecutable.class);

    private TaskScript script;
    private Map<String, DataSpacesFileObject> dataspaceBindings;
    /** execution progress value (between 0 and 100), can be updated by the script */
    private final AtomicInteger progress = new AtomicInteger(0);

    /**
     * Initialize the executable using the given executable container.
     *
     * @param execInitializer the executable initializer used to init the executable itself
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    // WARNING WHEN REMOVE OR RENAME, called by task launcher by introspection
    private void internalInit(ScriptExecutableInitializer execInitializer) throws Exception {
        script = execInitializer.getScript();
        dataspaceBindings = execInitializer.getDataspaceBindings();
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        logger.info("Executing script");
        ScriptHandler handler = ScriptLoader.createLocalHandler();
        for (Map.Entry<String, DataSpacesFileObject> dataspaceEntry : dataspaceBindings.entrySet()) {
            handler.addBinding(dataspaceEntry.getKey(), dataspaceEntry.getValue());
        }
        handler.addBinding(TaskScript.RESULTS_VARIABLE, results);
        handler.addBinding(TaskScript.PROGRESS_VARIABLE, progress);
        ScriptResult<Serializable> scriptResult = handler.handle(script);

        if (scriptResult.errorOccured()) {
            scriptResult.getException().printStackTrace();
            logger.error("Error on script occured : ", scriptResult.getException());
            throw new UserException("Script has failed on the current node", scriptResult.getException());
        }

        return scriptResult.getResult();
    }

    @Override
    public int getProgress() {
        return progress.get();
    }
}
