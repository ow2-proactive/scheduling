package essai.proplayer;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * class generating ProStream chunks (gif files)
 * @see ProStream
 */

public class AnimatedGIF implements java.io.Serializable {
  private int L,W,PPS,index,cpt,bufferSize,endHead,i;
  private byte[] buffer,buf;
  private String ver,sign;
  private boolean EOF;
  private String file;


  /**
   * Constructor
   * @param file the name of the animated GIF file
   */
  public AnimatedGIF(String file) {
    L = W = PPS = index = cpt = endHead = i = 0;
    EOF = false;
    this.file = file;
    try {
      FileInputStream fd = new FileInputStream(file);
      bufferSize = fd.available();
      buf = new byte[bufferSize];
      fd.read(buf);
      fd.close();
    } catch (IOException e) {
      System.out.println("IOException in AnimatedGif.java " + e.getMessage());
    }
    buffer = new byte[bufferSize];
  }


  /**
   * method reading the header of an animated GIF file
   * extracts the version and signature and displays them
   * on the standard output
   */
  public void readHeader() {
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


  public void stop() {
    L = W = PPS = index = cpt = endHead = i = 0;
    EOF = false;
  }


  /**say if the media is finish*/
  public boolean endOfFile() {
    return EOF;
  }

}






