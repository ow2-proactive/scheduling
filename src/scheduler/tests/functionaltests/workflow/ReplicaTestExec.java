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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class ReplicaTestExec extends JavaExecutable {

	private String foo;
	private String bar;

	public void init(Map<String, Serializable> args) throws Exception {
		this.foo = args.get("foo").toString();
		this.bar = args.get("bar").toString();

	}

	@Override
	public Serializable execute(TaskResult... results) throws Throwable {
		int i;

		for (String name : new String[] { foo, bar }) {
			DataSpacesFileObject inf = getLocalFile(name + ".in");
			DataSpacesFileObject outf = getLocalFile(name + ".out");
			outf.createFile();
			InputStream in = inf.getContent().getInputStream();
			OutputStream out = outf.getContent().getOutputStream();
			while ((i = in.read()) != -1) {
				out.write(i);
			}
			out.write(new String(" " + System.getProperty("foo") + " "
					+ System.getProperty("bar") + "\n").getBytes());

			out.close();
		}

		System.out.println("hello " + getIterationIndex() + " "
				+ getReplicationIndex());

		return "result " + getIterationIndex() + " " + getReplicationIndex();
	}

}
