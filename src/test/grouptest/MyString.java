package test.grouptest;


public class MyString implements java.io.Serializable {


    private String data;



    public MyString () {
	data = "";
    }

    public MyString (String s) {
	data = s;
    }



    public String toString () {
	return data;
    }

    public void display () {
	System.out.println(data);
    }

}
