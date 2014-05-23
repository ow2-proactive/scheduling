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
package org.ow2.proactive.jmx;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.StandardMBean;

import org.apache.log4j.Logger;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;


/**
 * This class (thread) dump all properties of MBean to RRD data base with specific period.
 */
public class RRDDataStore extends Thread {

    private StandardMBean mbean;
    protected int step = 4; //secs
    protected String dataBaseFile;
    protected HashMap<String, String> dataSources = new HashMap<String, String>();
    protected volatile boolean terminate = false;
    protected Logger logger;

    protected RRDDataStore(String dataBaseFilePath, int step, Logger logger) {
        this.step = step;
        this.dataBaseFile = dataBaseFilePath;
        this.logger = logger;
    }

    /**
     * Initializes a new RRD data base if it's not exist.
     *
     * @param mbean is the source of chronological data
     * @param dataBaseFilePath is the path to the file with the rrd data base
     * @param step is the data base refresh period
     * @throws IOException is thrown when the data base exists but cannot be read
     */
    public RRDDataStore(StandardMBean mbean, String dataBaseFilePath, int step, Logger logger)
            throws IOException {

        this(dataBaseFilePath, step, logger);
        this.mbean = mbean;

        for (MBeanAttributeInfo attrInfo : mbean.getMBeanInfo().getAttributes()) {
            try {
                if (attrInfo.isReadable() &&
                    mbean.getClass().getMethod("get" + attrInfo.getName()).getAnnotation(Chronological.class) != null) {

                    String sourceName = attrInfo.getName();
                    if (sourceName.length() > 20) {
                        // only 20 symbols allowed in data source name
                        sourceName = sourceName.substring(0, 19);
                    }
                    dataSources.put(sourceName, attrInfo.getName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        initDatabase();

        setName("RRD4J Data Store " + new File(dataBaseFilePath).getName());
        setDaemon(true);
        start();
    }

    protected void initDatabase() throws IOException {
        if (!new File(dataBaseFile).exists()) {
            logger.info("Creating a new RRD data base: " + dataBaseFile);

            RrdDef rrdDef = new RrdDef(dataBaseFile, System.currentTimeMillis() / 1000, step);

            for (String dataSource : dataSources.keySet()) {
                rrdDef.addDatasource(dataSource, DsType.GAUGE, 600, 0, Double.NaN);
            }

            // for step equals 4 seconds
            // Archive of 10 minutes = 600 seconds (4 * 1 * 150) of completely detailed data
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 150);

            // An archive of 1 hour = 3600 seconds (4 * 5 * 180) i.e. 180 averages of 5 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 5, 180);

            // An archive of 4 hours = 14400 seconds (4 * 10 * 360) i.e. 360 averages of 10 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 360);

            // An archive of 8 hours = 28800 seconds (4 * 20 * 360) i.e. 360 averages of 20 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 20, 360);

            // An archive of 24 hours = 86400 seconds (4 * 30 * 720) i.e. 720 averages of 30 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 30, 720);

            // An archive of 1 week = 604800 seconds (4 * 210 * 720) i.e. 720 averages of 210 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 210, 720);

            // An archive of 1 month ~= 28 days = 604800 seconds (4 * 840 * 720) i.e. 720 averages of 840 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 840, 720);

            // An archive of 1 year = 364 days = 31449600 seconds (4 * 10920 * 720) i.e. 720 averages of 10920 steps
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10920, 720);

            RrdDb dataBase = new RrdDb(rrdDef);
            dataBase.close();
        } else {
            logger.info("Using existing RRD database: " + new File(dataBaseFile).getAbsolutePath());
        }
    }

    /**
     * Periodically dumps the new mbean state to the data base
     */
    public void run() {
        try {
            RrdDb dataBase = new RrdDb(dataBaseFile);
            Sample sample = dataBase.createSample();

            logger.debug("RRD data base configuration:\n" + dataBase.getRrdDef().dump());

            while (!terminate) {
                try {
                    synchronized (dataSources) {

                        dataSources.wait(step * 1000);

                        if (terminate) {
                            break;
                        }

                        // updating the data base
                        for (String dataSource : dataSources.keySet()) {
                            Object attrValue = mbean.getAttribute(dataSources.get(dataSource));
                            sample.setValue(dataSource, Double.parseDouble(attrValue.toString()));
                            logger.debug(System.currentTimeMillis() / 1000 + " sampling: " + dataSource +
                                " " + Double.parseDouble(attrValue.toString()));
                        }

                        sample.setTime(System.currentTimeMillis() / 1000);
                        sample.update();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            dataBase.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Converts the data base into the bytes array in order to send it to a client.
     *
     * @return bytes array
     * @throws IOException when file cannot be read
     */
    public byte[] getBytes() throws IOException {
        synchronized (dataSources) {
            return FileToBytesConverter.convertFileToByteArray(new File(dataBaseFile.toString()));
        }
    }

    /**
     * Terminates the thread activity.
     */
    public void terminate() {
        synchronized (dataSources) {
            terminate = true;
            dataSources.notifyAll();
        }
    }

}
