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
package org.objectweb.proactive.benchmarks.timit.util.observing.commobserv;

import org.objectweb.proactive.benchmarks.timit.util.observing.EventData;


/**
 * This class implements the StatData interface. Part of the specialized
 * Observer/Observable pattern.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 */
public class CommEventData implements EventData {

    /**
     *
     */
    private String name;
    private int subjectRank;
    private int[] markedCommunication;
    private int[][] allValues;

    /**
     * Creates an instance of CommEventData.
     *
     * @param name
     *            The name of the event that will appear in the generated xml
     *            file
     * @param groupSize
     *            The size of the group used to create the communication pattern
     * @param subjectRank
     *            The rank in the group of the subject object ie the Observable
     * @param matrixChartParameters
     *            The parameters used to create the chart
     */
    public CommEventData(String name, int groupSize, int subjectRank) {
        this.name = name;
        this.markedCommunication = new int[groupSize];
        this.subjectRank = subjectRank;
    }

    /**
     * Part of the CommEvent implementation, in this case the data is an array
     * of marked communication.
     *
     * @param object
     *            The array of marked communication
     */
    public void setData(Object object) {
        this.markedCommunication = (int[]) object;
    }

    /**
     * A getter for the name of this event data.
     *
     * @return The name of the event data
     */
    public String getName() {
        return this.name;
    }

    /**
     * Part of the CommEvent implementation, in this case the data is the array
     * of marked communication.
     *
     * @return An array of int ie the marked communications array
     */
    public Object getData() {
        return this.markedCommunication.clone();
    }

    /**
     * Part of the CommEvent implementation, in this case the collapse operation
     * (performed when all CommEventDatas have been gathered) will be applied
     * between the first CommEventData and iteratively with the others.<br>
     * Suppose that there is n gathered instances of CommEventData. The collapse
     * operation will be applied like : <br>
     * <li>first.collapseWith(second) then first.collapseWith(third) ...
     * first.collapseWith(nth)</li>
     * <br>
     * <br>
     * In this case the collapse operation is a simple concatenation of n 1-dim
     * arrays.<br>
     * The result will be a square 2-dim array.
     *
     * @param anotherData
     *            Another data to merge with.
     * @param anotherRank
     *            The communication pattern creation needs the data rank
     * @return The collapsed value
     */
    public Object collapseWith(EventData anotherData, int anotherRank) {
        int length = this.markedCommunication.length;
        if (this.allValues == null) {
            this.allValues = new int[length][length];
            System.arraycopy(this.markedCommunication, 0, this.allValues[this.subjectRank], 0, length);
        }
        if (anotherData.equals(this)) {
            return this.allValues.clone();
        }

        int[] anotherCommArray = (int[]) anotherData.getData();

        for (int i = 0; i < length; i++) {
            this.allValues[anotherRank][i] += anotherCommArray[i];
        }

        return this.allValues.clone();
    }

    /**
     * Marks the communication specified by a target rank and a value.
     *
     * @param targetRank
     *            The rank of the target
     * @param value
     *            The communicated value (size or number of comms)
     */
    public void mark(int targetRank, double value) {
        this.markedCommunication[targetRank] += (int) value;
    }

    /**
     * Returns the 2-dim array of collapsed datas.
     *
     * @return The result of all collapsed datas
     */
    public Object getFinalized() {
        return this.allValues.clone();
    }

    /**
     * Description of this CommEventData.
     *
     * @return An empty string if values are not yet finalized, the
     *         pretty-printed description of the finalized data otherwise.
     */
    @Override
    public String toString() {
        if (this.allValues == null) {
            return "";
        }
        String res = "\n";

        int maxLength = ("" + getMaxValue(this.allValues)).length();
        StringBuilder sb = null;
        String[] lines = new String[this.allValues.length];
        int i;
        int j;

        for (i = 0; i < this.allValues.length; i++) {
            lines[i] = "";
            for (j = 0; j < this.allValues.length; j++) {
                sb = new StringBuilder(lines[i]);
                sb.append(String.format("%1$" + maxLength + "s", this.allValues[i][j]) + " ");
                lines[i] = sb.toString();
            }
        }

        for (i = lines.length - 1; i >= 0; i--) {
            res += (lines[i] + "\n");
        }

        return res;
    }

    /**
     * Returns the max of a 2-dim array of integers.
     *
     * @param a
     *            The 2-dim array
     * @return The max of the 2-dim array
     */
    public static int getMaxValue(int[][] a) {
        int i;
        int j;
        int max = 0;

        for (i = 0; i < a.length; i++) {
            for (j = 0; j < a.length; j++) {
                if (a[i][j] > max) {
                    max = a[i][j];
                }
            }
        }
        return max;
    }
}
