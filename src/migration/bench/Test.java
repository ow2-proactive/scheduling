package migration.bench;

public class Test {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Syntax: Test <regexp> <filename>");
      return;
    }

    SearchResult sr = DataBase.searchLocalFile(args[1], args[0]);

    System.out.println(sr);

    return;
  }
}
