package migration.bench;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class SearchResult extends Object implements Serializable {

  protected Vector allLines;


  public SearchResult() {
    this.allLines = new Vector();
  }


  public void addLine(String line) {
    this.allLines.addElement(line);
  }


  public String[] getAllLines() {
    String[] result = new String[this.allLines.size()];
    Enumeration en = this.allLines.elements();
    int index = 0;
    while (en.hasMoreElements()) {
      result[index++] = (String)en.nextElement();
    }

    return result;
  }


  public String toString() // Implementation a la porcasse
  {
    String[] toto = this.getAllLines();
    String result = "Search result: " + toto.length + " item(s) found\n";
    for (int i = 0; i < toto.length; i++)
      result = result + toto[i] + "\n";

    return result;
  }

  // public int size()
  //     {
  // 	return this.allLines.size();
  //     }
}
