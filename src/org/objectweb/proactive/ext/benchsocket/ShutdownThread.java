package org.objectweb.proactive.ext.benchsocket;



public class ShutdownThread extends Thread {
    //    //singleton pattern because of java bug #4533
    //    private static ShutdownThread sh;
    //
    //    static {
    //        sh = new ShutdownThread();
    //        Runtime.getRuntime().addShutdownHook(sh);
    //    }
    //
    //    public static synchronized void addStream(BenchStream b) {
    //        ShutdownThread.sh.streamList.add(b);
    //        System.out.println("Adding current size is " +
    //            ShutdownThread.sh.streamList.size());
    //    }
    //
    //    public static synchronized boolean removeStream(BenchStream b) {
    //        boolean result = ShutdownThread.sh.streamList.remove(b);
    //        System.out.println("Removing current size is " +
    //            ShutdownThread.sh.streamList.size());
    //        return result;
    //    }
    //
    //    private LinkedList streamList;
    //
    //    private ShutdownThread() {
    //        this.streamList = new LinkedList();
    //    }
    //
    //    public void run() {
    //        ////        this.bos.displayTotal();
    //        synchronized (sh.streamList) {
    //        	System.out.println("Run on list with " + this.streamList.size() +
    //        	" elements");
    //            Iterator it = this.streamList.iterator();
    //            while (it.hasNext()) {
    //                ((BenchStream) it.next()).displayTotal();
    //            }
    //        }
    //    }
    protected boolean fakeRun;
    protected BenchStream stream;

    public ShutdownThread() {
    }

    public ShutdownThread(BenchStream s) {
        this.stream = s;
    }

    protected void fakeRun() {
        this.fakeRun = true;
        this.start();
    }

    public void run() {
        ////        this.bos.displayTotal();
    	//System.out.println("XXXfakerun " + fakeRun);
        if (!this.fakeRun) {
            this.stream.displayTotal();
        }
    }
}
