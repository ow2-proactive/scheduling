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
package org.ow2.proactive.scheduler.synchronization;

import java.io.Serializable;


/**
 * Wrapper object used as result of {@link Synchronization#conditionalCompute(String, String, String, String) conditionalCompute} or {@link Synchronization#waitUntilThen(String, String, String, String) waitUntilThen} operations.
 *
 * Contains both the result of the predicate evaluation and action performed
 *
 * @author ActiveEon Team
 * @since 28/03/2018
 */
public final class PredicateActionResult implements Serializable {

    private boolean predicateResult;

    private Serializable actionResult;

    public PredicateActionResult(boolean predicateResult, Serializable actionResult) {
        this.predicateResult = predicateResult;
        this.actionResult = actionResult;
    }

    /**
     * Returns the result of the predicate evaluation
     * @return true if the predicate succeeded
     */
    public boolean isTrue() {
        return predicateResult;
    }

    /**
     * Returns the result of the conditional action
     * @return result of the action
     */
    public Serializable getActionResult() {
        return actionResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PredicateActionResult that = (PredicateActionResult) o;

        if (predicateResult != that.predicateResult)
            return false;
        return actionResult != null ? actionResult.equals(that.actionResult) : that.actionResult == null;
    }

    @Override
    public int hashCode() {
        int result = (predicateResult ? 1 : 0);
        result = 31 * result + (actionResult != null ? actionResult.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{ isTrue = " + predicateResult + ", actionResult = " + actionResult + " }";
    }
}
