import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class test
{
    public String s;

    public test(String str) {
	s = "Constructor has been called with "+str;
    }

    public String toto(double d) {
	return s + " and the number is " + d;
    }

    public static void launch()
    {
	//Fenetre fenetre = new Fenetre();
	//fenetre.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	//fenetre.show();
	System.out.println("salut calixte denizet");
    }

    public static String write(String s) {
	return "Salut " + s;
    }

    public static double F(double x) {
	return x;
    }
    
    public static double[] F(double[] x) {
	return x;
    }
    
    public static double[][] F(double[][] x) {
	return x;
    }
    
    public static int F(int x) {
	return x;
    }

    public static int[] F(int[] x) {
	return x;
    }

    public static int[][] F(int[][] x) {
	return x;
    }

    public static short F(short x) {
	return x;
    }

    public static short[] F(short[] x) {
	return x;
    }

    public static short[][] F(short[][] x) {
	return x;
    }

    public static byte F(byte x) {
	return x;
    }

    public static byte[] F(byte[] x) {
	return x;
    }

    public static byte[][] F(byte[][] x) {
	return x;
    }

    public static boolean F(boolean x) {
	return x;
    }

    public static boolean[] F(boolean[] x) {
	return x;
    }
    
    public static boolean[][] F(boolean[][] x) {
	return x;
    }

    public static String F(String x) {
	return x;
    }

    public static String[] F(String[] x) {
	return x;
    }

    public static String[][] F(String[][] x) {
	return x;
    }

    public static long F(long x) {
	return x;
    }

    public static long[] F(long[] x) {
	return x;
    }

    public static long[][] F(long[][] x) {
	return x;
    }

    public static char F(char x) {
	return x;
    }

    public static char[] F(char[] x) {
	return x;
    }

    public static char[][] F(char[][] x) {
	return x;
    }

    public static float F(float x) {
	return x;
    }

    public static float[] F(float[] x) {
	return x;
    }
    
    public static float[][] F(float[][] x) {
	return x;
    }

    public static String d(String s) {
	return "Начало сессии";
    }
    
    public static String[] e(String st) {
	String[] s = new String[2];
	s[0] = "Salut"; s[1] = "Calixte";
	if (st == null) {
	    s[0] = "Salut"; s[1] = "Erell";
	}
	return s;
    }

    public static String[][] f() {
	String[][] s = new String[2][2];
	s[0][0] = "Salut"; s[0][1] = "éàèç";s[1][0] = "Denizet"; s[1][1] = "Comment ca va ?";
	return s;
    }

    public static void g(boolean[] b) {
	String str = b[0] ? "vrai" : "faux";
	String str1 = b[1] ? "vrai" : "faux";
	System.out.println("!" + str + " " + str1 + "!");
    }

    public static boolean h() {
	return true;
    }

    public static boolean[] i() {
	boolean[] t = new boolean[2];t[0]=true;t[1]=false;
	return t;
    }
    
    public static boolean[][] j() {
	boolean[][] t = new boolean[2][2];t[0][0]=true;t[1][1]=false;t[1][0]=true;t[0][1]=true;
	return t;
    }

    public static double[][] transp(double[][] d) {
	double[][] td = new double[d[0].length][d.length];
	for (int i = 0; i < d.length; i++) {
	    for (int j = 0; j < d[0].length; j++) {
		td[j][i] = d[i][j];
	    }
	}
	return td;
    }

    public static byte[][] k(byte[][] b) {
	System.out.println("byte="+b[0][0]+","+b[0][1]+","+b[1][0]+","+b[1][1]);
	return b;
    }

    public static short[][] k(short[][] b) {
	System.out.println("short="+b[0][0]+","+b[0][1]+","+b[1][0]+","+b[1][1]);
	return b;
    }

    public static int[][] k(int[][] b) {
	System.out.println("int="+b[0][0]+","+b[0][1]+","+b[1][0]+","+b[1][1]);
	return b;
    }

    public static long[][] k(long[][] b) {
	System.out.println("long="+b[0][0]+","+b[0][1]+","+b[1][0]+","+b[1][1]);
	return b;
    }
}

class Fenetre extends JFrame
{
    public Fenetre()
    {
	Container leContenant = getContentPane();
	leContenant.setLayout(new FlowLayout());
	
	AffichageImage image = new AffichageImage("scilab_logo.gif");
	leContenant.add(image);
    }
}

class AffichageImage extends Canvas
{
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int largeurEcran = screenSize.width;
    int hauteurEcran = screenSize.height;
    Image image;
    
    public AffichageImage(String url)
    {
	image = getToolkit().getImage(url);
	prepareImage(image, this);
    }
    
    public void paint(Graphics g)
    {
	g.drawImage(image, 0, 0, this);
    }
    
    public boolean imageUpdate(Image image, int info, int x, int y, int l, int h)
    {
	if ((info & (WIDTH | HEIGHT)) != 0)
	    {
		setSize(l, h);
		getParent().getParent().getParent().getParent().setBounds( (largeurEcran - l) / 2, (hauteurEcran - h) / 2, l+8, h+32 );
	    }
	
	if ((info & (ALLBITS)) != 0)
	    {
		repaint();
		return false;
	    }
	else
	    {
		return true;
	    }
    }
}