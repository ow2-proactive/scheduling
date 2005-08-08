package essai.proplayer;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * class generating ProStream chunks (gif files)
 * @see ProStream
 */
public class AnimatedGIF implements java.io.Serializable {
    private int L;
    private int W;
    private int PPS;
    private int index;
    private int cpt;
    private int bufferSize;
    private int endHead;
    private int i;
    private byte[] buffer;
    private byte[] buf;
    private String ver;
    private String sign;
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
            System.out.println("IOException in AnimatedGif.java " +
                e.getMessage());
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

        //  0xFF
        while (buf[index] != -1) {
            buffer[index] = buf[index];
            index++;
        }

        // 0xFE
        while (buf[index] != -2) {
            buffer[index] = buf[index];
            index++;
        }

        // 0xF9
        while (buf[index] != -7) {
            buffer[index] = buf[index];
            index++;
        }

        // 0x2C
        while (buf[index] != 44) {
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

        // image start block
        if (buf[cpt] == 44) {
            buffer[endHead] = buf[cpt];
            cpt++;
            do {
                i = i + 1;
                cpt = cpt + 1;
                buffer[i] = buf[cpt];
            } while (((buf[cpt] != 44) || (buf[cpt + 1] != 0) ||
                    (buf[cpt + 2] != 0)) && (cpt < (bufferSize - 1)) &&
                    (i < (bufferSize - 1)));

            //0x2C 0x00 0x00
            buffer[i] = 00;
            i = i + 1;
            buffer[i] = 59;
            i = i + 1;
            index = i;
        } else {
            EOF = true;
        }

        // now beginning block following
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
