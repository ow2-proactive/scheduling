package modelisation.simulator.mixed.multiqueue;

import modelisation.simulator.common.SimulatorElement;

import modelisation.simulator.mixed.Agent;
import modelisation.simulator.mixed.ForwarderChain;
import modelisation.simulator.mixed.Server;
import modelisation.simulator.mixed.Source;


public class ThreadedUpdater implements Runnable {

    protected ThreadWrapper parent;
    protected Agent[] agent;
    protected Source[] source;
    //  protected Server server;
    protected ForwarderChain[] chain;
    protected boolean performUpdate = false;
    protected boolean decreaseTime = false;
    protected double time;

    public ThreadedUpdater(ThreadWrapper threadWrapper, Source[] source, 
                           Agent[] agent, ForwarderChain[] fChain) {
        this.parent = threadWrapper;
        this.agent = agent;
        this.source = source;
        // this.server = server;
        this.chain = fChain;
    }

    public synchronized void decreaseTime(double time) {
        this.time = time;
        this.decreaseTime = true;
        notifyAll();
    }

    public synchronized void updateElements(double currentTime) {
        this.time = currentTime;
        this.performUpdate = true;
        notifyAll();
    }

    public synchronized void work() {
        //       System.out.println("work " + this.decreaseTime + " " + this.performUpdate);
        while (!this.decreaseTime && !this.performUpdate) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //this.decreaseTime = false;
        if (this.decreaseTime) {
//            System.out.println(" decreaseTime");
            this._decreaseTime();
            this.decreaseTime = false;
        } else if (this.performUpdate) {
//            System.out.println(" performUpdate");
            this._updateElements();
            this.performUpdate = false;
        }
    }

    public void _decreaseTime() {
        if (time == 0) {
            this.parent.operationDone();
            return;
        }
        for (int i = 0; i < source.length; i++) {
            this.agent[i].decreaseRemainingTime(time);
            this.chain[i].decreaseRemainingTime(time);
            this.source[i].decreaseRemainingTime(time);
        }
        //      this.server.decreaseRemainingTime(time);
        this.parent.operationDone();
    }

    public void _updateElements() {
        for (int i = 0; i < source.length; i++) {
            this.agent[i].update(time);
            this.chain[i].update(time);
            this.source[i].update(time);
            // this.server.update(time);
        }
        this.parent.operationDone();
    }

    public void run() {
        //        System.out.println("ThreadedUpdaterStarted");
        //       this._decreaseTime();
        //       this._updateElements();
        while (true) {
            this.work();
        }
    }
}