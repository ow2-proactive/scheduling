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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.utils;

import java.util.Map;

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

    private static final long serialVersionUID = 61L;

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
     * @param baseEnv a base environment on which the user on will be computed
     */
    public InternalForkEnvironment(ForkEnvironment forkEnv, Map<String, String> baseEnv) {
        this(forkEnv, baseEnv, false);
    }

    /**
     * Create a new instance of InternalForkEnvironment
     *
     * @param forkEnv the user side fork environment, if null, an empty fork environment is used
     * @param baseEnv a base environment on which the user on will be computed
     * @param envReadOnly true if the system environment is read only, false if it can be modified.
     * 			if read only, methods that could modify system env will throw an UnsupportedOperationException
     */
    public InternalForkEnvironment(ForkEnvironment forkEnv, Map<String, String> baseEnv, boolean envReadOnly) {
        super(forkEnv, baseEnv);
        this.envReadOnly = envReadOnly;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSystemEnvironmentVariable(String name, String value, boolean append) {
        if (envReadOnly) {
            throw new UnsupportedOperationException("System environment is read only, you cannot modify it.");
        }
        super.addSystemEnvironmentVariable(name, value, append);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSystemEnvironmentVariable(String name, String value, char appendChar) {
        if (envReadOnly) {
            throw new UnsupportedOperationException("System environment is read only, you cannot modify it.");
        }
        super.addSystemEnvironmentVariable(name, value, appendChar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnvScript(Script<?> script) {
        throw new UnsupportedOperationException("Environment script should not be modified in this context.");
    }
}
