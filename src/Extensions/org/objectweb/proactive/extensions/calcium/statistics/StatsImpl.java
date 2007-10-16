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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.statistics;

import org.objectweb.proactive.extensions.calcium.muscle.Muscle;


public class StatsImpl implements Stats {
    private long computationTime;
    private long waitingTime;
    private long processingTime;
    private long readyTime;
    private long resultsTime;
    private long initTime;
    private long finitTime;
    private long currentStateStart;
    private long computationBlockedFetchingData;
    private long unusedCPUTime;
    private long uploadedBytes;
    private long downloadedBytes;
    private Workout workout;
    private int maxResources;

    //sub task related stats
    public int subTreeSize;
    public int numberLeafs;

    public StatsImpl() {
        computationTime = 0;
        waitingTime = processingTime = readyTime = resultsTime = 0;
        initTime = System.currentTimeMillis();
        finitTime = 0;
        currentStateStart = initTime;
        maxResources = 1;
        computationBlockedFetchingData = unusedCPUTime = 0;
        uploadedBytes = downloadedBytes = 0;

        subTreeSize = numberLeafs = 0;
        workout = new Workout(8);
    }

    public long getComputationTime() {
        return computationTime;
    }

    public void addComputationTime(long time) {
        computationTime += time;
    }

    public void addComputationBlockedFetchingData(long time) {
        computationBlockedFetchingData += time;
    }

    public void addUnusedCPUTime(long time) {
        unusedCPUTime += time;
    }

    public void addUploadedBytes(long time) {
        uploadedBytes += time;
    }

    public void addDownloadedBytes(long time) {
        downloadedBytes += time;
    }

    public void exitReadyState() {
        readyTime += getStateElapsedTime();
    }

    public void exitProcessingState() {
        processingTime += getStateElapsedTime();
    }

    public void exitWaitingState() {
        waitingTime += getStateElapsedTime();
    }

    public void exitResultsState() {
        resultsTime += getStateElapsedTime();
    }

    private long getStateElapsedTime() {
        long initTime = currentStateStart;
        currentStateStart = System.currentTimeMillis();
        return currentStateStart - initTime;
    }

    @Override
    public String toString() {
        String ls = System.getProperty("line.separator");

        return "Time: " + processingTime + "P " + readyTime + "R " +
        waitingTime + "W " + resultsTime + "F " + getWallClockTime() + "L " +
        getComputationTime() + "C[ms] " + unusedCPUTime + "UC[ms] " +
        computationBlockedFetchingData + "B[ms] " + uploadedBytes +
        "Up [bytes] " + downloadedBytes + "Down [bytes] " + "TreeSize:" +
        getTreeSize() + " " + "TreeSpan:" + getTreeSpan() + " " + "TreeDepth:" +
        getTreeDepth() + ls + workout;
    }

    public void markFinishTime() {
        finitTime = System.currentTimeMillis();
    }

    public Workout getWorkout() {
        return workout;
    }

    public void addChildStats(StatsImpl stats) {
        this.processingTime += stats.getProcessingTime();
        this.computationTime += stats.getComputationTime();
        this.readyTime += stats.getReadyTime();

        this.subTreeSize += stats.getTreeSize();
        this.numberLeafs += ((stats.getNumberLeafs() == 0) ? 1
                                                           : stats.getNumberLeafs());
        this.maxResources = Math.max(maxResources,
                stats.getMaxAvailableResources());
        this.workout.track(stats.workout);

        this.computationBlockedFetchingData += stats.computationBlockedFetchingData;
        this.unusedCPUTime += stats.unusedCPUTime;
        this.uploadedBytes += stats.uploadedBytes;
        this.downloadedBytes += stats.downloadedBytes;
    }

    private int getNumberLeafs() {
        return numberLeafs;
    }

    private int getNumberInnerNodes() {
        return getTreeSize() - getNumberLeafs();
    }

    public void setMaxAvailableResources(int numResources) {
        this.maxResources = Math.max(this.maxResources, numResources);
    }

    // **************   INTERFACE METHODS   *****************
    public long getWallClockTime() {
        if (finitTime == 0) {
            return System.currentTimeMillis() - initTime;
        }
        return finitTime - initTime;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public long getReadyTime() {
        return readyTime;
    }

    public long getResultsTime() {
        return resultsTime;
    }

    public int getTreeSize() {
        return subTreeSize + 1;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public float getTreeDepth() {
        float base = getTreeSpan();
        if (base <= 0) {
            return 0;
        }

        return (float) (Math.log(subTreeSize) / Math.log(base));
    }

    public float getTreeSpan() {
        if (getNumberInnerNodes() == 0) {
            return 0;
        }

        return subTreeSize / getNumberInnerNodes();
    }

    public Exercise getExcercise(Muscle muscle) {
        return workout.getExercise(muscle);
    }

    public int getMaxAvailableResources() {
        return this.maxResources;
    }
}
