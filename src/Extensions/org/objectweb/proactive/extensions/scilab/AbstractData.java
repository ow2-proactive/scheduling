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
package org.objectweb.proactive.extensions.scilab;

import java.io.Serializable;

import javasci.SciData;

import org.objectweb.proactive.annotation.PublicAPI;

import ptolemy.data.Token;


/**
 * This class encapsulate either a ptolemy token (Matlab interface to java) or a Scilab SciData
 * @author The ProActive Team
 *
 */
@PublicAPI
public class AbstractData implements Serializable {

    /**
     *
     */
    private SciData sciData;
    private String matName;
    private Token matData;

    private enum data_type {
        SCILAB_DATA, MATLAB_DATA;
    };

    private data_type type;

    /**
     * Build an AbstractData with a Scilab data type
     * @param data scilab data type
     */
    public AbstractData(SciData data) {
        this.sciData = data;
        this.type = data_type.SCILAB_DATA;
    }

    /**
     * Build an AbstractData with a ptolemy Matlab data type
     * @param name name of data
     * @param data actual data
     */
    public AbstractData(String name, Token data) {
        this.matName = name;
        this.matData = data;
        this.type = data_type.MATLAB_DATA;
    }

    /**
     * Retrieve the name of this data
     * @return name
     */
    public String getName() {
        switch (type) {
            case SCILAB_DATA:
                return sciData.getName();
            case MATLAB_DATA:
                return matName;
        }

        return null;
    }

    /**
     * Retrieve the data object itself (need to be cast into appropriate type)
     * @return data object
     */
    public Object getData() {
        switch (type) {
            case SCILAB_DATA:
                return sciData;
            case MATLAB_DATA:
                return matData;
        }

        return null;
    }

    @Override
    public String toString() {
        switch (type) {
            case SCILAB_DATA:
                return sciData.toString();
            case MATLAB_DATA:
                return matData.toString();
        }

        return null;
    }
}
