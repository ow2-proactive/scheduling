package essai.proplayer;


/**
 * Class generating a ProPlayer
 * @see ProStream
 */
public class Player implements java.io.Serializable {

    /**this player's server*/
    StreamServer server;

    /**this player's buffer*/
    ProStream stream;

    /**this player's gui;*/
    GUI gui;

    /**control the Thread*/
    private boolean then = true;

    public Player(StreamServer s) {
        this.server = s;
        this.stream = new ProStream();
        this.gui = new GUI(s, stream);
    }

    public void start() {
        this.gui.showPic.start();
        then = true;
    }

    public void stop() {
        this.stream = new ProStream();
        this.gui.showPic.stop();
        then = false;
    }

    public void run() {
        this.gui.showPic.start();
        while (then) {
            if (stream.nbAvailable() >= 0) {
                this.gui.showPic.start();
            }
        }
    }

    public void stopPlayer() {
        this.gui.showPic.stop();
    }
}
