/**
  PROJECT NAME:
    PAPI (ProActive Parallel Interface)
  FILE NAME:
    PAPI_sample.java
  DEPENDENCY FILE(S):
    -

  DATE:
    01-28-04
  UPDATE:
    07-30-04
  REVISION NUMBER: 1AA-040730-00
  
  CHECKED:
    Yes

  LANGUAGE:
    C
  COMPILATION LINE:
    javac PAPI_sample.java
  ENVIRONMENT CONFIGURATION:
    PATH= MPI include path

  DESCRIPTION:
    Accessibility of the MPICH implementation library within a JAVA class
      and to JAVA code from C/MPI code
    JAVA sample program 
  REMARK(S):
    -
/**/


class PAPI_sample
{
	static int  status;

	public static Object methodejava( int p1 )
	{
	    PAPI_sample q = new PAPI_sample();
		System.out.println( "methodejava est heureuse d'imprimer l'int que vous lui avez passé : " + p1 );
		PAPI_sample.status = 41;
		return q;
	}

	 public static void main( String[] args )
	 {

                byte text[] = { 64, 65, 66, 67, 68, 69 };
		byte text2[] = new byte[10000];

		PAPI p = PAPI.setup( "PAPI_sample" );
		
		p.job();
		// for(int i=0; i<6; i++) text[i] = 'a';
		p.send( text, 6, 1 );
		p.recv( text2, 6, 1, status );
		for(int i=0; i<6; i++) System.out.println( "BRAVO: " + text2[i] );
		
		p.finalize();
	 
	 }
}


/**
  END OF FILE
/**/
