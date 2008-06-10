/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.benchmarks.NAS.IS;

import java.util.Arrays;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.benchmarks.NAS.Kernel;
import org.objectweb.proactive.benchmarks.NAS.util.Random;
import org.objectweb.proactive.benchmarks.NAS.util.wrapper.IntWrapper;
import org.objectweb.proactive.benchmarks.NAS.util.wrapper.TabWrapper;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.util.Timed;
import org.objectweb.proactive.benchmarks.timit.util.TimerCounter;
import org.objectweb.proactive.benchmarks.timit.util.observing.Event;
import org.objectweb.proactive.benchmarks.timit.util.observing.EventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEvent;
import org.objectweb.proactive.benchmarks.timit.util.observing.commobserv.CommEventObserver;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventData;
import org.objectweb.proactive.benchmarks.timit.util.observing.defaultobserver.DefaultEventObserver;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;


/**
 * Kernel IS
 * 
 * A large integer sort. This kernel performs a sorting operation that is important in "particle
 * method" codes. It tests both integer computation speed and communication performance.
 */
public class WorkerIS extends Timed {

    public static final boolean COMMUNICATION_PATTERN_OBSERVING_MODE = false;

    /* Timer counters */
    private TimerCounter T_total = new TimerCounter("Total");
    private TimerCounter T_computation = new TimerCounter("Computation");
    private TimerCounter T_communication = new TimerCounter("Communication");
    /* Event observers */
    private EventObserver E_mflops;
    private CommEventObserver nbCommObserver;
    /** Number of Communication observer */
    private CommEventObserver commSizeObserver;
    /** Communication size observer */

    private ISProblemClass clss;
    private int[] key_array;
    private int[] key_buff1;
    private int[] key_buff2;
    private int[] bucket_size;
    private int[] bucket_size_totals;
    private int[] bucket_ptrs;
    private int[] process_bucket_distrib_ptr1;
    private int[] process_bucket_distrib_ptr2;
    private int[] send_displ;
    private int[] send_count;
    private int[] recv_displ;
    private int[] recv_count;
    private Random rng;
    private int nbReceiveCount = 0;
    private int nbReceiveData = 0;
    private int iteration;
    private int total_local_keys;
    private boolean isFreeIteration = true;

    /* Group related variables */
    private WorkerIS asyncRefToMe;
    /** The lead worker (Stub) */
    private WorkerIS leadWorker;
    /** The next worker (Stub) */
    private WorkerIS nextWorker;

    /** The ProActive group with me */
    private Group<WorkerIS> group;
    private WorkerIS typedGroup;

    /** The ProActive group without me */
    private Group<WorkerIS> groupWithoutMe;
    private WorkerIS typedGroupWithoutMe;

    /** A group used for a allToall operation */
    private Group<TabWrapper> tabWrapperGroup;
    private TabWrapper typedTabWrapperGroup;
    private TabWrapper[] arrayOfTabWrapper; // used for casts avoidance

    /** A group used for a allToall operation */
    private Group<IntWrapper> intWrapperGroup;
    private IntWrapper typedIntWrapperGroup;

    /* Active objects */
    private AllBucketSize allBucketSize;

    protected int rank;
    private int groupSize = 0;

    /* Verification counters */
    private int passed_verification = 0;
    private int totalPassedVerification = 0;
    private int passedVerificationReceived = 0;

    /** Is this first worker */
    private boolean isFirst;

    /** Is this last worker */
    private boolean isLast;

    private int receivedAllBucketSize;
    private int[] tableAllBucketSize;

    private int reductorRank;

    //
    // ------ CONSTRUCTORS ----------------------------------------------------
    //
    public WorkerIS() {
    }

    public WorkerIS(ISProblemClass clss) {
        this.clss = clss;
        this.rng = new Random();

    }

    public WorkerIS(ISProblemClass clss, AllBucketSize ao) {
        this.allBucketSize = ao;
        this.clss = clss;
        this.rng = new Random();

    }

    //
    // ------- PUBLIC METHODS -------------------------------------------------
    //
    /**
     * Start the worker
     */
    public void start() {
        this.asyncRefToMe = (WorkerIS) PAActiveObject.getStubOnThis();
        this.typedGroup = (WorkerIS) PASPMD.getSPMDGroup();
        this.typedGroupWithoutMe = (WorkerIS) PAGroup.captureView(this.typedGroup);
        this.groupSize = PASPMD.getMySPMDGroupSize();
        this.rank = PASPMD.getMyRank();
        this.group = PAGroup.getGroup(this.typedGroup);
        this.groupWithoutMe = PAGroup.getGroup(this.typedGroupWithoutMe);
        this.leadWorker = (WorkerIS) group.get(0);
        this.isFirst = (this.rank == 0);
        this.isLast = (this.rank == (this.groupSize - 1));
        this.nextWorker = (!isLast ? (WorkerIS) group.get(this.rank + 1) : null);
        this.reductorRank = (this.groupSize == 1 ? 0 : 1);

        // RETIRING THE CURRENT WORKER FROM THE GROUP TO AVOID LOCAL STUB CALL
        // WARNING THE typedGroupWithoutMe.size = typedGroup-1
        this.groupWithoutMe.remove(this.asyncRefToMe);

        if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {

            // Create a observer to observe the number of communication
            this.nbCommObserver = new CommEventObserver("nbCommObserver", groupSize, rank);

            // Create a observer to observe the size of the communication
            this.commSizeObserver = new CommEventObserver("commSizeObserver", this.groupSize, rank);
            super.activate(new EventObserver[] { nbCommObserver, commSizeObserver });
        }

        // Setup the timing system
        E_mflops = new DefaultEventObserver("mflops", DefaultEventData.MIN, DefaultEventData.MIN);
        super.activate(new TimerCounter[] { T_total, T_computation, T_communication },
                new EventObserver[] { E_mflops });

        this.initTypedTabWrapperGroup();

        if (this.clss == null) {
            System.err.println("No input data");
            this.terminate();
            return;
        }

        if (this.groupSize != this.clss.NUM_PROCS) {
            System.err.println("Number of required workers (" + this.clss.NUM_PROCS + ") and group size (" +
                this.groupSize + ") mismatch. Exiting ...");
            this.terminate();
            return;
        }

        this.createArrays();

        /*
         * Do one iteration for free (i.e., untimed) to guarantee intialization of all data and code
         * pages and respective tables
         */
        this.iteration = 1;
        // Begin total and computation time counter
        this.T_total.start();
        this.rank();

        /* Start the bench */
        if (this.isFirst) {
            System.out.println("\n   iterations");
        }
    }

    /**
     * The main loop
     */
    public void iterate() {
        if (this.iteration <= this.clss.MAX_ITERATIONS) {
            if (this.isFirst) {
                System.out.println("        " + this.iteration);

                if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                    // /////////////////////////////////////////////
                    // / Notification of all ranks except my rank
                    this.notifyAllGroupRanks(0, true);
                    // /////////////////////////////////////////////
                }

                // HERE USING THE typedGroupWithoutMe
                this.T_communication.start();
                this.typedGroupWithoutMe.rank();
                this.T_communication.stop();
                this.rank();
            }
        }
    }

    /**
     * Terminate the Active Object
     */
    public void terminate() {
        PAActiveObject.getBodyOnThis().terminate();
    }

    /**
     * Sort key_array
     */
    public void rank() {
        this.rank_init();
    }

    public void setAllBucketSize(AllBucketSize ao) {
        this.allBucketSize = ao;
    }

    /**
     * This method initialize the typedTabWrapperGroup used for the ALLtoALL operation. This method
     * involves a powerful ProActive functionality which is the automatic, threaded parameters
     * dispatching ability.
     */
    public void initTypedTabWrapperGroup() {
        try {

            this.typedTabWrapperGroup = (TabWrapper) PAGroup.newGroup(TabWrapper.class.getName());
            this.tabWrapperGroup = PAGroup.getGroup(this.typedTabWrapperGroup);

            this.typedIntWrapperGroup = (IntWrapper) PAGroup.newGroup(IntWrapper.class.getName());
            this.intWrapperGroup = PAGroup.getGroup(this.typedIntWrapperGroup);

            this.arrayOfTabWrapper = new TabWrapper[this.groupSize - 1];

            TabWrapper tabWrapper;
            for (int i = 0; i < this.groupSize - 1; i++) {
                tabWrapper = new TabWrapper(this.rank);
                this.arrayOfTabWrapper[i] = tabWrapper;
                this.tabWrapperGroup.add(tabWrapper);

                this.intWrapperGroup.add(new IntWrapper(this.rank));
            }

            PAGroup.setScatterGroup(this.typedTabWrapperGroup);
            PAGroup.setScatterGroup(this.typedIntWrapperGroup);

        } catch (ClassNotReifiableException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * TEMP Compute the sum of n operands and send it to the SPMD group
     * 
     * @param tab
     *            one operand of the sum operation
     */
    public void sum(int[] tab) {
        this.T_computation.start();
        for (int i = 0; i < tab.length; i++) {
            this.tableAllBucketSize[i] += tab[i];
        }

        this.receivedAllBucketSize++;

        if (this.groupSize == this.receivedAllBucketSize) {

            this.asyncRefToMe.receiveBucketSizeTotal(this.tableAllBucketSize); // need asynchronism
            // here

            this.T_computation.stop();
            this.T_communication.start();
            this.typedGroupWithoutMe.receiveBucketSizeTotal(this.tableAllBucketSize);
            this.T_communication.stop();
            this.T_computation.start();

            java.util.Arrays.fill(this.tableAllBucketSize, 0);
            this.receivedAllBucketSize = 0;
        }
        this.T_computation.stop();
    }

    /**
     * Receives the reduce operation sum result on the <code>bucket_size</code> and continue rank
     * (call rank_predistribute()).
     * 
     * @param table
     *            reduced table
     */
    public void receiveBucketSizeTotal(int[] table) {
        this.bucket_size_totals = table;

        this.T_computation.start();
        this.asyncRefToMe.rank_predistribute(); // ~~~
        this.T_computation.stop();
    }

    /**
     * Compute locally the distribution of buckets and each worker send how many keys will send to
     * every other worker.
     */
    public void rank_predistribute() {
        this.T_computation.start();
        int i, j;
        int bucket_sum_accumulator;
        int local_bucket_sum_accumulator;

        /*
         * Determine Redistibution of keys: accumulate the bucket size totals till this number
         * surpasses NUM_KEYS (which the average number of keys per processor). Then all keys in
         * these buckets go to processor 0. Continue accumulating again until supassing 2*NUM_KEYS.
         * All keys in these buckets go to processor 1, etc. This algorithm guarantees that all
         * processors have work ranking; no processors are left idle. The optimum number of buckets,
         * however, does not result in as high a degree of load balancing (as even a distribution of
         * keys as is possible) as is obtained from increasing the number of buckets, but more
         * buckets results in more computation per processor so that the optimum number of buckets
         * turns out to be 1024 for machines tested. Note that process_bucket_distrib_ptr1 and
         * ..._ptr2 hold the bucket number of first and last bucket which each processor will have
         * after the redistribution is done.
         */
        bucket_sum_accumulator = 0;
        local_bucket_sum_accumulator = 0;
        this.send_displ[0] = 0;
        this.process_bucket_distrib_ptr1[0] = 0;

        for (i = 0, j = 0; i < this.clss.NUM_BUCKETS; i++) {
            bucket_sum_accumulator += this.bucket_size_totals[i];
            local_bucket_sum_accumulator += this.bucket_size[i];

            if (bucket_sum_accumulator >= ((j + 1) * this.clss.NUM_KEYS)) {
                this.send_count[j] = local_bucket_sum_accumulator;

                if (j != 0) {
                    this.send_displ[j] = this.send_displ[j - 1] + this.send_count[j - 1];
                    this.process_bucket_distrib_ptr1[j] = this.process_bucket_distrib_ptr2[j - 1] + 1;
                }

                this.process_bucket_distrib_ptr2[j++] = i;
                local_bucket_sum_accumulator = 0;
            }
        }

        /*
         * This is the redistribution section: first find out how many keys each processor will send
         * to every other processor:
         */
        if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // /////////////////////////////////////////////
            // / Notification of a scattering communication to everybody except me
            this.notifyAllGroupRanks(4, // the size of the message
                    true); // my rank not included
            // /////////////////////////////////////////////
        }

        for (i = 0; i < this.rank; i++) {
            ((IntWrapper) this.intWrapperGroup.get(i)).setVal(this.send_count[i]);
        }

        for (i = this.rank + 1; i < this.groupSize; i++) {
            ((IntWrapper) this.intWrapperGroup.get(i - 1)).setVal(this.send_count[i]);
        }

        PAGroup.setScatterGroup(this.typedGroupWithoutMe);// added

        this.T_computation.stop();
        this.T_communication.start();
        this.typedGroupWithoutMe.receiveCount(this.typedIntWrapperGroup);
        this.T_communication.stop();
        this.T_computation.start();

        PAGroup.unsetScatterGroup(this.typedGroupWithoutMe);// added

        this.T_computation.stop();

        // Do job locally for this rank
        this.receiveCount(new IntWrapper(this.rank, this.send_count[this.rank]));
    }

    public void receiveCount(IntWrapper intWrapper) {
        this.T_computation.start();

        this.recv_count[intWrapper.getRank()] = intWrapper.getVal();
        this.nbReceiveCount++;

        if (this.nbReceiveCount == this.groupSize) {

            if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // /////////////////////////////////////////////
                // / Notification of a barrier communication to everybody
                this.notifyAllGroupRanks(TimIt.getObjectSize("receiveAllCount" + this.iteration), /*
                 * size
                 * of
                 * the
                 * message
                 */
                false); // my rank included
                // /////////////////////////////////////////////
            }

            this.T_computation.stop();
            this.T_communication.start();
            PASPMD.totalBarrier("receiveAllCount" + this.iteration);
            this.T_communication.stop();

            this.nbReceiveCount = 0;
            this.rank_senddisplacements(); // not need asynchronous call
        } else {
            this.T_computation.stop();
        }
    }

    /**
     * This method will be called remotely by other workers.
     */
    public void receiveData(TabWrapper tabWrapper) {
        this.T_computation.start();

        System.arraycopy(tabWrapper.getTab(), 0, this.key_buff2, // added
                this.recv_displ[tabWrapper.getRank()], this.recv_count[tabWrapper.getRank()]); // added

        this.nbReceiveData++;

        if (this.nbReceiveData == this.groupSize) {
            this.T_computation.stop();
            this.rank_end(); // not need asynchronous call
            this.nbReceiveData = 0;
        } else {
            this.T_computation.stop();
        }
    }

    public void full_verify2(int k) {
        int i;
        int j = 0;

        if (!this.isFirst) { // if (this.rank > 0) {
            if (k > this.key_array[0]) {
                System.out.println("Boundary element incorrect on proc " + this.rank + "; k, key = " + k +
                    ", " + this.key_array[0]);
                j++;
            }
        }

        /* Confirm keys correctly sorted: count incorrectly sorted keys, if any */
        for (i = 1; i < this.total_local_keys; i++)
            if (this.key_array[i - 1] > this.key_array[i]) {
                System.out.println("Internal element incorrect on proc " + this.rank + "; i, km1, k = " + i +
                    ", " + this.key_array[i - 1] + ", " + this.key_array[i]);
                j++;
            }

        if (j != 0) {
            System.out.println("Processor " + this.rank + ":  Full_verify: number of keys out of sort: " + j);
        } else {
            this.passed_verification++;
        }

        // ***** finalization ****
        super.getEventObservable().notifyObservers(new Event(E_mflops, getMflops()));

        if (this.isFirst) {
            this.receivePassedVerification(this.passed_verification);
            boolean verified = (groupSize == 1 ? (passed_verification == 0) : (passed_verification != 0));
            super.finalizeTimed(this.rank, verified ? "" : "UNSUCCESSFUL");
        } else {
            this.leadWorker.receivePassedVerification(this.passed_verification);
            super.finalizeTimed(this.rank, "");
        }
    }

    public void receivePassedVerification(int pv) {
        this.passedVerificationReceived++;

        /* Passed verification management */
        this.totalPassedVerification += pv;

        /* Check if all workers have answered */
        if (this.passedVerificationReceived == this.groupSize) {
            /* Check if passed verifications are ok */
            if (this.passed_verification != ((5 * this.clss.MAX_ITERATIONS) + this.groupSize)) {
                this.passed_verification = 0;
            }
        }
    }

    //
    // ------- PRIVATE METHODS ------------------------------------------------
    //
    private void createArrays() {
        this.key_array = new int[this.clss.SIZE_OF_BUFFERS]; // BIG
        this.key_buff1 = new int[this.clss.SIZE_OF_BUFFERS]; // BIG
        this.key_buff2 = new int[this.clss.SIZE_OF_BUFFERS]; // BIG

        int arraySize = this.clss.NUM_BUCKETS + this.clss.TEST_ARRAY_SIZE;

        this.bucket_ptrs = new int[this.clss.NUM_BUCKETS];
        this.bucket_size = new int[arraySize];
        this.process_bucket_distrib_ptr1 = new int[arraySize];
        this.process_bucket_distrib_ptr2 = new int[arraySize];

        this.send_displ = new int[this.clss.NUM_PROCS];
        this.send_count = new int[this.clss.NUM_PROCS];
        this.recv_displ = new int[this.clss.NUM_PROCS];
        this.recv_count = new int[this.clss.NUM_PROCS];

        /* Printout initial NPB info */
        if (this.isFirst) {
            Kernel.printStarted(this.clss.KERNEL_NAME, this.clss.PROBLEM_CLASS_NAME,
                    new long[] { this.clss.TOTAL_KEYS }, this.clss.MAX_ITERATIONS, this.groupSize);
        }

        /* Generate random number sequence and subsequent keys on all procs */
        this.create_seq(314159265.00d, 1220703125.00d);
    }

    /**
     * Generate locally a sequence of key with the NAS Random generator Parameters must be odd
     * double precision integers in the range (1, 2^46)
     * 
     * @param seed
     * @param a
     */
    private void create_seq(double seed, double a) {
        double x;
        int k;
        int forTest;
        k = this.clss.MAX_KEY / 4;

        this.rng.setSeed(seed);
        this.rng.setGmult(a);

        forTest = this.clss.NUM_KEYS * this.rank;
        for (int i = 0; i < forTest; i++) {
            this.rng.randlc();
            this.rng.randlc();
            this.rng.randlc();
            this.rng.randlc();
        }

        for (int i = 0; i < this.clss.NUM_KEYS; i++) {
            x = this.rng.randlc();
            x += this.rng.randlc();
            x += this.rng.randlc();
            x += this.rng.randlc();
            this.key_array[i] = (int) (x * k);
        }
    }

    /**
     * Initializate data, and sort keys into appropriate bucket.
     */
    private void rank_init() {

        this.T_computation.start();

        int i, j;
        int[] t;
        int shift = this.clss.MAX_KEY_LOG_2 - this.clss.NUM_BUCKETS_LOG_2;
        int key;

        /* Iteration alteration of keys */
        if (this.isFirst) { // if (this.rank == 0) {
            this.key_array[this.iteration] = this.iteration;
            this.key_array[this.iteration + this.clss.MAX_ITERATIONS] = this.clss.MAX_KEY - this.iteration;
        }

        /* Initialize */
        // ~1000 loops
        j = this.clss.NUM_BUCKETS + this.clss.TEST_ARRAY_SIZE;
        for (i = 0; i < j; i++) {
            this.bucket_size[i] = 0;
            // bucket_size_totals[i] = 0; // useless
            this.process_bucket_distrib_ptr1[i] = 0;
            this.process_bucket_distrib_ptr2[i] = 0;
        }

        /*
         * Determine where the partial verify test keys are, load into top of array bucket_size
         */
        // ~5 loops
        t = this.clss.test_index_array;
        for (i = 0; i < this.clss.TEST_ARRAY_SIZE; i++) {
            if ((t[i] / this.clss.NUM_KEYS) == this.rank) {
                this.bucket_size[this.clss.NUM_BUCKETS + i] = this.key_array[t[i] % this.clss.NUM_KEYS];
            }
        }

        /* Determine the number of keys in each bucket */
        // Up to 2^29 loops
        j = this.clss.NUM_KEYS;
        for (i = 0; i < j; i++)
            this.bucket_size[key_array[i] >> shift]++;

        /* Accumulative bucket sizes are the bucket pointers */
        // ~1000 loops
        this.bucket_ptrs[0] = 0;
        for (i = 1; i < this.clss.NUM_BUCKETS; i++)
            this.bucket_ptrs[i] = this.bucket_ptrs[i - 1] + this.bucket_size[i - 1];

        /* Sort into appropriate bucket */
        // Up to 2^29 loops
        j = this.clss.NUM_KEYS;
        for (i = 0; i < j; i++) {
            key = this.key_array[i];
            this.key_buff1[this.bucket_ptrs[key >> shift]++] = key;
        }

        if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
            // /////////////////////////////////////////////
            // / Notification of a communication to the reductor ( by convention it's the rank 1 )
            // / will be done by each rank
            this.notifyOneRank(this.reductorRank, TimIt.getObjectSize(this.bucket_size));
            // / If the current rank is 1 therefor the reductor is on the same node
            // / so we simulate a broadcast of the recolted data
            if (this.rank == 1) {
                this.notifyAllGroupRanks(TimIt.getObjectSize(this.bucket_size), false);
            }
            // /////////////////////////////////////////////
        }

        /*
         * Get the bucket size totals for the entire problem. These will be used to determine the
         * redistribution of keys
         */
        // PASPMD.barrier("allBucketSizeReduce");
        this.T_computation.stop();
        this.T_communication.start();
        this.allBucketSize.sum(this.bucket_size);
        this.T_communication.stop();
    }

    /**
     * Determine the receive array displacements for the buckets and send it.
     */
    private void rank_senddisplacements() {
        this.T_computation.start();

        int i;

        // Determine the receive array displacements for the buckets
        this.recv_displ[0] = 0;

        for (i = 1; i < this.groupSize; i++) {
            this.recv_displ[i] = this.recv_displ[i - 1] + this.recv_count[i - 1];
        }

        // Now send the keys to respective processors
        int[] table;

        for (i = 0; i < this.rank; i++) {
            table = new int[this.send_count[i]];
            System.arraycopy(this.key_buff1, this.send_displ[i], table, 0, this.send_count[i]);
            this.arrayOfTabWrapper[i].setTab(table);

            if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // /////////////////////////////////////////////
                // Notification of a scattering operation ( here detecting it by individual
                // communication )
                this.notifyOneRank(i, TimIt.getObjectSize(table));
                // /////////////////////////////////////////////
            }
        }

        for (i = this.rank + 1; i < this.groupSize; i++) {
            table = new int[this.send_count[i]];
            System.arraycopy(this.key_buff1, this.send_displ[i], table, 0, this.send_count[i]);
            this.arrayOfTabWrapper[i - 1].setTab(table);

            if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // /////////////////////////////////////////////
                // Notification of a scattering operation ( here detecting it by individual
                // communication )
                this.notifyOneRank(i, TimIt.getObjectSize(table));
                // /////////////////////////////////////////////
            }
        }

        PAGroup.setScatterGroup(this.typedGroupWithoutMe);// added

        this.T_computation.stop();
        this.T_communication.start();
        this.typedGroupWithoutMe.receiveData(this.typedTabWrapperGroup);
        this.T_communication.stop();
        this.T_computation.start();

        PAGroup.unsetScatterGroup(this.typedGroupWithoutMe);// added

        // Do job locally for this worker
        System.arraycopy(this.key_buff1, this.send_displ[this.rank], this.key_buff2,
                this.recv_displ[this.rank], this.recv_count[this.rank]);
        this.nbReceiveData++;

        this.T_computation.stop();
        if (this.nbReceiveData == this.groupSize) {
            this.rank_end(); // not need asynchronous call
            this.nbReceiveData = 0;
        }
    }

    /**
     * Rank end
     */
    private void rank_end() {
        this.T_computation.start();

        int i;
        int j;
        int k;
        int m;
        int temp;
        int shift = this.clss.MAX_KEY_LOG_2 - this.clss.NUM_BUCKETS_LOG_2;
        int min_key_val;
        int max_key_val;

        /*
         * The starting and ending bucket numbers on each processor are multiplied by the interval
         * size of the buckets to obtain the smallest possible min and greatest possible max value
         * of any key on each processor
         */
        min_key_val = this.process_bucket_distrib_ptr1[this.rank] << shift;
        max_key_val = ((this.process_bucket_distrib_ptr2[this.rank] + 1) << shift) - 1;

        /* Clear the work array */
        // for (i = 0; i < (max_key_val - min_key_val + 1); i++)
        // key_buff1[i] = 0;
        Arrays.fill(this.key_buff1, 0, max_key_val - min_key_val + 1, 0);

        /*
         * Determine the total number of keys on all other processors holding keys of lesser value
         */
        m = 0;

        for (k = 0; k < this.rank; k++)
            for (i = this.process_bucket_distrib_ptr1[k]; i <= this.process_bucket_distrib_ptr2[k]; i++)
                m += this.bucket_size_totals[i]; /* m has total # of lesser keys */

        /* Determine total number of keys on this processor */
        j = 0;

        for (i = this.process_bucket_distrib_ptr1[this.rank]; i <= this.process_bucket_distrib_ptr2[this.rank]; i++)
            j += this.bucket_size_totals[i]; /* j has total # of local keys */

        /* Ranking of all keys occurs in this section: */
        /* shift it backwards so no subtractions are necessary in loop */

        // key_buff_ptr = key_buff1 - min_key_val; // no ptr arithmetic in Java
        /*
         * In this section, the keys themselves are used as their own indexes to determine how many
         * of each there are: their individual population
         */
        for (i = 0; i < j; i++) {
            this.key_buff1[key_buff2[i] - min_key_val]++;
            /* Now they have individual key population */
        }

        /*
         * To obtain ranks of each key, successively add the individual key population, not
         * forgetting to add m, the total of lesser keys, to the first key population
         */
        this.key_buff1[0] += m;

        temp = (max_key_val - min_key_val);
        for (i = 0; i < temp; i++) {
            this.key_buff1[i + 1] += this.key_buff1[i];
        }

        this.partialVerify(min_key_val, max_key_val);

        if (!this.isFreeIteration) {
            this.iteration++;
        } else {
            this.isFreeIteration = false;
            // timer.resetAllTimers();
            // Before reset timers we need to stop them properly
            this.T_computation.stop();
            this.T_total.stop();
            // Reset timers
            this.T_computation.reset();
            this.T_communication.reset();
            this.T_total.reset();
            this.T_total.start();
            this.T_computation.start();
        }

        if (this.iteration > this.clss.MAX_ITERATIONS) {
            // terminate timing
            this.T_computation.stop();
            this.T_total.stop();
            // The final verification is not included in the total time
            PASPMD.totalBarrier("beforeFullVerify" + this.iteration);
            this.full_verify(min_key_val, j, m);

        } else {
            if (WorkerIS.COMMUNICATION_PATTERN_OBSERVING_MODE) {
                // /////////////////////////////////////////////
                // / Notification of a barrier communication to everybody
                this.notifyAllGroupRanks(TimIt.getObjectSize("rankEnd" + this.iteration), // the
                        // size
                        // of
                        // the
                        // message
                        false); // my rank included
                // /////////////////////////////////////////////
            }

            this.T_computation.stop();
            this.T_communication.start();
            this.asyncRefToMe.iterate();
            PASPMD.totalBarrier("rankEnd" + this.iteration);
            this.T_communication.stop();

        }
    }

    private void partialVerify(int min_key_val, int max_key_val) {
        /* This is the partial verify test section */
        /* Observe that test_rank_array vals are */
        /* shifted differently for different cases */
        for (int i = 0; i < this.clss.TEST_ARRAY_SIZE; i++) {
            int k = this.bucket_size_totals[i + this.clss.NUM_BUCKETS]; /* Keys were hidden here */

            // int k = this.partial_verify_vals[i];
            if ((min_key_val <= k) && (k <= max_key_val)) {
                switch (this.clss.PROBLEM_CLASS_NAME) {
                    case 'S':
                        if (i <= 2) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        } else {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        }
                        break;
                    case 'W':
                        if (i < 2) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + (this.iteration - 2))) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        } else {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        }
                        break;
                    case 'A':
                        if (i <= 2) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + (this.iteration - 1))) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        } else {
                            if (key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - (this.iteration - 1))) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        }
                        break;
                    case 'B':
                        if ((i == 1) || (i == 2) || (i == 4)) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        } else {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        }
                        break;
                    case 'C':
                        if (i <= 2) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        } else {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                            } else {
                                this.passed_verification++;
                            }
                        }
                        break;
                    case 'D':
                        if (i <= 2) {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] + this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                                System.out.println("test_rank_array[" + i + "]: " +
                                    (this.key_buff1[k - 1 - min_key_val] - this.iteration));
                            } else {
                                this.passed_verification++;
                            }

                            System.out.println("Verified: proc " + this.rank + ", test_rank_array[" + i +
                                "]: " + (this.key_buff1[k - 1 - min_key_val] - this.iteration));
                        } else {
                            if (this.key_buff1[k - 1 - min_key_val] != (this.clss.test_rank_array[i] - this.iteration)) {
                                System.out.println("Failed partial verification: " + "iteration " +
                                    this.iteration + ", processor " + this.rank + ", test key " + i);
                                System.out.println("test_rank_array[" + i + "]: " +
                                    (this.key_buff1[k - 1 - min_key_val] + this.iteration));
                            } else {
                                this.passed_verification++;
                            }

                            System.out.println("Verified: proc " + this.rank + ", test_rank_array[" + i +
                                "]: " + (this.key_buff1[k - 1 - min_key_val] + this.iteration));
                        }
                        break;
                }
            }
        }
    }

    private void full_verify(int min_key_val, int total_local_keys, int total_lesser_keys) {
        int i;

        /* Now, finally, sort the keys: */
        for (i = 0; i < total_local_keys; i++) {
            this.key_array[--this.key_buff1[this.key_buff2[i] - min_key_val] - total_lesser_keys] = key_buff2[i];
        }

        /* Send largest key value to next processor */
        if (!this.isLast) {
            this.nextWorker.full_verify2(this.key_array[total_local_keys - 1]);
        }

        if (this.isFirst) {
            this.full_verify2(-1);
        }
    }

    private double getMflops() {
        double time = T_total.getTotalTime() / 1000.0;
        double mflops = (double) (clss.MAX_ITERATIONS * clss.TOTAL_KEYS) / time / 1000000.0;
        return mflops;
    }

    // //////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////
    // METHODS USED TO OBSERVE NB COMMS AND COMM SIZE
    // //////////////////////////////////////////////////////////////////////////
    private void notifyOneRank(int destRank, int messageSize) {
        // Notification of 1 communication with the dest rank
        super.getEventObservable().notifyObservers(new CommEvent(this.nbCommObserver, destRank, 1));

        // Notification
        super.getEventObservable().notifyObservers(
                new CommEvent(this.commSizeObserver, destRank, messageSize));
    }

    private void notifyAllGroupRanks(int messageSize, boolean withoutMe) {
        if (withoutMe) {
            for (int i = 0; i < this.groupSize; i++) {
                if (i != this.rank)
                    this.notifyOneRank(i, messageSize);
            }
        } else {
            for (int i = 0; i < this.groupSize; i++) {
                this.notifyOneRank(i, messageSize);
            }
        }
    }
}
