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
 * %$ACTIVEEON_INITIAL_DEV$
 */

package org.ow2.proactive_grid_cloud_portal.cli.utils;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

public class StringUtility {

    public static boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }

    public static String string(ObjectArrayFormatter oaf) {
        return Tools.getStringAsArray(oaf);
    }

    public static String formattedDate(long time) {
        return Tools.getFormattedDate(time);
    }

    public static String formattedElapsedTime(long time) {
        return Tools.getElapsedTime(time);
    }

    public static String formattedDuration(long start, long end) {
        return Tools.getFormattedDuration(start, end);
    }

    public static String string(HttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (ParseException pe) {
            throw new CLIException(REASON_OTHER, pe);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    private StringUtility() {
    }

}
