package modelisation.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ThreadedReader implements Runnable {

  private InputStream stream;
  private boolean running;


  public ThreadedReader(InputStream stream) {
    this.stream = stream;
    this.running = true;
  }


  public void setRunning(boolean r) {
    this.running = r;
  }


  public void run() {
    String s;
    BufferedReader br = new BufferedReader(new InputStreamReader(this.stream));
    System.out.println("ThreadedReader: run()");

    try {
      while (((s = br.readLine()) != null) && (running)) {
        System.out.println(s);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
