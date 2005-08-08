package essai.proplayer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * ProPlayer Graphical User Interface
 * @see ProStream
 */
public class GUI extends JFrame implements WindowListener, ActionListener,
    ChangeListener, MouseListener {

    /**JTextField*/

    //private JTextField textField;
    private JTextField statusBar;

    /**JPanel*/
    private JPanel commandPanel;
    private JPanel buttonPanel;
    private JPanel buttonPanel2;

    /**JButton*/
    private JButton stopButton;
    private JButton startButton;
    private JButton closeButton;

    /**ButtonGroup*/
    private ButtonGroup buttonGroup;

    /**JWindow*/
    private JWindow window;

    /**JTextArea*/
    private JTextArea textArea;

    /**StreamServer*/
    private StreamServer server;

    /**ProStream*/
    private ProStream stream;

    /**ImageBlaster*/
    ImageBlaster showPic;

    /**name of the user*/
    String name;

    /**
     * Constructor
     * @param server a StreamServer serving JPG or GIF files
     * @param stream the ProStream buffer that holds the images in byte array format
     */
    public GUI(StreamServer server, ProStream stream) {
        super("ProPlayer");
        this.server = server;
        this.stream = stream;
        setSize(750, 550);
        getContentPane().setLayout(new BorderLayout());

        /**definition of the statusBar*/
        statusBar = new JTextField();
        statusBar.setForeground(Color.black);
        statusBar.setBackground(Color.yellow);
        statusBar.setEditable(false);

        /**definition of the JPanel*/
        commandPanel = new JPanel(new GridLayout(4, 2));
        commandPanel.setBackground(Color.blue);
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.blue);
        buttonPanel2 = new JPanel();
        buttonPanel2.setBackground(Color.blue);

        /**definition of the buttonGroup*/
        buttonGroup = new ButtonGroup();

        /**definition of startButton*/
        startButton = new JButton("Start");
        startButton.setBackground(Color.white);
        startButton.setForeground(Color.red);
        startButton.addActionListener(this);
        startButton.addMouseListener(this);

        /**definition of stopButton*/
        stopButton = new JButton("Stop");
        stopButton.setBackground(Color.white);
        stopButton.setForeground(Color.red);
        stopButton.addActionListener(this);
        stopButton.addMouseListener(this);

        /**add to ButtonPanel*/
        buttonPanel.add(startButton);
        buttonPanel2.add(stopButton);

        /**WindowListener*/
        addWindowListener(this);

        /**MouseListener*/
        addMouseListener(this);

        /**add commandPanel to the JFrame*/
        getContentPane().add(commandPanel, BorderLayout.NORTH);

        /**add showPic to the JFrame*/
        showPic = new ImageBlaster(stream.buffer, this);
        showPic.setBackground(Color.white);
        showPic.setVisible(true);

        /**Maximum of images in the buffer*/
        showPic.setMaxBufSize(100);
        getContentPane().add(showPic, BorderLayout.CENTER);

        /**add the two buttonPanel to the JFrame*/
        getContentPane().add(buttonPanel, BorderLayout.WEST);
        getContentPane().add(buttonPanel2, BorderLayout.EAST);

        /**add the statusBar*/
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        /**get the login of the user*/
        name = System.getProperty("user.name");

        status("Welcome to ProPlayer " + name);
        init();
    }

    /** Internal methods*/
    public void status(String s) {
        statusBar.setText(s);
    }

    /**To init a JFrame */
    public void init() {
        setVisible(true);
    }

    /**set the Text*/
    public void setText(String s) {
        textArea.append(s);
    }

    /**When press Button Start*/
    private void start() {
        status("sends a startStream");
        showPic.setSize(0);
        showPic.setVisible(true);
        server.startStream();
        showPic.start();
    }

    /**when press Button stop*/
    private void stop() {
        status("sends a stopStream");
        showPic.stop();
        server.stopStream();
        stream.clear();
        showPic.setVisible(false);
        showPic.notifyListChange();
    }

    /**for ChangeListener*/
    public void stateChanged(ChangeEvent e) {
    }

    /**for ActionListener*/
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("Start")) {
            start();
        } else if (action.equals("Stop")) {
            stop();
        } else {
            status("I can't see what you mean !");
        }
    }

    /**for WindowListener*/
    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    /**for MouseListener*/
    public void mouseClicked(MouseEvent e) {
        status("");
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}
