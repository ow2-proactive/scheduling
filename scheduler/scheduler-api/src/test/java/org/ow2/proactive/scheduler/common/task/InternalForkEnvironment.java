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
package org.ow2.proactive.scheduler.common.task;

import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scripting.Script;


/**
 * InternalForkEnvironment is the internal view of a fork environment.
 * It adds a base environment on a fork one.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class InternalForkEnvironment extends ForkEnvironment {

    private boolean envReadOnly = false;

    /**
     * Create a new instance of InternalForkEnvironment using a fork environment and a base env.<br/>
     * This solution was the only one to decorate a fork environment according to those constraints :
     * <ul>
     * <li>ForkEnvironment type and API must be the same at user side and in the worker side (executed in the envscript)</li>
     * <li>No additional constructor is allowed in ForkEnvironment</li>
     * <li>We cannot use getter from ForkEnvironment as they don't match the real field type</li>
     * <li>It's not possible for the internal type and the user type to be in the same package as the internal type must be in internal package</li>
     * <li>We must "replay" user environment at worker side in the same order as he put it in ForkEnvironment at user side</li>
     * </ul>
     * <br/>
     * The goal of this class and constructor is just to have a way to create an object extending ForkEnvironment
     * and that is able to see protected field to re-set values. As it is not possible to see protected field
     * in a subclass out of the super class package, we create a protected constructor in the super class.<br/>
     * This class just use the superclass protected constructor.<br/>
     * <br/>
     * The result is an object extending ForkEnvironment, that represents the same object + a base env seen as the user ForkEnvironment<br/>
     * <br/>
     * Note : if baseEnv is null, an empty one is used.<br/>
     * <br/>
     *
     * @param forkEnv the user side fork environment, if null, an empty fork environment is used
     */
    public InternalForkEnvironment(ForkEnvironment forkEnv) throws ExecutableCreationException {
        this(forkEnv, false);
    }

    /**
     * Create a new instance of InternalForkEnvironment
     *
     * @param forkEnv the user side fork environment, if null, an empty fork environment is used
     * @param envReadOnly true if the system environment is read only, false if it can be modified.
     * 			if read only, methods that could modify system env will throw an UnsupportedOperationException
     */
    public InternalForkEnvironment(ForkEnvironment forkEnv, boolean envReadOnly) throws ExecutableCreationException {
        super(forkEnv);
        this.envReadOnly = envReadOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addSystemEnvironmentVariable(String name, String value) {
        if (envReadOnly) {
            throw new UnsupportedOperationException("System environment is read only, you cannot modify it.");
        }

        return super.addSystemEnvironmentVariable(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvScript(Script<?> script) {
        throw new UnsupportedOperationException("Environment script should not be modified in this context.");
    }

}
