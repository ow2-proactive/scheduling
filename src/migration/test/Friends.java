package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Friends implements Serializable {

  public Friends friend = null;


  public Friends() {

  }


  public void meetFriend(Object o) {
    System.out.println("Friends: meetFriend() I am gonna meet my friend :-)");
    try {
      ProActive.migrateTo(o);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void echo() {
    System.out.println("Friends: echo() I'am here :-)");

  }


  public void moveTo(String t) {
    try {
      ProActive.migrateTo(t);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;
  }


  public void setFriend(Friends f) {
    friend = f;
  }


  public void callFriend() {
    friend.echo();

  }

  //This call is supposed to create a Futur
  public EmptyFuture getFuture() {
    return new EmptyFuture();

  }


  public void getFutureFromFriend() {
    friend.getFuture();

  }
}
