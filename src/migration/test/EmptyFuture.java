package migration.test;

import java.io.Serializable;

public class EmptyFuture implements Serializable {

  public String name;


  public EmptyFuture() {
    name = "toto";
  }


  public String getName() {
    return name;
  }
}
