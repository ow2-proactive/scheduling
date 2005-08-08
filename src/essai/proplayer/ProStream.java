package essai.proplayer;

import java.util.LinkedList;


/**
 * Represents the stream of data a StreamClient creates from a StreamServer
 * @see StreamClient
 *this class is a buffer for the pictures of the Client
 */
public class ProStream implements java.io.Serializable {

    /**the internal buffer*/
    protected LinkedList buffer;

    //protected LinkedList buffer;

    /**reference on the client generating this stream*/
    private StreamClient client;

    /**define a GUI*/
    private StreamGUI gui;

    /**initializes the internal buffer*/
    public ProStream() {
        buffer = new LinkedList();
        gui = new StreamGUI(this);
    }

    /**
     * checks whether the stream can deliver an element
     */
    public boolean OneAvailable() {
        return (buffer.size() >= 1);
    }

    /**
     * the number of elements the stream holds
     */
    public int nbAvailable() {
        return buffer.size();
    }

    /**
     * retrieves the first element from the stream
     */
    public Object getNext() {
        return buffer.removeFirst();
    }

    /**
     * retrieves all the chunks available from the client
     * ie. flushes the client request buffer
     */
    protected void getAllNexts() {
        System.out.println("getAllNexts");
    }

    /**
     * method used by the StreamClient to fill this stream
     * @param object the element to be added
     */
    public void add(Object object) {
        //System.out.println("ProStream.add() : "+object.toString() + ")");
        buffer.add(object);
    }

    /**
     * closes the stream
     */
    public void clear() {

        /**removes all of the elements from this buffer*/
        buffer.clear();
    }
}
