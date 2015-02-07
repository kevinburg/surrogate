package surrogate;

import java.util.*;
import java.io.*;
import java.net.*;

public class Surrogate {
	
	private static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException {
		
		System.out.println("Building base model");
		Model model = new Model("sherlock.txt");
		System.out.println("Done building base model.");
		
		serverSocket = new ServerSocket(8080); // Start, listen on port 8080
		System.out.println("Listening on 8080...");
		while (true) {
			try {
				Socket s = serverSocket.accept(); 
				(new HTTPPOSTServer(s, model)).start(); 
			} catch (Exception x) {
				System.out.println(x);
			}
		}
	}

}
