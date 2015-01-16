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
package org.ow2.proactive.scheduler.task.forked;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.newimpl.TaskLauncher;
import org.ow2.proactive.scheduler.task.java.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scheduler.util.process.ThreadReader;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 *
 * @author The ProActive Team
 *
 */
public class JavaForkerExecutable extends JavaExecutable implements ForkerStarterCallback {


    private PrintStream outputSink;
    private JavaExecutableContainer container;

    public JavaForkerExecutable(TaskLauncher stubOnThis) {
        super();
    }

    private void internalInit(ForkedJavaExecutableInitializer execInitializer) throws Exception {
        container = execInitializer.getJavaExecutableContainer();
        outputSink = execInitializer.getOutputSink();

    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        System.out.println("I'm forking");

        OSProcessBuilder builder = ForkerUtils.getOSProcessBuilderFactory("").getBuilder();
        OSProcessBuilder pb = builder.command("java", "-cp", "/home/ybonnaffe/src/scheduling/dist/lib/*",
          "org.ow2.proactive.scheduler.task.forked.Test");
        // TODO fork and run something

        File f = new File( "/tmp/shm" );

        FileChannel channel = FileChannel.open( f.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE );

        MappedByteBuffer b = channel.map( FileChannel.MapMode.READ_WRITE, 0, 4096 );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.writeObject(container);
        objectOutputStream.close();

        b.put(out.toByteArray());

        Process process = pb.start();

        BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Thread tsout = new Thread(new ThreadReader(sout, outputSink)); // not sysout but outputstream
        tsout.start();

        process.waitFor();
        return 0;
    }

    @Override
    public void callback(Node n) {

    }
}
