package functionalTests.activeobject.creation.local.newactive.constructors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class B {
    private String choosed = null;

    public B() {
    }

    public B(Object o) {
        choosed = "C1";
    }

    public B(String s) {
        choosed = "C2";
    }

    public B(int i) {
        choosed = "C3";
    }

    public B(long j) {
        choosed = "C4";
    }

    public B(Long j) {
        choosed = "C5";
    }

    public B(String s, Object o) {
        choosed = "C6";
    }

    public B(Object o, String s) {
        choosed = "C7";
    }

    public B(Collection o) {
        choosed = "C8";
    }

    public B(List o) {
        choosed = "C9";
    }

    public B(ArrayList o) {
        choosed = "C10";
    }

    public String getChoosed() {
        return choosed;
    }
}
