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
package org.ow2.proactive.resourcemanager.node.jmx;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Ps;
import org.ow2.proactive.resourcemanager.utils.RRDSigarDataStore;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;


public class SigarProcesses implements SigarProcessesMXBean {

    /** Log4J logger */
    private final static Logger logger = Logger.getLogger(SigarProcesses.class);

    private String statBaseName;

    public SigarProcesses(String statBaseName) {
        this.statBaseName = statBaseName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProcessInfo[] getProcesses() throws SigarException {
        Sigar sigar = new Sigar();
        long[] pids = sigar.getProcList();

        List<ProcessInfo> result = new ArrayList<ProcessInfo>(pids.length);

        for (int i = 0; i < pids.length; i++) {
            long pid = pids[i];
            try {
                @SuppressWarnings("rawtypes")
                List info = Ps.getInfo(sigar, pid); // Add standard info. 
                info.add(sigar.getProcArgs(pid)); // Add also arguments of each process. 
                info.add(sigar.getProcCpu(pid).getPercent()); // Add cpu usage (perc.).

                result.add(new ProcessInfo(info));
            } catch (SigarException e) {
                // Ignore it, probably the process does not exist anymore.
                logger.warn("Could not get information for PID: " + pid, e);
            }

            // TODO see why sigar.getProcCpu(pid).getPercent()
            // returns '0.0' always.

        }

        return result.toArray(new ProcessInfo[] {});
    }

    @Override
    public String getAttributesHistory(String objectName, String[] attrs, String range) throws IOException {

        RrdDb db = new RrdDb(statBaseName, true);

        long timeEnd = db.getLastUpdateTime();
        // force float separator for JSON parsing
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        // formatting will greatly reduce response size
        DecimalFormat formatter = new DecimalFormat("###.###", otherSymbols);

        // construct the JSON response directly in a String
        StringBuilder result = new StringBuilder();
        result.append("{");

        for (int i = 0; i < attrs.length; i++) {

            String dataSource = RRDSigarDataStore.toDataStoreName(attrs[i] + "-" + objectName);

            char zone = range.charAt(0);
            long timeStart;

            switch (zone) {
                default:
                case 'a': // 1 minute
                    timeStart = timeEnd - 60;
                    break;
                case 'm': // 10 minute
                    timeStart = timeEnd - 60 * 10;
                    break;
                case 'h': // 1 hours
                    timeStart = timeEnd - 60 * 60;
                    break;
                case 'H': // 8 hours
                    timeStart = timeEnd - 60 * 60 * 8;
                    break;
                case 'd': // 1 day
                    timeStart = timeEnd - 60 * 60 * 24;
                    break;
                case 'w': // 1 week
                    timeStart = timeEnd - 60 * 60 * 24 * 7;
                    break;
                case 'M': // 1 month
                    timeStart = timeEnd - 60 * 60 * 24 * 28;
                    break;
                case 'y': // 1 year
                    timeStart = timeEnd - 60 * 60 * 24 * 365;
                    break;
            }

            FetchRequest req = db.createFetchRequest(ConsolFun.AVERAGE, timeStart, timeEnd);
            req.setFilter(dataSource);
            FetchData fetchData = req.fetchData();
            result.append("\"").append(dataSource).append("\":[");

            double[] values = fetchData.getValues(dataSource);
            for (int j = 0; j < values.length - 1; j++) {
                if (Double.compare(Double.NaN, values[j]) == 0) {
                    result.append("null");
                } else {
                    result.append(formatter.format(values[j]));
                }
                if (j < values.length - 2)
                    result.append(',');
            }
            result.append(']');
            if (i < attrs.length - 1)
                result.append(',');
        }
        result.append("}");

        db.close();

        return result.toString();
    }
}
