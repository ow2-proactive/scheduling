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


public class StreamGUI extends JFrame implements WindowListener, ActionListener,
    ChangeListener, MouseListener {

    /**JProgressBar*/
    private JProgressBar progressBar;

    /**JPanel*/
    private JPanel buttonPanel;

    /**ProStream*/
    private ProStream stream;

    /**JLabel*/
    private JLabel label;

    /**BufferBlaster*/
    private BufferBlaster bar;

    public StreamGUI(ProStream stream) {
        super("buffer");
        this.stream = stream;
        setSize(190, 135);
        getContentPane().setLayout(new GridLayout(1, 1));

        /**definition of the JPanel*/
        buttonPanel = new JPanel();

        /**definition of label*/
        label = new JLabel("state of buffer");

        /**definition of the progressBar*/
        progressBar = new JProgressBar(0);
        progressBar.setMinimum(0);
        progressBar.setMaximum(75);
        progressBar.setForeground(Color.red);
        progressBar.setBackground(Color.white);
        progressBar.setStringPainted(false);

        /**definiton of the BufferBlaster*/
        bar = new BufferBlaster(this, stream);

        /**add the progressBar to JPanel*/
        buttonPanel.add(label);
        buttonPanel.add(progressBar);

        /**add label and progressBar to the JFrame*/
        getContentPane().add(buttonPanel);

        /**start the Thread*/
        bar.start();

        /**To see the StreamGUI*/
        init();
    }

    /**To init a JFrame*/
    public void init() {
        setVisible(true);
    }

    /**set a value to the progressBar*/
    public void barValue(int nb) {
        progressBar.setValue(nb);
    }

    /**for ChangeListener*/
    public void stateChanged(ChangeEvent e) {
    }

    /**for ActionListener*/
    public void actionPerformed(ActionEvent e) {
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
