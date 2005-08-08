package essai.proplayer;

import java.util.Vector;


/**
 * a ProStream client
 * @see ProStream
 */

//This class Set a connexion with the Server
public abstract class StreamClient implements java.io.Serializable { //makes it serializable

    /**array of ProPlayers*/
    Vector playerArray;

    /**
     * no arg constructor -specific to ProActive
     */
    public StreamClient() {
    }

    /**
     * @param n default number of servers to connect
     */
    public StreamClient(Integer n) {
        playerArray = new Vector(n.intValue());
        //System.out.println("StreamClient("+n+")");
    }

    /**Object == image,song*/
    /**id == id of the server*/
    public void next(Object object, int id) {
        ((Player) playerArray.elementAt(id)).stream.add(object);
    }

    /**
     * adds a player to the array of players for this client
     * player : the player to be added
     */
    public void addPlayer(Player player) {
        System.out.println("StreamClient: addPlayer");
        playerArray.add(player);
    }

    /**close a StreamClient*/
    public void close() {
        playerArray.clear();
    }

    /**A method for the congestion*/
    public void aSend() {
    }

    /**A method for the congection*/
    public void aReceive() {
    }
}
