
package mpi.example;

import ProActiveMPI;
import testsuite.test.FunctionalTest;





public class Test extends FunctionalTest {

	static int  status;
	boolean result;
	
	public Test() {
		super("ProActiveMPI", "Test");
	}


	public void action() throws Exception {


                byte text[] = { 64, 65, 66, 67, 68, 69 };
		byte text2[] = new byte[10000];

		ProActiveMPI p = ProActiveMPI.setup( "MPI_C_Interface_sample" );
		
		p.startMPI();
		// for(int i=0; i<6; i++) text[i] = 'a';
		p.sendToMPI( text, 6, 1 );
		p.recvFromMPI( text2, 6, 1, status );
		for(int i=0; i<6; i++) System.out.println( "BRAVO: " + text2[i] );
		
		p.endMPI();
	 
		result = True;
	}


	public void initTest() throws Exception {
	}


	public void endTest() throws Exception {
    	
	}

	public boolean postConditions() throws Exception {
		return (result);
	}
    
	
	public static Object methodejava( int p1 )
	{
	    Test q = new Test();
		System.out.println( "methodejava est heureuse d'imprimer l'int que vous lui avez passé : " + p1 );
		q.status = 41;
		return q;
	}
	
}
