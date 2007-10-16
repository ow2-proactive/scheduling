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
package org.objectweb.proactive.benchmarks.timit.util;


/**
 * This class represent parameters for one benchmark test. These parameters are
 * set in the configuration file.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk, Judicael Ribault
 */
public class Parameter {
    public String className;
    public String[] args;
    public int run;
    public String outputFile;
    public String note;
    public String name;
    public XMLHelper xhp;
    public String proActiveDescriptor;
    public long timeout;
    public String[] jvmArgs;
    public int warmUpRuns;

    /**
     * Create an instance of Paramater for a benchmark test
     *
     * @param xhp
     *            a reference to the XMLHelper instance
     * @param name
     *            the name of the benchmark test
     * @param className
     *            the full class name of the application to benchmark
     * @param args
     *            the arguments list to pass to application
     * @param run
     *            how many time you want to run the application
     * @param outputFile
     *            the output filename for XML results
     * @param note
     *            specific information to add for this benchmark test
     * @param proActiveDescriptor
     *            the ProActive deployement descriptor filename
     * @param timeout
     *            timeout in seconds for this test
     * @param jvmArgs
     *            Arguments passed to create a jvm
     */
    public Parameter(XMLHelper xhp, String name, String className,
        String[] args, int run, String outputFile, String note,
        String proActiveDescriptor, long timeout, String[] jvmArgs,
        int warmUpRuns) {
        this.xhp = xhp;
        this.name = name;
        this.className = className;
        this.args = args.clone();
        this.run = run;
        this.outputFile = outputFile;
        this.note = note;
        this.proActiveDescriptor = proActiveDescriptor;
        this.timeout = timeout;
        this.jvmArgs = jvmArgs.clone();
        this.warmUpRuns = warmUpRuns;
    }
}
