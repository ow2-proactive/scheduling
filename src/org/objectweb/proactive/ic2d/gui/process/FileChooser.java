package org.objectweb.proactive.ic2d.gui.process;

//Standard Package
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.globus.GlobusProcess;

public class FileChooser extends JFrame {
	
		static Logger logger = Logger.getLogger(FileChooser.class.getName());

    private static final int DEFAULT_WIDTH =500;
    private static final int DEFAULT_HEIGHT = 250;         

    //the default file where to choose the GlobusSetupXML
    private static final String HOME=System.getProperty("user.home");
    private javax.swing.JFrame globusProcessFrame;  
    private javax.swing.JFrame frame;  
    private ExternalProcess  externalProcess;
    public boolean isReady=false;

    static private final String newline = "\n";

    public FileChooser(){}

    public FileChooser(ExternalProcess  ep) {
	super("FileChooser");
	frame=this;
	this.setSize(new java.awt.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        //Create the log first, because the action listeners
        //need to refer to it.
        final JTextArea log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        final JFileChooser fc = new JFileChooser(HOME+"/ProActive/descriptors/");
	fc.setSelectedFile(new File( HOME+"/ProActive/descriptors/"+"LocalGlobusSetup.xml" )); 
	
        //Create the select button
        ImageIcon selectIcon = new ImageIcon("../images/open.gif");
        JButton selectButton = new JButton("Select a File...", selectIcon);
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(FileChooser.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    log.append("Selecting: " + file.getName() + "." + newline);
                } else {
                    log.append("Select command cancelled by user." + newline);
                }
            }
        });



        //Create the read button
        JButton readButton = new JButton("read");
        readButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		File file = fc.getSelectedFile();
		//MAKE CHANGE HERE
		log.append("reading: "+ file.getName() );
		try{ 
		    ProActiveDescriptor pad = ProActive.getProactiveDescriptor("file:"+fc.getSelectedFile());
		    GlobusProcess globusProcess = (GlobusProcess)pad.getProcess("globusProcess");
		    if (globusProcess==null){
			logger.warn("globusProcess is null");
		    }
		    //this.setVisible(false);
		    frame.setVisible(false);
		    //pad.activateMappings(); ???
		    //On creer la fenetre GlobusProcessControlFrame
		    globusProcessFrame=new GlobusProcessControlFrame(globusProcess);
		    isReady=true;
		}catch(ProActiveException e2){
		    e2.printStackTrace();
		}
            }
        });
	
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectButton);
        buttonPanel.add(readButton);
 
        //Explicitly set the focus sequence.
        //saveButton.setNextFocusableComponent(selectButton);

        //Add the buttons and the log to the frame
        Container contentPane = getContentPane();
        contentPane.add(buttonPanel, BorderLayout.NORTH);
        contentPane.add(logScrollPane, BorderLayout.CENTER);
        
    }
    
    public boolean ready(){return isReady;}
    
    public void changeVisibilityGlobusProcessFrame(){
	if (globusProcessFrame.isVisible()) {
            globusProcessFrame.hide();
	} else {
            globusProcessFrame.show();
	}
    }
    
    public static void main(String[] args) {
        JFrame frame = new FileChooser();
	
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }
}

