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

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.cli.HelpFormatter;
import org.ow2.proactive_grid_cloud_portal.cli.Main;

public class HelpCommand extends AbstractCommand implements Command {

    private static final String USAGE = "rest-cli [-u <server-url>] "
            + "[-k | -ca <store-path>  [-cap <store-pass>]] "
            + "[-l <login-name> [-p <password>] | -c <cerd-file-path>] "
            + "[-start | -stop | -pause | -resume | -freeze | -kill | -lj | -stats "
            + "| -s | -sa | -js | -jo | -jr | -sj | -pj | -rj | -rmj "
            + "| -to | -tr | -pt | -rt | -h | -sf | -i]";

    public HelpCommand() {
    }

    @Override
    public void execute() throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        Writer writer = context().getDevice().getWriter();
        PrintWriter pw = new PrintWriter(writer, true);
        formatter.printHelp(pw, 110, USAGE, "", Main.options(),
                formatter.getLeftPadding(), formatter.getDescPadding(), "",
                false);
    }
}
