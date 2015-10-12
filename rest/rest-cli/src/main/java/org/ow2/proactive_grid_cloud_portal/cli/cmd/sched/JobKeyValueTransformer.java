/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */



package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import com.google.common.collect.Maps;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

import java.util.Map;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;


public class JobKeyValueTransformer {
    private final static String USAGE=" The correct format is [ submit('workflow.xml','{\"var1\":\"value1\",\"var2\":\"value2\"}') ]";
    public Map<String, String> transformVariablesToMap(String jsonVariables ){
        Map<String, String> jobVariables = Maps.newHashMap();
        if (jsonVariables!=null){
            try {
               jobVariables = (JSONObject)new JSONParser().parse(jsonVariables);
                validateJSONVariables(jobVariables);
             } catch (ParseException | IllegalArgumentException e) {

                throw new CLIException(REASON_INVALID_ARGUMENTS, e.getMessage() + USAGE);
                //throw new CLIException(REASON_INVALID_ARGUMENTS,USAGE);
             }
        }
        return jobVariables;
    }

    private void validateJSONVariables(Map<String, String> jsonVariables ){
            if (jsonVariables.size() == 0) {
                throw new IllegalArgumentException("empty json ");
            }else {
                for (String key : jsonVariables.keySet()) {
                    if (key.length() == 0 ) {
                        throw new IllegalArgumentException("empty key ");
                    }
                    if (jsonVariables.get(key).length() == 0) {
                        throw new IllegalArgumentException("empty value ");
                    }
                }
            }
    }
}