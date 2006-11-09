package net.coregrid.gcmcca.example;


public class HelloComponent implements HelloPort {


	public String hello(String s) {
		System.err.println("Server received: " + s);
		return "Server received: " + s;
	}

}
