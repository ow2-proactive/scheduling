package essai.proplayer;


/**A thread to animated the JProgressBar*/
public class BufferBlaster implements Runnable {
  /**The GUI*/
  private StreamGUI gui;
  /**the ProStream*/
  private ProStream stream;
  /**a Thread*/
  private Thread animator;


  /**constructor*/
  public BufferBlaster(StreamGUI gui, ProStream stream) {
    this.gui = gui;
    this.stream = stream;
  }


  /**start the Thread*/
  public void start() {
    animator = new Thread(this);
    animator.start();
  }


  /**stop the Thread*/
  public void stop() {
    if (animator != null)
      animator = null;
  }


  /**run the Thread*/
  public void run() {
    Thread me = Thread.currentThread();
    while (me == animator) {
      gui.barValue(stream.nbAvailable());
    }
  }

}
