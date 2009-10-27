/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.executables;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class Logging extends JavaExecutable {

    public static final String MSG = "LoG";
    public static final String RESULT = "ReSuLt";

    private int numberOfLines;
    private long sleepTime;
    private String stream;

    public Logging() {
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        if (this.stream.equals("out")) {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                System.out.println(MSG);
            }
        } else {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                System.out.println(MSG);
            }
        }

        return RESULT;
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        this.numberOfLines = Integer.parseInt((String) args.get("lines"));
        this.sleepTime = Long.parseLong((String) args.get("sleep"));
        this.stream = (String) args.get("stream");
    }

}
