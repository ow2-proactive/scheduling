package test.turnactive;

public class Dog implements java.io.Serializable {
  
  private String name;
  
  public Dog() {
  }
  
  public Dog(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
}