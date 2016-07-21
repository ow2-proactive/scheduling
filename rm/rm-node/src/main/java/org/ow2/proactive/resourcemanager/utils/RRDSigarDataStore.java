/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.log4j.Logger;
import org.ow2.proactive.jmx.RRDDataStore;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;


/**
 * This class (thread) dump all properties of Sigar MBean to RRD data base with specific period.
 */
public class RRDSigarDataStore extends RRDDataStore {

    private static final String[] OBJECT_NAMES = { "java.lang:type=OperatingSystem", "java.lang:type=Memory",
            "java.lang:type=Threading", "java.lang:type=ClassLoading", "sigar:Type=CpuCoreUsage,Name=*",
            "sigar:Type=FileSystem,Name=*", "sigar:Type=CpuUsage", "sigar:Type=Mem",
            "sigar:Type=NetInterface,Name=*", "sigar:Type=Swap" };

    private HashMap<String, String> compositeTypes = new HashMap<>();

    private MBeanServer mbs;

    /**
     * Initializes a new RRD data base if it's not exist.
     *
     * @param mbs            is the source of chronological data
     * @param dataBaseFilePath is the path to the file with the rrd data base
     * @param step             is the data base refresh period
     * @throws java.io.IOException is thrown when the data base exists but cannot be read
     */
    public RRDSigarDataStore(MBeanServer mbs, String dataBaseFilePath, int step, Logger logger)
            throws IOException, MalformedObjectNameException, IntrospectionException,
            InstanceNotFoundException, ReflectionException {
        super(dataBaseFilePath, step, logger);

        compositeTypes.put("HeapMemoryUsage-java.lang:type=Memory", "used");

        this.mbs = mbs;

        for (String objectName : OBJECT_NAMES) {
            Set<ObjectName> realNames = mbs.queryNames(new ObjectName(objectName), null);

            for (ObjectName realObjectName : realNames) {
                for (MBeanAttributeInfo attrInfo : mbs.getMBeanInfo(realObjectName).getAttributes()) {
                    if (attrInfo.isReadable()) {

                        String sourceName = attrInfo.getName() + "-" + realObjectName;
                        String dataStoreName = toDataStoreName(sourceName);

                        logger.trace("Adding a " + dataStoreName + " / " + sourceName + " source to rrd");
                        dataSources.put(dataStoreName, sourceName);
                    }
                }
            }
        }

        initDatabase();
        setName("RRD4J Data Store " + new File(dataBaseFile).getName());

        setDaemon(true);
        start();
    }

    /**
     * Shorten the name to 20 symbols trying to keep it meaningful
     */
    public static String toDataStoreName(String name) {
        String res = name.replace("-java.lang:type=", "");

        // to unify the following
        // Combined-sigar:Name=4,Type=CpuCoreUsage
        // Combined-sigar:Type=CpuCoreUsage,Name=4
        res = res.replace(",Type=CpuCoreUsage", "");
        res = res.replace("Type=CpuCoreUsage,", "");

        // RxBytes-sigar:Name=lo,Type=NetInterface / RxBytesName=lo,Type
        // RxBytes-sigar:Type=NetInterface,Name=eth0
        res = res.replace(",Type=NetInterface", "");
        res = res.replace("Type=NetInterface,", "");

        res = res.replace("-sigar:Type=", "");
        res = res.replace("-sigar:", "");

        if (res.length() > 20) {
            // only 20 symbols allowed in data source name
            res = res.substring(0, 19);
        }
        return res;
    }

    /**
     * Periodically dumps the new mbean state to the data base
     */
    public void run() {
        try {

            RrdDb dataBase = new RrdDb(dataBaseFile);

            logger.debug("RRD database configuration:\n" + dataBase.getRrdDef().dump());

            while (!terminate) {
                synchronized (dataSources) {

                    dataSources.wait(step * 1000);

                    if (terminate) {
                        break;
                    }
                    sample(dataBase, System.currentTimeMillis());
                }
            }
            dataBase.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    // package protected for testing
    void sample(RrdDb dataBase, long timeInMs) throws IOException {
        Sample sample = dataBase.createSample();

        // updating the data base
        for (String dataSource : dataSources.keySet()) {
            String fullName = dataSources.get(dataSource);
            String[] names = fullName.split("-");
            String attrName = names[0];
            String objectName = names[1];

            try {
                Object attrValue = mbs.getAttribute(new ObjectName(objectName), attrName);

                if (attrValue instanceof CompositeDataSupport && compositeTypes.get(fullName) != null) {
                    Object val = ((CompositeDataSupport) attrValue).get(compositeTypes.get(fullName));
                    if (val != null) {
                        attrValue = val;
                    }
                }
                sample.setValue(dataSource, Double.parseDouble(attrValue.toString()));
                if (logger.isTraceEnabled()) {
                    logger.trace(timeInMs / 1000 + " sampling: " + dataSource + " / " + fullName + " " +
                        Double.parseDouble(attrValue.toString()));
                }
            } catch (NumberFormatException ex) {
                // do not save non-numeric values
                if (logger.isTraceEnabled()) {
                    logger.trace("Non numeric value for " + dataSource + " / " + fullName + ": " +
                        ex.getMessage());
                }
            } catch (InstanceNotFoundException | AttributeNotFoundException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cannot read attribute " + attrName + " for object " + objectName + ": " +
                        e.getMessage());
                }

            } catch (Exception e) {
                logger.warn("Error while reading attribute " + attrName + " for object " + objectName + ": ",
                        e);

            }
        }
        try {
            sample.setTime(timeInMs / 1000);
            sample.update();
        } catch (Exception e) {
            logger.error("Cannot update RRD database: " + e.getMessage());
        }
    }

}
