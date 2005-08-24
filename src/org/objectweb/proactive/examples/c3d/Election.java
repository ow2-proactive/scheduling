/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.examples.c3d;

import org.objectweb.proactive.examples.c3d.geom.Vec;

import java.util.HashMap;
import java.util.Iterator;


/**
 * An election, as a poll under democracy. A certain number of people can vote,
 * and then results are drawn. Implements the singleton pattern.
 */

// final avoids creation of subclasses of Election ==> don't break singleton pattern
final class Election extends Thread {
    //static fields
    private static final int WAITSECS = 4; // Duration of one election round in seconds
    private static Election election;

    //private fields, accessible through Election.election
    private boolean running = false;
    private HashMap wishes; // hashes of (user_ident, rotation Vec voted)

    // implementation note : I would have prefered an array, but how can I know 
    // how many Users there are? Maybe in the constructor? 
    private C3DDispatcher c3ddispatcher; // to give back results

    /**
     * Private constructor, which is only called by Election, needed by singleton pattern.
     */
    private Election(int i_user, Vec wish, C3DDispatcher c3ddispatcher) {
        this.c3ddispatcher = c3ddispatcher;
        this.running = true;
        this.wishes = new HashMap();
        this.c3ddispatcher.userLog(i_user,
            "Request 'rotate " + wish.direction() + "' submitted, \nnew " +
            WAITSECS + " second election started ...");
        this.c3ddispatcher.allLogExcept(i_user,
            "New " + WAITSECS + " second election started:\n   User " +
            this.c3ddispatcher.nameOfUser(i_user) + " wants to rotate " +
            wish.direction());
        // Launches the Election thread
        this.start();
    }

    /**
     * Handles the voting thread : waits for k seconds, counts votes, decides of the winner.
     * This method runs while others calls can be made, like a new vote submission.
     */
    public synchronized void run() {
        try {
            wait(WAITSECS * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.c3ddispatcher.allLog("Election finished");
        // count scores
        // TODO : this is horrible code ! counting votes should be more elegant!
        HashMap scores = new HashMap();
        for (Iterator iterator = wishes.values().iterator();
                iterator.hasNext();) {
            // for every vote expressed(let's call it 'vote')
            Vec vote = (Vec) iterator.next();
            boolean found = false;
            for (Iterator keyiterator = scores.keySet().iterator();
                    keyiterator.hasNext();) {
                // for every vote already counted  (let's call it 'counted') 
                Vec counted = (Vec) keyiterator.next();

                // if 'vote' is 'counted', increment  
                if (counted.equals(vote)) {
                    found = true;
                    Integer oldVal = (Integer) scores.remove(counted);
                    scores.put(vote, new Integer(oldVal.intValue() + 1));
                }
            }

            //  'vote' is not yet 'counted', so count it
            if (!found) {
                scores.put(vote, new Integer(1));
            }
        }

        // display results 
        Vec winner = null;
        this.c3ddispatcher.allLog("   Result:");
        for (Iterator iterator = scores.keySet().iterator();
                iterator.hasNext();) {
            Vec key = (Vec) iterator.next();
            int votes = ((Integer) scores.get(key)).intValue();
            c3ddispatcher.allLog("       " + votes + " votes for " +
                key.direction());
            if (winner == null) {
                winner = key;
            } else {
                // at least 2 different votes, ie no consensus
                winner = null;
                break;
            }
        }

        if (winner == null) {
            this.c3ddispatcher.allLog(
                "   No consensus found, vote again please!");
        } else {
            this.c3ddispatcher.allLog("   The scene will be rotated by " +
                winner.direction());
            this.c3ddispatcher.rotateScene(0, winner);
        }
        this.running = false;
        this.wishes.clear();
    }

    /**
     * Submit a vote in this election.
     * @param i_user id of the voter
     * @param wish Vec that is voted
     * @return the numbers of voters up to now
     */
    public synchronized static int vote(int i_user, Vec wish) {
        assert Election.election != null : "Trying to vote in an Election not started!";
        if (Election.election.wishes.containsKey(new Integer(i_user))) {
            Election.election.c3ddispatcher.userLog(i_user,
                "You have already voted in this round");
        } else {
            Election.election.wishes.put(new Integer(i_user), wish);
        }
        return Election.election.wishes.size();
    }

    public synchronized static boolean isRunning() {
        if (Election.election == null) {
            return false;
        }
        return Election.election.running;
    }

    /**
     * Static method to declare the vote over.
     */
    public static synchronized void finish() {
        assert Election.election != null : "Trying to finish an Election not started!";
        Election.election.voteOver();
    }

    /**
     * Declare the vote over.
     * This is not static, so it has to be called as Election.election.voteOver()
     */
    private synchronized void voteOver() {
        this.c3ddispatcher.allLog("Everybody voted");
        this.notify();
    }

    public static void newElection(int i_user, Vec wish,
        C3DDispatcher c3ddispatcher) {
        Election.election = new Election(i_user, wish, c3ddispatcher);
        Election.vote(i_user, wish);
    }
}
