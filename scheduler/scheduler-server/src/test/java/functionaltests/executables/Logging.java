/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.executables;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
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
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);

        if (this.stream.equals("out")) {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                getOut().println(MSG);
            }
        } else {
            for (int i = 0; i < numberOfLines; i++) {
                Thread.sleep(this.sleepTime);
                getErr().println(MSG);
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
