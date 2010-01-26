/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.util.concurrent.atomic.AtomicReference;

import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * This class represents a holder of two instances of RMStatistics, two instances 
 * are used because an instance of this class can be potentially manipulated concurrently by
 * multiple threads. In fact, it is a specific case of the single writer and multiple readers
 * situation. The writer updates the statistical data of the first instance and the readers
 * access the statistical data of the second instance. Once the writer has finished 
 * updating it swaps atomically the two instances. The first once becomes the second and the 
 * second is reused for writing.
 * Using such a technique has the advantage of never blocking the writer, keeping the statistical
 * data consistent and avoid useless copies (if using copy-on write technique).  
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public final class AtomicRMStatisticsHolder {

    /**
     * A reference on statistics accessed only by the writer. 
     */
    private RMStatistics writeonlyStatistics;

    /**
     * Atomic reference on statistics accessed by the readers. 
     */
    private final AtomicReference<RMStatistics> readonlyStatistics;

    /**
     * Creates a new instance of this class. 
     */
    public AtomicRMStatisticsHolder() {
        this.writeonlyStatistics = new RMStatistics();
        this.readonlyStatistics = new AtomicReference<RMStatistics>(new RMStatistics());
    }

    /**
     * Transmits the incoming node events to the statistics.
     * @param event incoming event
     */
    public void nodeEvent(final RMNodeEvent event) {
        this.writeonlyStatistics.nodeEvent(event);
        this.swapAndUpdate();
    }

    /**
     * Transmits the incoming Resource Manager events to the statistics.
     * @param event incoming event
     */
    public void rmEvent(final RMEvent event) {
        this.writeonlyStatistics.rmEvent(event);
        this.swapAndUpdate();
    }

    /**
     * Atomically swaps in and out, then updates the out.
     */
    private void swapAndUpdate() {
        // First atomically replace read-only by write-only
        final RMStatistics oldReadonlyStatistics = this.readonlyStatistics
                .getAndSet(this.writeonlyStatistics);
        // reuse old and update its values
        this.writeonlyStatistics = oldReadonlyStatistics.updateFrom(this.writeonlyStatistics);
    }

    /**
     * Returns a consistent view on the statistics if and only if 
     * the caller of this method has called <code>startReadStatistics</code>.
     * @return the resource manager statistics
     */
    public RMStatistics getStatistics() {
        return this.readonlyStatistics.get();
    }
}