/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.core.security.gui;

//
//import java.awt.BorderLayout;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.security.InvalidKeyException;
//import java.security.KeyPair;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.PrivateKey;
//import java.security.SignatureException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.util.Enumeration;
//import java.util.Vector;
//
//import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
//import javax.swing.JButton;
//import javax.swing.JFileChooser;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JRadioButton;
//import javax.swing.JScrollPane;
//import javax.swing.JTable;
//import javax.swing.JTextField;
//import javax.swing.JTextPane;
//import javax.swing.table.DefaultTableModel;
//
//import org.objectweb.proactive.core.security.CertTools;
//import org.objectweb.proactive.core.security.KeyTools;
//import org.objectweb.proactive.core.security.SecurityConstants;
//
//
///**
// * @author acontes
// *
// */
//public class ProActiveCertificateGeneratorGUI extends JFrame
//    implements ActionListener {
//    private String[] jTableColumnName = { "Entity Id", "DN" };
//    private int KEYSTORE_APPLICATION = 1;
//    private int KEYSTORE_ENTITY = 2;
//    private int currentKeyStoreType;
//    private javax.swing.JPanel jContentPane = null;
//    private JLabel jLabel = null;
//    private JTextPane jTextPanePublicKey = null;
//    private JLabel jLabel1 = null;
//    private JTextPane jTextPanePrivateKey = null;
//    private JButton jButton = null;
//    private KeyPair currentKeyPair = null;
//
//    //   private X509Certificate currentCertificate = null;
//    private KeyStore currentKeyStore = null;
//    private JScrollPane jScrollPane = null;
//    private JLabel jLabel2 = null;
//    private JTextField jTextFieldDistinguishedName = null;
//    private JLabel jLabel3 = null;
//    private JTextField jTextFieldValidity = null;
//    JFileChooser fc = new JFileChooser();
//    private JMenuBar jJMenuBar = null;
//    private JMenu jMenu = null;
//    private JMenuItem jMenuItemLoadCertificate = null;
//    private JPanel jContentPane1 = null;
//    private JFrame jFrameSubCertificate = null; //  @jve:decl-index=0:visual-constraint="693,550"
//    private JLabel jLabel4 = null;
//    private JLabel jLabel5 = null;
//    private JTextField jTextFieldSubCertificate = null;
//    private JPanel jPanel = null;
//    private JButton jButton1 = null;
//    private JButton jButtonSubCertificateOk = null;
//    private KeyStore subKeyStore = null;
//    private JButton jButton3 = null;
//    private JMenu jMenu1 = null;
//    private JMenuItem jMenuItem = null;
//    private JTable jTable = null;
//    private JScrollPane jScrollPane1 = null;
//    private JLabel jLabel6 = null;
//    private JTextField jTextFieldPath = null;
//    private JMenuItem jMenuItemSubCert = null;
//    private JLabel jLabel7 = null;
//    private JMenuItem jMenuSaveCert = null;
//    private String lastOpenedDir = null;
//    private JScrollPane jScrollPane2 = null;
//    private JMenuItem jMenuItemAppCert = null;
//    private JRadioButton jRadioButtonApplicationCertType = null;
//    private JRadioButton jRadioButton1EntityCertType = null;
//    private JLabel jLabel8 = null;
//    private JLabel jLabel9 = null;
//    private JLabel jLabel10 = null;
//    private ButtonGroup bgCertificateType = new ButtonGroup();
//
//    /**
//     * This is the default constructor
//     */
//    public ProActiveCertificateGeneratorGUI() {
//        super();
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    System.exit(0);
//                }
//            });
//        initialize();
//    }
//
//    /**
//     * This method initializes this
//     *
//     * @return void
//     */
//    private void initialize() {
//        this.setJMenuBar(getJJMenuBar());
//        this.setSize(685, 476);
//        this.setContentPane(getJContentPane());
//        this.setTitle("ProActive Certificate Generator");
//    }
//
//    /**
//     * This method initializes jContentPane
//     *
//     * @return javax.swing.JPanel
//     */
//    private javax.swing.JPanel getJContentPane() {
//        if (jContentPane == null) {
//            jLabel10 = new JLabel();
//            jLabel9 = new JLabel();
//            jLabel8 = new JLabel();
//            jLabel7 = new JLabel();
//            jLabel6 = new JLabel();
//            jLabel = new JLabel();
//            jLabel3 = new JLabel();
//            jLabel2 = new JLabel();
//            jLabel1 = new JLabel();
//            jContentPane = new javax.swing.JPanel();
//            jContentPane.setLayout(null);
//            jLabel.setBounds(16, 125, 96, 26);
//            jLabel.setText("Public key :");
//            jLabel.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jLabel1.setBounds(29, 225, 81, 30);
//            jLabel1.setText("Private key :");
//            jLabel1.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jLabel2.setBounds(10, 357, 134, 24);
//            jLabel2.setText("Subjet DN :");
//            jLabel2.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jLabel3.setBounds(11, 396, 132, 21);
//            jLabel3.setText("Validity (in days):");
//            jLabel3.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jLabel6.setBounds(20, 9, 93, 25);
//            jLabel6.setText("File :");
//            jLabel6.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jLabel7.setBounds(393, 78, 249, 29);
//            jLabel7.setText("Certificate chain :");
//            jLabel8.setBounds(154, 43, 77, 21);
//            jLabel8.setText("application");
//            jLabel9.setBounds(289, 44, 83, 23);
//            jLabel9.setText("Entity");
//            jLabel10.setBounds(16, 44, 106, 22);
//            jLabel10.setText("Certificate Type");
//            jContentPane.add(jLabel, null);
//            jContentPane.add(jLabel1, null);
//            jContentPane.add(getJButton(), null);
//            jContentPane.add(getJScrollPane(), null);
//            jContentPane.add(jLabel2, null);
//            jContentPane.add(getJTextFieldDistinguishedName(), null);
//            jContentPane.add(jLabel3, null);
//            jContentPane.add(getJTextFieldValidity(), null);
//            jContentPane.add(getJButton3(), null);
//            jContentPane.add(getJScrollPane1(), null);
//            jContentPane.add(jLabel6, null);
//            jContentPane.add(getJTextFieldPath(), null);
//            jContentPane.add(jLabel7, null);
//            jContentPane.add(getJScrollPane2(), null);
//            jContentPane.add(getJRadioButtonApplicationCertType(), null);
//            jContentPane.add(getJRadioButton1EntityCertType(), null);
//            jContentPane.add(jLabel8, null);
//            jContentPane.add(jLabel9, null);
//            jContentPane.add(jLabel10, null);
//            bgCertificateType.add(getJRadioButtonApplicationCertType());
//            bgCertificateType.add(getJRadioButton1EntityCertType());
//        }
//        return jContentPane;
//    }
//
//    /**
//     * This method initializes jTextPanePublicKey
//     *
//     * @return javax.swing.JTextPane
//     */
//    private JTextPane getJTextPanePublicKey() {
//        if (jTextPanePublicKey == null) {
//            jTextPanePublicKey = new JTextPane();
//            jTextPanePublicKey.setEditable(false);
//        }
//        return jTextPanePublicKey;
//    }
//
//    /**
//     * This method initializes jTextPanePrivateKey
//     *
//     * @return javax.swing.JTextPane
//     */
//    private JTextPane getJTextPanePrivateKey() {
//        if (jTextPanePrivateKey == null) {
//            jTextPanePrivateKey = new JTextPane();
//            jTextPanePrivateKey.setEditable(false);
//        }
//        return jTextPanePrivateKey;
//    }
//
//    /**
//     * This method initializes jButton
//     *
//     * @return javax.swing.JButton
//     */
//    private JButton getJButton() {
//        if (jButton == null) {
//            jButton = new JButton("Generate Key Pair");
//            jButton.setBounds(181, 319, 171, 24);
//            jButton.addActionListener(new java.awt.event.ActionListener() {
//                    public void actionPerformed(java.awt.event.ActionEvent e) {
//                        currentKeyPair = CertTools.keyPair(512);
//                        jTextPanePrivateKey.setText(currentKeyPair.getPrivate()
//                                                                  .toString());
//                        jTextPanePublicKey.setText(currentKeyPair.getPublic()
//                                                                 .toString());
//                    }
//                });
//        }
//        return jButton;
//    }
//
//    /**
//     * This method initializes jScrollPane
//     *
//     * @return javax.swing.JScrollPane
//     */
//    private JScrollPane getJScrollPane() {
//        if (jScrollPane == null) {
//            jScrollPane = new JScrollPane();
//            jScrollPane.setBounds(125, 121, 208, 90);
//            jScrollPane.setViewportView(getJTextPanePublicKey());
//        }
//        return jScrollPane;
//    }
//
//    /**
//     * This method initializes jTextFieldDistinguishedName
//     *
//     * @return javax.swing.JTextField
//     */
//    private JTextField getJTextFieldDistinguishedName() {
//        if (jTextFieldDistinguishedName == null) {
//            jTextFieldDistinguishedName = new JTextField();
//            jTextFieldDistinguishedName.setBounds(149, 360, 321, 24);
//        }
//        return jTextFieldDistinguishedName;
//    }
//
//    /**
//     * This method initializes jTextFieldValidity
//     *
//     * @return javax.swing.JTextField
//     */
//    private JTextField getJTextFieldValidity() {
//        if (jTextFieldValidity == null) {
//            jTextFieldValidity = new JTextField();
//            jTextFieldValidity.setBounds(248, 385, 67, 24);
//            jTextFieldValidity.setText("360");
//        }
//        return jTextFieldValidity;
//    }
//
//    public void actionPerformed(java.awt.event.ActionEvent ev) {
//        if (ev.getSource() == jMenuSaveCert) {
//            if (currentKeyPair == null) {
//                JOptionPane.showMessageDialog(this,
//                    "No RSA key !\n you have to generate a key pair prior to saving the certificate into a file",
//                    "Certificate Warning", JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//
//            try {
//                CertificateFactory cf = CertificateFactory.getInstance("X.509",
//                        "BC");
//
//                String dnEntry = jTextFieldDistinguishedName.getText();
//
//                X509Certificate certificate = CertTools.genSelfCert(dnEntry,
//                        Integer.parseInt(getJTextFieldValidity().getText()),
//                        null, currentKeyPair.getPrivate(),
//                        currentKeyPair.getPublic(), true);
//
//                System.out.println("ddd" + dnEntry);
//
//                System.out.println(certificate.toString());
//
//                currentKeyStore = KeyTools.createP12(SecurityConstants.KEYSTORE_ENTITY_PATH,
//                        currentKeyPair.getPrivate(), certificate,
//                        (Certificate[]) null);
//
//                JFileChooser fc = new JFileChooser(lastOpenedDir);
//
//                //fc.addActionListener(this);
//                //Handle open button action.
//                int returnVal = fc.showOpenDialog(ProActiveCertificateGeneratorGUI.this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    File file = fc.getSelectedFile();
//                    lastOpenedDir = file.getCanonicalPath();
//                    //This is where a real application would open the file.
//                    currentKeyStore.store(new FileOutputStream(file),
//                        "ha".toCharArray());
//                    System.out.println("writing: " + file.getName() + ".");
//                } else {
//                    System.out.println("Open command cancelled by user.");
//                }
//            } catch (CertificateException e1) {
//                e1.printStackTrace();
//            } catch (NoSuchProviderException e1) {
//                e1.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (SignatureException e) {
//                e.printStackTrace();
//            } catch (KeyStoreException e) {
//                e.printStackTrace();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        // loading a certificate
//        else if (ev.getSource() == jMenuItemLoadCertificate) {
//            File f = null;
//
//            JFileChooser fc = new JFileChooser(lastOpenedDir);
//
//            //fc.addActionListener(this);
//            //Handle open button action.
//            int returnVal = fc.showOpenDialog(ProActiveCertificateGeneratorGUI.this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                File file = fc.getSelectedFile();
//                try {
//                    lastOpenedDir = file.getCanonicalPath();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                loadCertificateFile(file);
//            } else {
//                System.out.println("Open command cancelled by user.");
//            }
//        }
//        // creating a sub certificate
//        else if (ev.getSource() == jMenuItemSubCert) {
//            if (currentKeyStore == null) {
//                JOptionPane.showMessageDialog(this,
//                    "No current certificate !\n you have to load or create one before generating an sub certificate",
//                    "Certificate Warning", JOptionPane.WARNING_MESSAGE);
//            } else {
//                getJTextFieldSubCertificate()
//                    .setText(jTextFieldDistinguishedName.getText());
//                getJFrameSubCertificate().setVisible(true);
//            }
//        } else if (ev.getSource() == jButtonSubCertificateOk) {
//            try {
//                CertificateFactory cf = CertificateFactory.getInstance("X.509",
//                        "BC");
//
//                KeyPair subKeyPair = CertTools.keyPair(512);
//
//                X509Certificate subCertificate = CertTools.genCert(jTextFieldSubCertificate.getText(),
//                        Long.parseLong(getJTextFieldValidity().getText()) * 3600 * 24,
//                        null, subKeyPair.getPrivate(), subKeyPair.getPublic(),
//                        true,
//                        ((X509Certificate) currentKeyStore
//                         .getCertificate(SecurityConstants.KEYSTORE_ENTITY_PATH)).getSubjectDN()
//                         .toString(), currentKeyPair.getPrivate(),
//                        ((X509Certificate) currentKeyStore.getCertificate(
//                            SecurityConstants.KEYSTORE_ENTITY_PATH)).getPublicKey());
//
//                System.out.println("->" + jTextFieldSubCertificate.getText());
//
//                subKeyStore = KeyStore.getInstance("PKCS12", "BC");
//
//                subKeyStore = KeyTools.createP12(SecurityConstants.KEYSTORE_ENTITY_PATH,
//                        subKeyPair.getPrivate(), subCertificate,
//                        currentKeyStore.getCertificateChain(
//                            SecurityConstants.KEYSTORE_ENTITY_PATH));
//
//                JFileChooser fc = new JFileChooser(lastOpenedDir);
//
//                //fc.addActionListener(this);
//                //Handle open button action.
//                int returnVal = fc.showOpenDialog(ProActiveCertificateGeneratorGUI.this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    File file = fc.getSelectedFile();
//                    lastOpenedDir = file.getCanonicalPath();
//                    //This is where a real application would open the file.
//                    subKeyStore.store(new FileOutputStream(file),
//                        "ha".toCharArray());
//                    System.out.println("writing: " + file.getName() + ".");
//                } else {
//                    System.out.println("Open command cancelled by user.");
//                }
//            } catch (CertificateException e1) {
//                e1.printStackTrace();
//            } catch (NoSuchProviderException e1) {
//                e1.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            } catch (SignatureException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        // creating an application certificate
//        else if (ev.getSource() == jMenuItemAppCert) {
//            if (currentKeyStore == null) {
//                JOptionPane.showMessageDialog(this,
//                    "No current certificate !\n you have to load or create one before generating an application certificate",
//                    "Certificate Warning", JOptionPane.WARNING_MESSAGE);
//            } else {
//                try {
//                    String reponse;
//                    String message = "Enter a name for the application certificate \nIt could be the name of the application";
//                    reponse = JOptionPane.showInputDialog(this, message);
//
//                    CertificateFactory cf = CertificateFactory.getInstance("X.509",
//                            "BC");
//
//                    KeyPair subKeyPair = CertTools.keyPair(512);
//
//                    X509Certificate subCertificate = CertTools.genCert(reponse,
//                            Long.parseLong(getJTextFieldValidity().getText()) * 360,
//                            null, subKeyPair.getPrivate(),
//                            subKeyPair.getPublic(), true,
//                            ((X509Certificate) currentKeyStore
//                             .getCertificate(
//                                SecurityConstants.KEYSTORE_ENTITY_PATH)).getSubjectDN()
//                             .toString(), currentKeyPair.getPrivate(),
//                            ((X509Certificate) currentKeyStore.getCertificate(
//                                SecurityConstants.KEYSTORE_ENTITY_PATH)).getPublicKey());
//
//                    System.out.println("->" + reponse);
//
//                    subKeyStore = KeyStore.getInstance("PKCS12", "BC");
//
//                    subKeyStore = KeyTools.createP12(SecurityConstants.KEYSTORE_APPLICATION_PATH,
//                            subKeyPair.getPrivate(), subCertificate,
//                            currentKeyStore.getCertificateChain(
//                                SecurityConstants.KEYSTORE_ENTITY_PATH));
//
//                    JFileChooser fc = new JFileChooser(lastOpenedDir);
//
//                    //fc.addActionListener(this);
//                    //Handle open button action.
//                    int returnVal = fc.showOpenDialog(ProActiveCertificateGeneratorGUI.this);
//
//                    if (returnVal == JFileChooser.APPROVE_OPTION) {
//                        File file = fc.getSelectedFile();
//                        lastOpenedDir = file.getCanonicalPath();
//                        //This is where a real application would open the file.
//                        subKeyStore.store(new FileOutputStream(file),
//                            "ha".toCharArray());
//                        System.out.println("writing: " + file.getName() + ".");
//                    } else {
//                        System.out.println("Open command cancelled by user.");
//                    }
//                } catch (CertificateException e1) {
//                    e1.printStackTrace();
//                } catch (NoSuchProviderException e1) {
//                    e1.printStackTrace();
//                } catch (InvalidKeyException e) {
//                    e.printStackTrace();
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                } catch (SignatureException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        } else if (ev.getSource() == jButton3) {
//            try {
//                KeyTools.getCertChain(currentKeyStore,
//                    SecurityConstants.KEYSTORE_ENTITY_PATH);
//            } catch (KeyStoreException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * This method initializes jJMenuBar
//     *
//     * @return javax.swing.JMenuBar
//     */
//    private JMenuBar getJJMenuBar() {
//        if (jJMenuBar == null) {
//            jJMenuBar = new JMenuBar();
//            jJMenuBar.add(getJMenu());
//            jJMenuBar.add(getJMenu1());
//        }
//        return jJMenuBar;
//    }
//
//    /**
//     * This method initializes jMenu
//     *
//     * @return javax.swing.JMenu
//     */
//    private JMenu getJMenu() {
//        if (jMenu == null) {
//            jMenu = new JMenu();
//            jMenu.setName("File");
//            jMenu.setText("File");
//            jMenu.add(getJMenuItem());
//        }
//        return jMenu;
//    }
//
//    /**
//     * This method initializes jMenuItemLoadCertificate
//     *
//     * @return javax.swing.JMenuItem
//     */
//    private JMenuItem getJMenuItemLoadCertificate() {
//        if (jMenuItemLoadCertificate == null) {
//            jMenuItemLoadCertificate = new JMenuItem();
//            jMenuItemLoadCertificate.setText("Load Certificate");
//            jMenuItemLoadCertificate.addActionListener(this);
//        }
//        return jMenuItemLoadCertificate;
//    }
//
//    /**
//     * This method initializes jContentPane1
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJContentPane1() {
//        if (jContentPane1 == null) {
//            jContentPane1 = new JPanel();
//            jLabel4 = new JLabel("sub Certificate Generator");
//            jLabel5 = new JLabel("Sub certificate DN");
//            jContentPane1.setLayout(new BorderLayout());
//            jLabel4.setText("JLabel");
//            jLabel5.setText("JLabel");
//            jContentPane1.add(jLabel4, java.awt.BorderLayout.NORTH);
//            jContentPane1.add(jLabel5, java.awt.BorderLayout.WEST);
//            jContentPane1.add(getJTextFieldSubCertificate(),
//                java.awt.BorderLayout.CENTER);
//            jContentPane1.add(getJPanel(), java.awt.BorderLayout.SOUTH);
//        }
//        return jContentPane1;
//    }
//
//    /**
//     * This method initializes jFrameSubCertificate
//     *
//     * @return javax.swing.JFrame
//     */
//    private JFrame getJFrameSubCertificate() {
//        if (jFrameSubCertificate == null) {
//            jFrameSubCertificate = new JFrame();
//            jFrameSubCertificate.setContentPane(getJContentPane1());
//            jFrameSubCertificate.setSize(379, 150);
//            jFrameSubCertificate.setTitle("jFrameSubCertificate");
//        }
//        return jFrameSubCertificate;
//    }
//
//    /**
//     * This method initializes jTextFieldSubCertificate
//     *
//     * @return javax.swing.JTextField
//     */
//    private JTextField getJTextFieldSubCertificate() {
//        if (jTextFieldSubCertificate == null) {
//            jTextFieldSubCertificate = new JTextField();
//        }
//        return jTextFieldSubCertificate;
//    }
//
//    /**
//     * This method initializes jPanel
//     *
//     * @return javax.swing.JPanel
//     */
//    private JPanel getJPanel() {
//        if (jPanel == null) {
//            jPanel = new JPanel();
//            jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
//            jPanel.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
//            jPanel.add(getJButton1(), null);
//            jPanel.add(getJButtonSubCertificateOk(), null);
//        }
//        return jPanel;
//    }
//
//    /**
//     * This method initializes jButton1
//     *
//     * @return javax.swing.JButton
//     */
//    private JButton getJButton1() {
//        if (jButton1 == null) {
//            jButton1 = new JButton();
//            jButton1.setText("Cancel");
//        }
//        return jButton1;
//    }
//
//    /**
//     * This method initializes jButtonSubCertificateOk
//     *
//     * @return javax.swing.JButton
//     */
//    private JButton getJButtonSubCertificateOk() {
//        if (jButtonSubCertificateOk == null) {
//            jButtonSubCertificateOk = new JButton();
//            jButtonSubCertificateOk.addActionListener(this);
//            jButtonSubCertificateOk.setText("Ok");
//        }
//        return jButtonSubCertificateOk;
//    }
//
//    /**
//     * This method initializes jButton3
//     *
//     * @return javax.swing.JButton
//     */
//    private JButton getJButton3() {
//        if (jButton3 == null) {
//            jButton3 = new JButton("");
//            jButton3.addActionListener(this);
//            jButton3.setBounds(514, 391, 141, 25);
//            jButton3.setText("Display as text");
//        }
//        return jButton3;
//    }
//
//    /**
//     * This method initializes jMenu1
//     *
//     * @return javax.swing.JMenu
//     */
//    private JMenu getJMenu1() {
//        if (jMenu1 == null) {
//            jMenu1 = new JMenu();
//            jMenu1.setText("Certificate");
//            jMenu1.add(getJMenuItemLoadCertificate());
//            jMenu1.add(getJMenuItemSubCert());
//            jMenu1.add(getJMenuItemAppCert());
//            jMenu1.add(getJMenuSaveCert());
//        }
//        return jMenu1;
//    }
//
//    /**
//     * This method initializes jMenuItem
//     *
//     * @return javax.swing.JMenuItem
//     */
//    private JMenuItem getJMenuItem() {
//        if (jMenuItem == null) {
//            jMenuItem = new JMenuItem();
//            jMenuItem.setText("Quit");
//            jMenuItem.addActionListener(new java.awt.event.ActionListener() {
//                    public void actionPerformed(java.awt.event.ActionEvent e) {
//                        System.exit(0);
//                    }
//                });
//        }
//        return jMenuItem;
//    }
//
//    /**
//     * This method initializes jTable
//     *
//     * @return javax.swing.JTable
//     */
//    private JTable getJTable() {
//        if (jTable == null) {
//            jTable = new JTable(new DefaultTableModel(jTableColumnName, 0));
//        }
//        return jTable;
//    }
//
//    /**
//     * This method initializes jScrollPane1
//     *
//     * @return javax.swing.JScrollPane
//     */
//    private JScrollPane getJScrollPane1() {
//        if (jScrollPane1 == null) {
//            jScrollPane1 = new JScrollPane();
//            jScrollPane1.setBounds(392, 112, 249, 183);
//            jScrollPane1.setViewportView(getJTable());
//        }
//        return jScrollPane1;
//    }
//
//    /**
//     * This method initializes jTextField
//     *
//     * @return javax.swing.JTextField
//     */
//    private JTextField getJTextFieldPath() {
//        if (jTextFieldPath == null) {
//            jTextFieldPath = new JTextField();
//            jTextFieldPath.setBounds(119, 8, 520, 25);
//            jTextFieldPath.setEditable(false);
//        }
//        return jTextFieldPath;
//    }
//
//    /**
//     * This method initializes jMenuItemAppCert
//     *
//     * @return javax.swing.JMenuItem
//     */
//    private JMenuItem getJMenuItemSubCert() {
//        if (jMenuItemSubCert == null) {
//            jMenuItemSubCert = new JMenuItem();
//            jMenuItemSubCert.setText("create sub certificate");
//            jMenuItemSubCert.setToolTipText(
//                "create a certificate signed by current certificate");
//            jMenuItemSubCert.addActionListener(this);
//        }
//        return jMenuItemSubCert;
//    }
//
//    /**
//     * This method initializes jMenuItemAppCert
//     *
//     * @return javax.swing.JMenuItem
//     */
//    private JMenuItem getJMenuSaveCert() {
//        if (jMenuSaveCert == null) {
//            jMenuSaveCert = new JMenuItem();
//            jMenuSaveCert.setText("Save");
//            jMenuSaveCert.setToolTipText(
//                "Save current certificate into a pkcs12 file");
//            jMenuSaveCert.addActionListener(this);
//        }
//        return jMenuSaveCert;
//    }
//
//    protected void loadCertificateFile(File file) {
//        try {
//            currentKeyStore = KeyStore.getInstance("PKCS12", "BC");
//            Certificate[] chain = null;
//            X509Certificate certificate = null;
//
//            jTextFieldPath.setText(file.getAbsoluteFile().toURI().toString());
//
//            //This is where a real application would open the file.
//            System.out.println("Opening: " + file.getName() + ".");
//            currentKeyStore.load(new FileInputStream(file), "ha".toCharArray());
//
//            Enumeration e = currentKeyStore.aliases();
//
//            System.out.println("--------------");
//            for (; e.hasMoreElements();) {
//                System.out.println(e.nextElement());
//            }
//            System.out.println("--------------");
//
//            // display certificate type 
//            if (currentKeyStore.getCertificate(
//                        SecurityConstants.KEYSTORE_APPLICATION_PATH) != null) {
//                getJRadioButtonApplicationCertType().setSelected(true);
//                System.out.println("cert type " + KEYSTORE_APPLICATION);
//                currentKeyStoreType = KEYSTORE_APPLICATION;
//                chain = currentKeyStore.getCertificateChain(SecurityConstants.KEYSTORE_APPLICATION_PATH);
//                certificate = ((X509Certificate) currentKeyStore.getCertificate(SecurityConstants.KEYSTORE_APPLICATION_PATH));
//                currentKeyPair = new KeyPair(certificate.getPublicKey(),
//                        (PrivateKey) currentKeyStore.getKey(
//                            SecurityConstants.KEYSTORE_APPLICATION_PATH, null));
//            } else {
//                getJRadioButton1EntityCertType().setSelected(true);
//                System.out.println("cert type " + KEYSTORE_ENTITY);
//                currentKeyStoreType = KEYSTORE_ENTITY;
//                chain = currentKeyStore.getCertificateChain(SecurityConstants.KEYSTORE_ENTITY_PATH);
//                certificate = ((X509Certificate) currentKeyStore.getCertificate(SecurityConstants.KEYSTORE_ENTITY_PATH));
//
//                currentKeyPair = new KeyPair(certificate.getPublicKey(),
//                        (PrivateKey) currentKeyStore.getKey(
//                            SecurityConstants.KEYSTORE_ENTITY_PATH, null));
//            }
//
//            System.out.println(currentKeyStore + " chain length : " +
//                chain.length + " ++" + certificate.getSubjectDN());
//
//            jTextFieldDistinguishedName.setText(certificate.getSubjectDN()
//                                                           .toString());
//            jTextPanePrivateKey.setText(currentKeyPair.getPrivate().toString());
//            jTextPanePublicKey.setText(currentKeyPair.getPublic().toString());
//
//            DefaultTableModel tableModel = (DefaultTableModel) jTable.getModel();
//
//            // empty jtable 
//            for (int i = 0; i < tableModel.getRowCount(); i++) {
//                tableModel.removeRow(i);
//            }
//            for (int i = 1; i < chain.length; i++) {
//                Vector<String> v = new Vector<String>();
//                v.addElement(new Integer(i).toString());
//                v.addElement(((X509Certificate) chain[i]).getSubjectDN()
//                              .toString());
//                tableModel.addRow(v);
//            }
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (UnrecoverableKeyException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * This method initializes jScrollPane2
//     *
//     * @return javax.swing.JScrollPane
//     */
//    private JScrollPane getJScrollPane2() {
//        if (jScrollPane2 == null) {
//            jScrollPane2 = new JScrollPane();
//            jScrollPane2.setBounds(127, 219, 207, 94);
//            jScrollPane2.setViewportView(getJTextPanePrivateKey());
//        }
//        return jScrollPane2;
//    }
//
//    /**
//     * This method initializes jMenuItemAppCert
//     *
//     * @return javax.swing.JMenuItem
//     */
//    private JMenuItem getJMenuItemAppCert() {
//        if (jMenuItemAppCert == null) {
//            jMenuItemAppCert = new JMenuItem();
//            jMenuItemAppCert.setToolTipText("Create an application certificate");
//            jMenuItemAppCert.setText("Create an app cert");
//            jMenuItemAppCert.addActionListener(this);
//        }
//        return jMenuItemAppCert;
//    }
//
//    /**
//     * This method initializes jRadioButtonApplicationCertType
//     *
//     * @return javax.swing.JRadioButton
//     */
//    private JRadioButton getJRadioButtonApplicationCertType() {
//        if (jRadioButtonApplicationCertType == null) {
//            jRadioButtonApplicationCertType = new JRadioButton();
//            jRadioButtonApplicationCertType.setBounds(128, 44, 21, 21);
//            jRadioButtonApplicationCertType.setEnabled(true);
//        }
//        return jRadioButtonApplicationCertType;
//    }
//
//    /**
//     * This method initializes jRadioButton1EntityCertType
//     *
//     * @return javax.swing.JRadioButton
//     */
//    private JRadioButton getJRadioButton1EntityCertType() {
//        if (jRadioButton1EntityCertType == null) {
//            jRadioButton1EntityCertType = new JRadioButton();
//
//            jRadioButton1EntityCertType.setBounds(255, 44, 21, 21);
//            jRadioButton1EntityCertType.setEnabled(true);
//        }
//        return jRadioButton1EntityCertType;
//    }
//
//    public static void main(String[] args) {
//    }
//} //  @jve:decl-index=0:visual-constraint="81,117"
