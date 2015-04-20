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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * This class implements a wrapper for user defined BatchJobInfrastructure. You must provide
 * a class file and classname of a class that implements a {@link BatchJobInfrastructure}.
 */
public class GenericBatchJobInfrastructure extends BatchJobInfrastructure {

    private static final long serialVersionUID = 62L;

    @Configurable(description = "Fully qualified classname\nof the implementation")
    protected String implementationClassname;

    @Configurable(fileBrowser = true, description = "Absolute path to the\nclass file of the implementation")
    protected String implementationFile;

    // the actual implementation of the infrastructure
    private BatchJobInfrastructure implementation;

    @Override
    public void configure(Object... parameters) {
        super.configure(parameters);
        this.implementationClassname = parameters[9].toString();
        byte[] implemtationClassfile = (byte[]) parameters[10];

        // read the class file and create a BatchJobInfrastructure instance
        try {

            //create dir for tmp classpath
            File f = File.createTempFile("BatchJobClassDir", "GENERATED");
            f.delete();
            f.mkdir();
            f.deleteOnExit();

            // if the class name contains the ".class", remove it
            if (this.implementationClassname.endsWith(".class")) {
                this.implementationClassname = this.implementationClassname.substring(0,
                        this.implementationClassname.lastIndexOf("."));
            }
            int lastIndexOfDot = this.implementationClassname.lastIndexOf(".");
            boolean inPackage = lastIndexOfDot != -1;
            StringBuffer currentDirName = new StringBuffer(f.getAbsolutePath());

            // create hierarchy for class file
            if (inPackage) {
                StringTokenizer packages = new StringTokenizer(this.implementationClassname.substring(0,
                        lastIndexOfDot), ".");
                while (packages.hasMoreTokens()) {
                    currentDirName.append(File.separator + packages.nextToken());
                    File currentDir = new File(currentDirName.toString());
                    currentDir.mkdir();
                    currentDir.deleteOnExit();
                }
            }
            //create the classfile
            File classFile = new File(currentDirName +
                File.separator +
                this.implementationClassname.substring(lastIndexOfDot + 1, this.implementationClassname
                        .length()) + ".class");
            classFile.deleteOnExit();
            if (logger.isDebugEnabled()) {
                logger.debug("Created class file for generic BatchJobInfrastructure : " +
                    classFile.getAbsolutePath());
            }
            FileToBytesConverter.convertByteArrayToFile(implemtationClassfile, classFile);
            URLClassLoader cl = new URLClassLoader(new URL[] { f.toURL() }, this.getClass().getClassLoader());
            Class<? extends BatchJobInfrastructure> implementationClass = (Class<? extends BatchJobInfrastructure>) cl
                    .loadClass(this.implementationClassname);
            this.implementation = implementationClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + this.implementationClassname + " does not exist", e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Implementation class file does not exist", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class " + this.implementationClassname + " cannot be loaded",
                e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + this.implementationClassname + " cannot be loaded",
                e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create temp file for class " +
                this.implementationClassname, e);
        }

    }

    @Override
    protected String extractSubmitOutput(String output) {
        return implementation.extractSubmitOutput(output);
    }

    @Override
    protected String getBatchinJobSystemName() {
        if (implementation != null) {
            return implementation.getBatchinJobSystemName();
        } else {
            return "GENERIC";
        }
    }

    @Override
    protected String getDeleteJobCommand() {
        return implementation.getDeleteJobCommand();
    }

    @Override
    protected String getSubmitJobCommand() {
        return implementation.getSubmitJobCommand();
    }

}
