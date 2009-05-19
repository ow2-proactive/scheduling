/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.utils.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ow2.proactive.scheduler.common.util.BoundedLinkedList;


/**
 * VisualConsole is a visual implementation of the {@link Console} interface.<br>
 * If this console is not started, it ensure that it won't write anything on the display.
 * This console include an historic and syntax colors.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class VisualConsole extends JFrame implements Console, KeyListener {

    private static final int HISTORY_SIZE = 40;
    private static final Object lock = new Object();
    private static final Color CARET_COLOR = Color.GREEN;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color FOREGROUND_COLOR = Color.WHITE;
    private static final int PROMPT_NB_LINES = 4;
    private JPanel jContentPane = null;
    private JScrollPane jScrollPane = null;
    private JScrollPane jScrollPanePrompt = null;
    private JTextArea jTextArea = null;
    private JTextArea jTextAreaPrompt = null;
    private LinkedList<String> history = null;
    private PipedReader reader;
    private PipedWriter writer;
    private PipedWriter internalPipedWriter;
    private String valueToReturn;
    private boolean started = false;
    private String prompt = " > ";

    /**
     * Create a new instance of VisualConsole
     *
     */
    public VisualConsole() {
        super();
        initialize();
    }

    /**
     * Create a new instance of VisualConsole using the given prompt.<br>
     * This constructor does not need a call to the {@link #start(String)} method. It
     * automatically starts the console with the given prompt.
     *
     * @param prompt the prompt to be displayed on the console.
     */
    public VisualConsole(String prompt) {
        this();
        start(prompt);
    }

    private void initialize() {
        this.setSize(1200, 768);
        this.setContentPane(getJContentPane());
        this.setTitle("Visual Controller");
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
            jContentPane.add(getJScrollPanePrompt(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());
        }
        return jScrollPane;
    }

    private JScrollPane getJScrollPanePrompt() {
        if (jScrollPanePrompt == null) {
            jScrollPanePrompt = new JScrollPane();
            jScrollPanePrompt.setViewportView(getJTextAreaPrompt());
        }
        return jScrollPanePrompt;
    }

    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setAutoscrolls(true);
            jTextArea.setEditable(false);
            jTextArea.setSelectionColor(Color.GREEN);
            jTextArea.setBackground(BACKGROUND_COLOR);
            jTextArea.setForeground(FOREGROUND_COLOR);
            jTextArea.setCaretColor(CARET_COLOR);
            jTextArea.setFont(getMonospaceFont());
            jTextArea.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_Z ||
                        e.getKeyCode() == KeyEvent.VK_COMMA) {
                        getJTextAreaPrompt().requestFocus();
                        getJTextAreaPrompt().append("" + e.getKeyChar());
                    }
                }

                public void keyReleased(KeyEvent e) {
                }

                public void keyTyped(KeyEvent e) {
                }
            });
        }
        return jTextArea;
    }

    private JTextArea getJTextAreaPrompt() {
        if (jTextAreaPrompt == null) {
            jTextAreaPrompt = new JTextArea();
            jTextAreaPrompt.setEditable(true);
            jTextAreaPrompt.setPreferredSize(new Dimension(0, 15 * PROMPT_NB_LINES));
            jTextAreaPrompt.setAutoscrolls(true);
            jTextAreaPrompt.setBackground(BACKGROUND_COLOR);
            jTextAreaPrompt.setForeground(FOREGROUND_COLOR);
            jTextAreaPrompt.setCaretColor(CARET_COLOR);
            jTextAreaPrompt.addKeyListener(this);
            jTextAreaPrompt.setFont(getMonospaceFont());
        }
        return jTextAreaPrompt;
    }

    private Font font;

    private Font getMonospaceFont() {
        if (font == null) {
            font = new Font("Monospaced", Font.PLAIN, 12);
        }
        return font;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#error(java.lang.String, java.lang.Object[])
     */
    public Console error(String format, Object... args) {
        if (this.started) {
            //			jTextArea.setSelectedTextColor(SELECTED_TEXT_COLOR);
            //			jTextArea.setSelectionColor(SELECTION_COLOR);
            //			jTextArea.requestFocus();
            //jTextArea.setSelectionStart(jTextArea.getText().length());
            jTextArea.append(String.format(format, args));
            //jTextArea.setSelectionEnd(jTextArea.getText().length());
            jTextArea.append("\n");
            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#flush()
     */
    public void flush() {
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#handleExceptionDisplay(java.lang.String, java.lang.Throwable)
     */
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (this.started) {
            error(msg + " : " + (t.getMessage() == null ? t : t.getMessage()));
            try {
                if ("yes".equalsIgnoreCase(readStatement("Display stack trace ? (yes/no)"))) {
                    printStackTrace(t);
                }
            } catch (IOException e) {
                error("Could not display the stack trace");
            }
        } else {
            throw new RuntimeException("Console is not started !");
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#printStackTrace(java.lang.Throwable)
     */
    public void printStackTrace(Throwable t) {
        if (this.started) {
            jTextArea.append(stack2string(t));
            jTextArea.append("\n");
            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
        } else {
            throw new RuntimeException("Console is not started !");
        }
    }

    private static String stack2string(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#printf(java.lang.String, java.lang.Object[])
     */
    public Console printf(String format, Object... args) {
        if (this.started) {
            jTextArea.append(String.format(format, args));
            jTextArea.append("\n");
            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#readStatement()
     */
    public String readStatement() throws IOException {
        return readStatement(prompt);
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#readStatement(java.lang.String)
     */
    public String readStatement(String prompt) throws IOException {
        if (this.started) {
            jTextArea.append(prompt);
            jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
            jTextAreaPrompt.requestFocus();
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (valueToReturn.length() > 0) {
                jTextArea.append(valueToReturn + "\n");
                jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
            }
            return valueToReturn;
        } else {
            throw new RuntimeException("Console is not started !");
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#reader()
     */
    public Reader reader() {
        return reader;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#start(java.lang.String)
     */
    public Console start(String prompt) {
        this.prompt = prompt;
        this.history = new BoundedLinkedList<String>(HISTORY_SIZE);
        this.history.add("");
        try {
            this.reader = new PipedReader();
            this.internalPipedWriter = new PipedWriter(reader);
            this.writer = new PipedWriter();
            final BufferedReader br = new BufferedReader(new PipedReader(writer));
            new Thread() {
                @Override
                public void run() {
                    try {
                        char c;
                        while ((c = (char) br.read()) != -1) {
                            jTextArea.append("" + c);
                        }
                        jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.started = true;
        this.jTextAreaPrompt.requestFocus();
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#stop()
     */
    public void stop() {
        if (this.started) {
            this.started = false;
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#writer()
     */
    public PrintWriter writer() {
        return new PrintWriter(writer);
    }

    private int index = 0;
    private boolean keyPressedDuringHist = false;
    private boolean shiftPressed = false;
    private boolean ctrlPressed = false;

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
            keyPressedDuringHist = true;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (!jTextAreaPrompt.getText().contains("\n") || !keyPressedDuringHist) {
                    index++;
                    if (index > history.size() - 1) {
                        index = history.size() - 1;
                    }
                    jTextAreaPrompt.setText(history.get(index));
                    keyPressedDuringHist = false;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!jTextAreaPrompt.getText().contains("\n") || !keyPressedDuringHist) {
                    index--;
                    if (index < 0) {
                        index = 0;
                    }
                    jTextAreaPrompt.setText(history.get(index));
                    keyPressedDuringHist = false;
                }
                break;
            case KeyEvent.VK_ENTER:
                if (!shiftPressed) {
                    valueToReturn = "";
                    if (jTextAreaPrompt.getText().length() > 0) {
                        valueToReturn = jTextAreaPrompt.getText();
                        history.add(1, valueToReturn);
                        index = 0;
                        try {
                            //send to the one which is connected to the reader
                            internalPipedWriter.write(valueToReturn + "\n");
                        } catch (Exception ex) {
                        }
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
                break;
            case KeyEvent.VK_K:
                if (ctrlPressed) {
                    jTextAreaPrompt.setText("");
                    index = 0;
                }
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed = true;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlPressed = true;
                break;
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (!shiftPressed) {
                    jTextAreaPrompt.setText("");
                } else {
                    jTextAreaPrompt.append("\n");
                }
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed = false;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlPressed = false;
                break;
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

}
