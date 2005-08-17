///*
// * Created on Jul 27, 2004
// *
// */
//package org.objectweb.proactive.core.exceptions.manager;
//
//import org.objectweb.proactive.core.exceptions.NonFunctionalException;
//import org.objectweb.proactive.core.exceptions.handler.Handler;
//
//import java.awt.Dimension;
//
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//import javax.swing.JTextArea;
//
//
///**
// * @author agenoud
// *
// */
//public class HandlerWindow extends JFrame {
//    private JTabbedPane jTabbedPane = null;
//    private JPanel jPanelHandler = null;
//    private JPanel jPanelNode = null;
//    private JPanel jPanelProblem = null;
//    private JPanel jPanelSuggestion = null;
//    private JTextArea jTextArea = null;
//    private JTextArea jTextArea1 = null;
//    private JTextArea jTextArea2 = null;
//    private JTextArea jTextArea3 = null;
//
//    /**
//     * This is the default constructor
//     */
//    public HandlerWindow(NonFunctionalException nfe, Handler h, Object info) {
//        super();
//        initialize(nfe, h, info);
//    }
//
//    /**
//     * This method initializes this
//     *
//     * @return void
//     */
//    private void initialize(NonFunctionalException nfe, Handler h, Object info) {
//        this.setForeground(java.awt.Color.lightGray);
//        this.setContentPane(getJTabbedPane());
//        this.setSize(400, 300);
//        this.setTitle("Handler of " + nfe.getDescription());
//
//        jTextArea.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
//        jTextArea.setForeground(java.awt.Color.red);
//        jTextArea.setText("Handler of " + nfe.getDescription() + "\n");
//
//        jTextArea1.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
//        jTextArea1.setForeground(java.awt.Color.black);
//        jTextArea1.setText("ERROR on NODE " + ((String) info) + "\n");
//
//        jTextArea2.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
//        jTextArea2.setForeground(java.awt.Color.black);
//        jTextArea2.setText("PROBLEM is " + nfe.getMessage() + "\n");
//
//        jTextArea3.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
//        jTextArea3.setForeground(java.awt.Color.black);
//        jTextArea3.setText("Solution 1: UPDATE HANDLER [" +
//            h.getClass().getName() + "]\n\n");
//        jTextArea3.append("Solution 2: SET a new HANDLER for NFE [" +
//            nfe.getClass().getName() + "]\n");
//    }
//
//    /**
//     * This method initializes jTabbedPane
//     *
//     * @return javax.swing.JTabbedPane
//     */
//    private JTabbedPane getJTabbedPane() {
//        if (jTabbedPane == null) {
//            jTabbedPane = new JTabbedPane();
//            jTabbedPane.addTab("Handler", null, getJPanelHandler(), null);
//            jTabbedPane.addTab("Node", null, getJPanelNode(), null);
//            jTabbedPane.addTab("Problem", null, getJPanelProblem(), null);
//            jTabbedPane.addTab("Suggestions", null, getJPanelSuggestion(), null);
//        }
//        return jTabbedPane;
//    }
//
//    /**
//     * This method initializes jPanelHandler
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJPanelHandler() {
//        if (jPanelHandler == null) {
//            jPanelHandler = new JPanel();
//            jPanelHandler.setName("Handler");
//            jPanelHandler.add(getJTextArea(), null);
//        }
//        return jPanelHandler;
//    }
//
//    /**
//     * This method initializes jPanelNode
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJPanelNode() {
//        if (jPanelNode == null) {
//            jPanelNode = new JPanel();
//            jPanelNode.setName("Node");
//            jPanelNode.add(getJTextArea1(), null);
//        }
//        return jPanelNode;
//    }
//
//    /**
//     * This method initializes jPanelProblem
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJPanelProblem() {
//        if (jPanelProblem == null) {
//            jPanelProblem = new JPanel();
//            jPanelProblem.setName("Problem");
//            jPanelProblem.add(getJTextArea2(), null);
//        }
//        return jPanelProblem;
//    }
//
//    /**
//     * This method initializes jPanelSuggestion
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJPanelSuggestion() {
//        if (jPanelSuggestion == null) {
//            jPanelSuggestion = new JPanel();
//            jPanelSuggestion.setName("suggestion");
//            jPanelSuggestion.add(getJTextArea3(), null);
//        }
//        return jPanelSuggestion;
//    }
//
//    /**
//     * This method initializes jTextArea
//     *
//     * @return javax.swing.JTextArea
//     */
//    private JTextArea getJTextArea() {
//        if (jTextArea == null) {
//            jTextArea = new JTextArea();
//            jTextArea.setEditable(false);
//            jTextArea.setWrapStyleWord(true);
//            jTextArea.setLineWrap(true);
//            //jTextArea.setPreferredSize(jPanelHandler.getSize());
//            jTextArea.setPreferredSize(new Dimension(380, 260));
//        }
//        return jTextArea;
//    }
//
//    /**
//     * This method initializes jTextArea1
//     *
//     * @return javax.swing.JTextArea
//     */
//    private JTextArea getJTextArea1() {
//        if (jTextArea1 == null) {
//            jTextArea1 = new JTextArea();
//            jTextArea1.setEditable(false);
//            jTextArea1.setLineWrap(true);
//            jTextArea1.setWrapStyleWord(true);
//            //jTextArea1.setPreferredSize(jTabbedPane.getSize());
//            jTextArea1.setPreferredSize(new Dimension(380, 260));
//        }
//        return jTextArea1;
//    }
//
//    /**
//     * This method initializes jTextArea2
//     *
//     * @return javax.swing.JTextArea
//     */
//    private JTextArea getJTextArea2() {
//        if (jTextArea2 == null) {
//            jTextArea2 = new JTextArea();
//            jTextArea2.setEditable(false);
//            jTextArea2.setWrapStyleWord(true);
//            jTextArea2.setLineWrap(true);
//            //jTextArea2.setPreferredSize(jPanelProblem.getSize());
//            jTextArea2.setPreferredSize(new Dimension(380, 260));
//        }
//        return jTextArea2;
//    }
//
//    /**
//     * This method initializes jTextArea3
//     *
//     * @return javax.swing.JTextArea
//     */
//    private JTextArea getJTextArea3() {
//        if (jTextArea3 == null) {
//            jTextArea3 = new JTextArea();
//            jTextArea3.setEditable(false);
//            jTextArea3.setWrapStyleWord(true);
//            jTextArea3.setLineWrap(true);
//            //jTextArea3.setPreferredSize(jPanelSuggestion.getSize());
//            jTextArea3.setPreferredSize(new Dimension(380, 260));
//        }
//        return jTextArea3;
//    }
//} //  @jve:decl-index=0:visual-constraint="4,4"
