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
package org.objectweb.proactive.examples.c3d;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.examples.c3d.geom.Vec;


/**
 * An election, as a poll under democracy. A certain number of people can vote,
 * and then results are drawn.
 */
public class Election implements RunActive, Serializable {
    private static final int WAITMSECS = 4000; // Duration of one election round in milliseconds
    private C3DDispatcher c3ddispatcher; // to give back results
    private int nbUsers = 0; // To know when the election is over
    private long startTime = 0; // To enable the countdown
    private Ballots ballots = new Ballots(); // registers the votes
    private Vector<Integer> voters = new Vector<Integer>(); // remembers who has voted

    /** Required ProActive empty no-arg constructor */
    public Election() {
    }

    /** Real Constructor */
    public Election(C3DDispatcher c3ddispatcher) {
        this.c3ddispatcher = c3ddispatcher;
        nbUsers = 1;
    }

    /** ProActive queue handling. Serve methods with time-out once election is started. */
    public void runActivity(Body body) {
        Service service = new Service(body);

        // Loops over lifetime
        while (body.isActive()) {
            if (this.startTime == 0) { // election not yet started
                service.blockingServeOldest(); // just wait for first vote to trigger timer.
            } else { // An election was started, let's use a timer.   
                long time = // time is in milliseconds 
                this.startTime - System.currentTimeMillis() + WAITMSECS;
                if (time < 0) {
                    voteOver("time's up");
                } else {
                    // serve one request, or return if time given is up 
                    service.blockingServeOldest(time);
                }
            }
        }
    }

    /** Submit a vote in this election.
     * @param i_user id of the voter
     * @param wish Vec that is voted
     * @return the numbers of voters up to now */
    public void vote(int i_user, String name, Vec wish) {
        // check no bad dude is voting twice
        if (voters.contains(new Integer(i_user))) {
            this.c3ddispatcher.userLog(i_user, "You have already voted in this round");
            return;
        }

        // register this vote
        this.voters.add(new Integer(i_user));
        this.ballots.add(wish);

        //  We should be starting a new election, if startime=0 <==> ballots.size=0 <==> voters.size=0 
        if (this.startTime == 0) {
            this.startTime = System.currentTimeMillis();
            this.c3ddispatcher.userLog(i_user, "Request 'rotate " + wish.direction() + "' submitted, \nnew " +
                (WAITMSECS / 1000) + " second election started.");
            this.c3ddispatcher
                    .allLogExcept(i_user, "New " + (WAITMSECS / 1000) + " second election started:");
        }
        this.c3ddispatcher.allLogExcept(i_user, "   User " + name + " wants to rotate " + wish.direction());
        // Has everybody voted ? 
        if (this.voters.size() == this.nbUsers) {
            voteOver("everybody voted");
        }
    }

    /** Declare the vote over, find out the winner, and notify Dispatcher. */
    private void voteOver(String reason) {
        this.c3ddispatcher.allLog("Election finished : " + reason);
        Vec winner = ballots.winner();
        if (winner == null) {
            this.c3ddispatcher.allLog("   No consensus found, vote again please!");
        } else {
            this.c3ddispatcher.allLog("   The scene will be rotated by " + winner.direction());
            this.c3ddispatcher.rotateScene(-1, winner); // i_user = -1 means this is called from Election!
        }

        this.startTime = 0;
        voters.clear();
        ballots.clear();
    }

    /** Declares how many voters may vote in this election.
     * @param nbUsers The nbUsers to set. */
    public void setNbUsers(int nbUsers) {
        this.nbUsers = nbUsers;
    }

    public boolean isRunning() {
        return this.startTime != 0;
    }

    /** Destroy the Active Object */
    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }

    /** Class to register votes, and then determine a winner.
     * In this implementation, a vote is saved if it is not already in this Vector.
     * This allows us to have ballots.size == 1 <==> all votes are equal
     * To make a democracy, ie winner has most votes, you need to change winner and add methods. */
    private class Ballots extends Vector implements Serializable {
        public Vec winner() {
            if (size() == 1) {
                return (Vec) get(0);
            }
            return null;
        }

        /** Only add elements which have not been put in yet. This is not DEMOCRACY!
         * To mimic a democracy, you need to count all votes, and then determine the majority*/
        public void add(Vec v) {
            int size = size();
            for (int i = 0; i < size; i++) {
                Vec tmp = (Vec) get(i);
                if (tmp.equals(v)) {
                    return;
                }
            }
            super.add(v);
        }
    }
}
