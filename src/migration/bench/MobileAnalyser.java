package migration.bench;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ext.migration.NodeDestination;
import org.objectweb.proactive.ext.migration.MigrationStrategy;
import org.objectweb.proactive.ext.migration.MigrationStrategyManager;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.Body;

import java.io.Serializable;
import java.util.Calendar;

public class MobileAnalyser extends Object implements Active, Serializable {

  SearchResult results;
  String fileToAnalyze;
  String keyWord;
  //the time we called the start() method
  long startTime;
  //the execution time, i.e the time took to migrate, parse and come back
  long executionTime;
  MigrationStrategyManager migrationStrategyManager;


  public MobileAnalyser() {

  }


  public void initialStart(String s) {
    try {
      ProActive.migrateTo(s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void moveTo(String s) {
    try {
      migrationStrategyManager.onArrival("searchInFile");
      ProActive.migrateTo(s);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void start(String home, String dest) {
    startTime = System.currentTimeMillis();
    try {
      Body b = ProActive.getBodyOnThis();
      migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) b);
      MigrationStrategy myItinerary = migrationStrategyManager.getMigrationStrategy();
      //System.out.println("home = " + home + " dest = " +dest);
      //ProActive.migrateTo(s);
      //myItinerary = new MigrationStrategyImpl();
      myItinerary.add(new NodeDestination(dest, "searchInFile"));
      myItinerary.add(new NodeDestination(home, "displayResults"));
      migrationStrategyManager.startStrategy(b);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void prepareForSearch(String file, String word) {
    fileToAnalyze = file;
    keyWord = word;
  }


  public void searchInFile() {
    results = DataBase.searchLocalFile(fileToAnalyze, keyWord);
  }


  public void displayResults() {
    executionTime = System.currentTimeMillis() - startTime;
    //	System.out.println(" " +results.toString());
    Calendar rightNow = Calendar.getInstance();
    System.out.println("Bench over at " + rightNow.getTime());
    System.out.println("Execution time is  " + executionTime);//+ " and found " + results.size() + " occurences");
    //System.out.println("TOTAL EXECUTION TIME = " + executionTime);
  }


  public SearchResult getResults() {
    return results;
  }


  public static void main(String args[]) {
    //	char result[] = {};
    SearchResult localResult;
    // 	long startTime;
    // 	long endTime;


    if (args.length < 4) {
      System.out.println("Usage: java migration.bench.MobileAnalyser //homeHost/home //hostname/nodeName fileName keyWord");
      System.exit(-1);
    }

    try {
      //First we create the MobileAnalyser

      // System.out.println("XXXX 0 = "  +args[0] + " 1=" + args[1] +" 2=" +args[2] +" 3= "+args[3]);
      //System.out.flush();
 
      //Node node = new Node(args[0]);
      //     MobileAnalyser mobileAnalyser = (MobileAnalyser) ProActive.newActive("migration.bench.MobileAnalyser", null, new NodeLocator(args[0]));
      MobileAnalyser mobileAnalyser = (MobileAnalyser)ProActive.newActive("migration.bench.MobileAnalyser", null);
      mobileAnalyser.initialStart(args[0]);
      //   startTime = System.currentTimeMillis();
      mobileAnalyser.prepareForSearch(args[2], args[3]);
      // result = converter.getLocalFile("xaa");
      //  mobileAnalyser.moveTo(args[0]);
      // mobileAnalyser.displayResults();
      // localResult = mobileAnalyser.getResults();

      //  ProActive.waitFor(localResult);
      mobileAnalyser.start(args[0], args[1]);

      //   endTime = System.currentTimeMillis();
      //and now we display the results
      //System.out.println("" + localResult);
      // 	    System.out.println("Total execution time: " + (endTime - startTime));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
