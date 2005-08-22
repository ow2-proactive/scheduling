package modelisation.simulator.mixed.multiqueue;

import modelisation.simulator.common.SimulatorElement;
import modelisation.simulator.mixed.Agent;
import modelisation.simulator.mixed.ForwarderChain;
import modelisation.simulator.mixed.Server;
import modelisation.simulator.mixed.Source;


public class ThreadWrapper {
    protected int elements;
    protected int remainingToProcess;
    protected SimulatorElement[] source;
    protected SimulatorElement[] agent;
    protected SimulatorElement[] forwarderChain;
    protected int elementsPerThread;
    ThreadedUpdater[] threadedUpdaterArray;
    protected Server s;

    public ThreadWrapper(SimulatorElement[] source, SimulatorElement[] agent,
        SimulatorElement[] forwarderChain, Server s, int value) {
        this.elements = source.length;

        // this.elements = source.length;
        int couples = source.length;
        this.elementsPerThread = value;

        int numberOfThreads;
        if ((elements % value) == 0) {
            numberOfThreads = elements / value;
        } else {
            numberOfThreads = (elements / value) + 1;
        }
        this.threadedUpdaterArray = new ThreadedUpdater[numberOfThreads];
        //        System.out.println(" Elements " + elements);
        //        System.out.println(" Puting " + value + " elements per threads");
        //        System.out.println(" Creating " + numberOfThreads + 
        //                           "  ThreadUpdater");
        Source[] tmpSource = null;
        Agent[] tmpAgent = null;
        ForwarderChain[] tmpFChain = null;
        int pos = 0;
        int length = 0;
        int remain = couples;
        while (remain > 0) {
            if (remain > value) {
                length = value;
            } else {
                length = remain;
            }
            tmpSource = new Source[length];
            tmpAgent = new Agent[length];
            tmpFChain = new ForwarderChain[length];
            System.arraycopy(source, pos, tmpSource, 0, length);
            System.arraycopy(agent, pos, tmpAgent, 0, length);
            System.arraycopy(forwarderChain, pos, tmpFChain, 0, length);
            System.out.println(pos / value);
            threadedUpdaterArray[pos / value] = new ThreadedUpdater(this,
                    tmpSource, tmpAgent, tmpFChain);
            new Thread(threadedUpdaterArray[pos / value]).start();
            pos += length;
            remain -= length;
        }

        //
        //        for (int i = 0; i < elements; i++) {
        //
        //
        //            threadedUpdaterArray[i] = new ThreadedUpdater(this,
        //                                                          (Source)source[i],
        //                                                          (Agent)agent[i],
        //                                                          (ForwarderChain)forwarderChain[i]);
        //            new Thread(threadedUpdaterArray[i]).start();
        //        }
    }

    public void decreaseTimeElements(double time) {
        this.remainingToProcess = threadedUpdaterArray.length;
        int max = this.remainingToProcess;

        //        System.out.println(
        //                "ThreadWrapper decreaseTimeElements " + 
        //                this.remainingToProcess);
        for (int i = 0; i < max; i++) {
            this.threadedUpdaterArray[i].decreaseTime(time);
        }
        this.waitForEnd();
    }

    public void updateElements(double currentTime) {
        this.remainingToProcess = threadedUpdaterArray.length;
        int max = this.remainingToProcess;

        //        System.out.println(
        //                "ThreadWrapper updateElements " + this.remainingToProcess);
        for (int i = 0; i < max; i++) {
            this.threadedUpdaterArray[i].updateElements(currentTime);
        }
        this.waitForEnd();
    }

    public synchronized void operationDone() {
        this.remainingToProcess--;
        //        System.out.println("done, remain " + this.remainingToProcess);
        if (this.remainingToProcess == 0) {
            notifyAll();
        }
    }

    public synchronized void waitForEnd() {
        while (this.remainingToProcess != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //        this.remainingToProcess = elements;
    }
}
