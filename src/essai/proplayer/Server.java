package essai.proplayer;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.NodeFactory;

import java.io.FileInputStream;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * a ProStream N server
 * @see ProStream
 */
public class Server extends StreamServer implements org.objectweb.proactive.RunActive {
  /**chunk provider*/
  //AnimatedGIF producer;
  private int L,W,PPS,index,cpt,bufferSize,endHead,i;
  private String ver,sign;
  private boolean EOF;
  private byte[] buffer,buf;


  /**empty no arg constructor -specific to ProActive*/
  public Server() {
  }


  /**filename : name of the file to be read and served to the client*/
  public Server(String filename) {
    this.filename = filename;
  }


  /**
   * method reading the header of an animated GIF file
   * extracts the version and signature and displays them
   * on the standard output
   */
  public void readHeader() {
    index = cpt = endHead = i = 0;
    EOF = false;
    sign = new String(buf, 0, 3);
    ver = new String(buf, 3, 3);
    System.out.println("signature & version: " + sign + "\t" + ver);
    L = buf[6];
    W = buf[8];
    PPS = 0;
    while (buf[index] != -1)    /**0xFF*/ {
      buffer[index] = buf[index];
      index++;
    }
    while (buf[index] != -2)  /**0xFE*/ {
      buffer[index] = buf[index];
      index++;
    }
    while (buf[index] != -7)  /**0xF9*/ {
      buffer[index] = buf[index];
      index++;
    }
    while (buf[index] != 44)  /**0x2C*/ {
      buffer[index] = buf[index];
      index++;
    }
    cpt = index;
    endHead = index;
    EOF = false;
  }


  /**
   * method called by a StreamServer to retrieve the next picture from the file
   *loops on last picture
   *@return a static gif file in byte array format
   */
  public byte[] nextPicture() {
    int i = endHead + 1;
    if (buf[cpt] == 44) /**image start block*/ {
      buffer[endHead] = buf[cpt];
      cpt++;
      do {
        i = i + 1;
        cpt = cpt + 1;
        buffer[i] = buf[cpt];
      } while ((buf[cpt] != 44 || buf[cpt + 1] != 0 || buf[cpt + 2] != 0) && (cpt < (bufferSize - 1)) && (i < bufferSize - 1));  /**0x2C 0x00 0x00*/
      buffer[i] = 00;
      i = i + 1;
      buffer[i] = 59;
      i = i + 1;
      index = i;
    } else
      EOF = true;    /**now beginning block following*/
    byte[] buffer2 = new byte[index];
    for (i = 0; i < index; i++)
      buffer2[i] = buffer[i];
    return buffer2;
  }


  /**true if EOF*/
  public boolean endOfFile() {
    return EOF;
  }


  /**serves a ProStream <code>n</code> times*/
  public void startStream() {
    Server myself = (Server) ProActive.getStubOnThis();
    Body body = (Body) ProActive.getBodyOnThis();
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    L = W = PPS = index = cpt = endHead = i = 0;
    EOF = false;

    /**read the file, and put it in an array of bytes*/
    try {
      FileInputStream fd = new FileInputStream(filename);
      bufferSize = fd.available();
      buf = new byte[bufferSize];
      fd.read(buf);
      fd.close();
    } catch (java.io.IOException e) {
      System.out.println("IOException in AnimatedGif.java " + e.getMessage());
    }
    buffer = new byte[bufferSize];

    /**read header of the file*/
    readHeader();

    if (myself != null) {
      do {
        myself.getChunk();        /**the producer loops on the last picture*/
        service.flushingServeYoungest("getChunk");
      } while (!endOfFile());
    } else {
      /**there is an error*/
      System.out.println("Server: could not get a stub on myself");
      System.exit(1);
    }
  }


  /**has a higher priority in the request queue than getChunk()*/
  public void stopStream() {
    L = W = PPS = index = cpt = endHead = i = 0;
    EOF = false;
  }


  /**adds a chunk to the ProStream's buffer*/
  public synchronized void getChunk() {
    client.aSend();
    /**producer.nextPicture() == byte[]*/
    /**this.id == id of the Server*/
    client.next(nextPicture(), this.id);
    client.aReceive();
  }


  /**notify the Thread*/
  public synchronized void go() {
    notify();
  }

  /**the live method @param body this Active object's body*/
  public void runActivity(Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);

    /**wait for a registerClient*/
    service.blockingServeOldest("registerClient");
    while (true) {
      service.blockingServeOldest("startStream");  /**wait for a startStream*/
      this.serveGetChunks(body);
      if (!service.hasRequestToServe()) {
        System.out.println("waiting for new requests...");
        service.waitForRequest();
        /**This is blocking a call we use for waiting (using a non-active wait loop)*/
      }
    }
  }


  /**give the highest priority to stopStream requests*/
  private void serveGetChunks(Body body) {
    Request call = body.getRequestQueue().getOldest("stopStream");
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    boolean cleared = true;

    if (call != null) {
      cleared = false;
      service.flushingServeYoungest("stopStream");
      call = body.getRequestQueue().getOldest("startStream");
      if (call != null) {
        cleared = true;
      }
    }
    /**no more stopStreams in the queue*/
    if (cleared) { /** serve the other calls*/
      /** get an iterator over all the methods in the queue*/
      while (!body.getRequestQueue().isEmpty()) {
        body.serve(body.getRequestQueue().blockingRemoveOldest()); /**blocking*/
      }
    }
  }


  /**Main Method*/
  public static void main(String[] args) {
    String filename = null;
    Object server = new Object();

    /**launch Server*/
    try {
      /**specifying a file to be served*/
      ClassLoader cl = Server.class.getClassLoader();
      URL u = cl.getResource("essai/proplayer/femmehh.gif");
      filename = u.getFile();
      System.out.println(":-)" + filename);

      Object[] param = {filename};
      String hostName = java.net.InetAddress.getLocalHost().getHostName();
      server = (Server) ProActive.newActive("essai.proplayer.Server",
                                            param,
                                            NodeFactory.getNode("//localhost:7777/Node0"));
      ProActive.register(server, "//" + hostName + ":7777/proServer");
    } catch (ProActiveException e) {
      System.out.println("RegisteringException " + e.getMessage());
    } catch (UnknownHostException e) {
      System.out.println("Error :" + e.getMessage());
    } catch (Exception e) {
      System.out.println("Failed to create Active instance of Server :" + e);
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Server is ready...");
  }
}




