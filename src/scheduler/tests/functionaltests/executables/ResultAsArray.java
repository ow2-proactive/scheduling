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
import java.util.ArrayList;
import java.util.Arrays;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class ResultAsArray extends JavaExecutable {

    private int size = 10;

    //	public ResultAsArray(){}

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        final ArrayList<RenderedBucket> renderedBucketList = new ArrayList<RenderedBucket>(size);
        for (int i = 0; i < size; i++) {
            float[] tab = new float[3 * 32 * 32]; // 3072 color component test
            Arrays.fill(tab, (float) Math.random());
            renderedBucketList.add(new RenderedBucket(i * 1, i * 2, i * 3, i * 4, tab));
        }
        return renderedBucketList;
    }

    static class RenderedBucket implements Serializable {
        private int x, y, w, h;
        private float[] color;

        public RenderedBucket(int x, int y, int w, int h, float[] color) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = color;
        }

        @Override
        public String toString() {
            return "" + x + y + w + h;
        }
    }
}