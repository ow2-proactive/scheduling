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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;

public class StartIModeCommand extends AbstractCommand implements Command {

	private ScriptEngine engine;

	public StartIModeCommand() {
		ApplicationContext context = ApplicationContext.instance();
		if (context.getEngine() != null) {
			engine = context.getEngine();
		} else {

			ScriptEngineManager mgr = new ScriptEngineManager();
			engine = mgr.getEngineByExtension("js");
			engine.getContext().setWriter(
					applicationContext().getDevice().getWriter());
			context.setEngine(engine);
		}
		try {
			InputStream is = StartIModeCommand.class
					.getResourceAsStream("RestfulSchedulerActions.js");
			engine.eval(new InputStreamReader(is));
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute() throws Exception {
		while (!applicationContext().isTermiated()) {
			try {
				engine.eval(readLine("rest-cli>"));
			} catch (ScriptException se) {
				writeLine("An error occured while executing the script ..");
				Throwable cause = se.getCause();
				se.printStackTrace((PrintWriter) writer());
				writeLine("");
			}
		}
	}

}
