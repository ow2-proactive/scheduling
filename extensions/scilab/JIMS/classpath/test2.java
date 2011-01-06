import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class test2 extends Frame{
  
    ImageIcon img;
    
    public static void launch(String img){
	test2 ai = new test2(img);
    }
    
    public test2(String path) {
	super("Image Frame for " + path);
	img = new ImageIcon(path);
	setSize(img.getIconWidth(), img.getIconHeight());
	setVisible(true);
	addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent we){
		    dispose();
		}
	    });
    }

    public void paint(Graphics g) {
	g.drawImage(img.getImage(), 0, 0, this);
    }
}