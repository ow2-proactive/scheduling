package essai.proplayer;

import java.awt.*;
import java.util.LinkedList;


/**
 * a ProStream player
 * @see ProStream
 *this class situed on side Client
 */
public class ImageBlaster extends Canvas implements Runnable {
    private MediaTracker tracker;
    private Image anim;
    private int index;
    private LinkedList list;
    private LinkedList tempo;
    private LinkedList myList;
    private Thread animator;
    private boolean then;
    private int sizeBuffer;

    /**Minimal size of the buffer*/
    private int maxBufSize;

    /**Maximal size of the buffer*/
    private GUI gui;

    /**Constructor @param l the buffer containing the images*/
    public ImageBlaster(LinkedList l, GUI gui) {
        super();
        myList = l;
        list = l;
        tempo = l;
        index = 0;
        tracker = new MediaTracker(this);
        then = true;
        this.gui = gui;
    }

    /**Start the animation thread*/
    public void start() {
        list = myList;
        tempo = myList;
        index = 0;
        then = true;
        //if(animator == null)
        animator = new Thread(this);
        animator.start();
    }

    /**Stop the animation thread*/
    public void stop() {
        then = false;
        if (animator != null) {
            animator = null;
        }
        list.clear();
        tempo.clear();
        myList.clear();
    }

    /**notify the ImageBlaster*/
    public synchronized void notifyListChange() {
        notifyAll();
    }

    /**returns the number of elements in the Buffer*/
    public synchronized int getBufferSize() {
        return list.size();
    }

    /**definiton of the maximum images in buffer*/
    public void setMaxBufSize(int size) {
        maxBufSize = size;
    }

    /**return the maximum of images in the buffer*/
    public int maxBufSize() {
        return maxBufSize;
    }

    /**Runs the animation thread*/
    public void run() {

        /**Begin the movie (use for Benchmark)*/
        long begin;

        /**Stop the movie (use for Benchmark)*/
        long finish;

        /**movie's space time (use for Benchmark)*/
        long spaceTime;
        long space1;
        long space2;
        long waiting;

        /**First wait for all of the animation*/
        /**frames to finish loading. Then, loop and*/
        /**increment the animation frame index*/
        Thread me = Thread.currentThread();
        begin = System.currentTimeMillis();
        while (then) {
            while (animator == me) {
                space1 = System.currentTimeMillis();
                synchronized (this) {
                    finish = System.currentTimeMillis();
                    spaceTime = finish - begin;
                    while (list.size() == 0) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            System.out.println("InterruptedException e " +
                                e.getMessage());
                        }
                    }
                }
                anim = getToolkit().createImage((byte[]) list.removeFirst());
                tracker.addImage(anim, index);
                try {
                    tracker.waitForID(index);
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException in ImageBlaster " +
                        e);
                    return;
                }
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    System.out.println(
                        "InterruptedException in ImageBlaster.java " + e);
                    break;
                }
                space2 = System.currentTimeMillis();
                waiting = space2 - space1;
                while (waiting < 40) {
                    space2 = System.currentTimeMillis();
                    waiting = space2 - space1;
                    System.out.println("**" + waiting + "**");
                }
                repaint();
                index++;
                if (list.size() == 0) {
                    System.out.println();
                    System.out.println("BENCHMARK: ****" + spaceTime + "****");
                }
            }
        }
    }

    /**Just calls the paint method*/
    public void update(Graphics g) {
        this.paint(g);
    }

    /**set the size of the buffer*/
    public void setSize(int n) {
        sizeBuffer = n;
    }

    /**return the size of the buffer*/
    public int giveSizeBuffer() {
        return sizeBuffer;
    }

    /**return the size of the LinkedList*/
    public int giveSizeList() {
        return list.size();
    }

    /**return number of the image*/
    public int giveNumberImage() {
        return index;
    }

    /**The paint method
     *@param g the current Graphics
     */
    public void paint(Graphics g) {
        if ((list.size() > 0) && (anim != null)) {
            try {
                g.drawImage(anim, 55, 20, this);
            } catch (Exception e) {
                System.out.println("Exception in ImageBlaster.java " + e);
            }
        }
    }
}
