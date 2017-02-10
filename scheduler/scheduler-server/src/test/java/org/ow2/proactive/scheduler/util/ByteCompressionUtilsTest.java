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
package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;


public class ByteCompressionUtilsTest {

    private TaskFlowJob job;

    private byte[] jobByte;

    @Before
    public void setup() throws UserException {
        job = new TaskFlowJob();
        job.setName(this.getClass().getName());
        job.addTask(new JavaTask());

        jobByte = SerializationUtils.serialize(job);
    }

    @Test
    public void test() throws IOException, DataFormatException {
        byte[] compressed = ByteCompressionUtils.compress(jobByte);
        System.out.println("compressed lenght : " + jobByte.length);
        System.out.println("compressed lenght : " + compressed.length);
        assertThat(compressed.length < jobByte.length, is(true));
        byte[] decompressed = ByteCompressionUtils.decompress(compressed);
        System.out.println("compressed lenght : " + compressed.length);
        System.out.println("decompressed lenght : " + decompressed.length);
        assertThat(decompressed.length == jobByte.length, is(true));
    }

}
